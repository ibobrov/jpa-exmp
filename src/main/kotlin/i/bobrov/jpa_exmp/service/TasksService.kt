package i.bobrov.jpa_exmp.tasks

import i.bobrov.jpa_exmp.common.*
import i.bobrov.jpa_exmp.extension.stubMissedKeys
import i.bobrov.jpa_exmp.util.toTasksGetDetailsGoods
import i.bobrov.jpa_exmp.dao.TasksRepo
import i.bobrov.jpa_exmp.dto.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class TasksService(
    private val tasksRepo: TasksRepo,
    private val virtualTopologyServiceClient: VirtualTopologyServiceClient,
    private val shipmentsService: ShipmentsService,
    private val userProfileService: UserProfileService,
    private val packingServiceClient: PackingServiceClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun getDetails(request: TasksGetDetailsRequest): TasksGetDetailsResponse {

        val taskDetailsResult = tasksRepo.findTaskDetailsById(request.taskId)
            ?: throw PickingServiceException(ErrorCode.TASK_IS_NOT_FOUND, "Task not found")

        val cellsForFind: MutableSet<UUID> = mutableSetOf()
        val goodsIdSet = HashSet<UUID>()

        taskDetailsResult.unpickedBodies.forEach { bodies -> goodsIdSet.add(bodies.goodsId) }
        taskDetailsResult.handlingUnits.values.forEach { hu ->
            hu.taskBodies.forEach { pair ->
                goodsIdSet.add(pair.first.goodsId)
                cellsForFind.add(pair.first.pickingCellId)
            }
        }

        cellsForFind.addAll(
            taskDetailsResult.handlingUnits.values
                .filter { it.targetCellId != null }
                .map { it.targetCellId!! }
        )
        cellsForFind.addAll(taskDetailsResult.unpickedBodies.map { it.pickingCellId })

        val goodsBaseInfoMap = getGoodsInfo(goodsIdSet)

        var quantity = 0
        var cancelledQuantity = 0
        var pickedQuantity = 0
        var lastActivity: Instant = taskDetailsResult.startedAt ?: taskDetailsResult.createdAt

        var cellsDtoMap = virtualTopologyServiceClient.getCellsByIds(taskDetailsResult.warehouseId, cellsForFind)
        val notFoundCells = cellsDtoMap.stubMissedKeys(
            keys = cellsForFind,
            stubFunction = { CellDto(it, UNKNOWN_STUB) },
            logConsumer = { log.warn("virtualTopologyService#getCellsByIds() missed ids={}", it) }
        )
        if (notFoundCells.isNotEmpty()) {
            cellsDtoMap = cellsDtoMap + notFoundCells.associateBy { it.id }
        }

        val unpickedGoods = taskDetailsResult.unpickedBodies.map {
            quantity += it.getExpectedQuantity()
            cancelledQuantity += it.cancelledQuantity

            it.toTasksGetDetailsGoods(
                goodsBaseInfo = goodsBaseInfoMap[it.goodsId]!!,
                pickedQuantity = 0,
                pickedAt = null,
                cellDto = cellsDtoMap[it.pickingCellId]!!,
            )
        }

        val isUtilization = listOf(PickingSubMode.UTILIZATION, PickingSubMode.UTILIZATION_NLO)
            .any { taskDetailsResult.subMode == it }

        val cargoDataByHU: Map<UUID, List<CargoUnitData>>? =
            if (taskDetailsResult.subMode in listOf(PickingSubMode.UTILIZATION, PickingSubMode.UTILIZATION_NLO)) packingServiceClient.getByHandlingUnitsCached(taskDetailsResult.handlingUnits.keys)
            else null

        val handlingUnits = taskDetailsResult.handlingUnits.map { entry ->
            val huId = entry.key
            val hu = entry.value

            TasksGetDetailsHandlingUnit(
                targetCellAddress = hu.targetCellId?.let { cellsDtoMap[it]?.address },
                status = hu.status,
                barcode = hu.barcode,
                goods = hu.taskBodies.map { pair ->
                    val taskBodyDetails = pair.first
                    val handlingUnitBodyDetails = requireNotNull(pair.second)

                    pickedQuantity += handlingUnitBodyDetails.pickedQuantity

                    taskBodyDetails.updatedAt?.let { if (it > lastActivity) lastActivity = it }
                    if (handlingUnitBodyDetails.lastPicked > lastActivity) lastActivity =
                        handlingUnitBodyDetails.lastPicked

                    val goodsBaseInfo = goodsBaseInfoMap[taskBodyDetails.goodsId]!!
                    taskBodyDetails.toTasksGetDetailsGoods(
                        goodsBaseInfo = goodsBaseInfo,
                        pickedQuantity = handlingUnitBodyDetails.pickedQuantity,
                        pickedAt = handlingUnitBodyDetails.lastPicked,
                        cellDto = cellsDtoMap[taskBodyDetails.pickingCellId]!!,
                    )
                }.sortedBy { goods -> goods.goodsId },
                cargoUnitNumber = if (isUtilization) cargoDataByHU?.get(huId)?.firstOrNull()?.cargoUnitNumber else null,
            )
        }.sortedBy { it.barcode }

        val huTaskBodyDetailsResults = taskDetailsResult.handlingUnits.values
            .flatMap { it.taskBodies }
            .map { it.first }
            .distinctBy { it.bodyId }

        quantity += huTaskBodyDetailsResults.sumOf { it.getExpectedQuantity() }
        cancelledQuantity += huTaskBodyDetailsResults.sumOf { it.cancelledQuantity }

        val userIds = listOfNotNull(taskDetailsResult.performerId, taskDetailsResult.creatorId).distinct()
        val userProfilesMap = if (userIds.isNotEmpty()) userProfileService.getProfilesByIds(userIds) else mapOf()

        return with(taskDetailsResult) {
            TasksGetDetailsResponse(
                taskId = taskId,
                taskNumber = taskNumber,
                status = status,
                performer = performerId?.let { id ->
                    userProfilesMap[id] ?: run {
                        log.warn("Performer profile with id={} not found", id)
                        User(phone = UNKNOWN_STUB, fullName = UNKNOWN_STUB)
                    }
                },
                subMode = subMode,
                pickedQuantity = pickedQuantity,
                quantity = quantity,
                cancelledQuantity = cancelledQuantity,
                priority = priority,
                pickingDeadline = pickingDeadline,
                startedAt = startedAt,
                downtime = countDowntime(lastActivity, status),
                pickingZone = virtualTopologyServiceClient.getZoneByIds(setOf(pickingZoneId))[pickingZoneId]?.code
                    ?: run {
                        log.warn("Zone with id={} not found", pickingZoneId)
                        UNKNOWN_STUB
                    },
                bufferZones = buffers ?: run {
                    log.warn("Buffer not defined for taskId={}", taskId)
                    listOf(UNKNOWN_STUB)
                },
                bufferZone = buffers?.firstOrNull() ?: run {
                    log.warn("Buffer not defined for taskId={}", taskId)
                    UNKNOWN_STUB
                },
                sortMethod = shipmentsService.getSortMethodByIds(warehouseId, setOf(sortMethodId))[sortMethodId]?.name
                    ?: run {
                        log.warn("Sort method with id={} not found", sortMethodId)
                        UNKNOWN_STUB
                    },
                creator = userProfilesMap[creatorId] ?: run {
                    log.warn("Creator profile with id={} not found", creatorId)
                    User(phone = UNKNOWN_STUB, fullName = UNKNOWN_STUB)
                },
                createdAt = createdAt,
                unpickedGoods = unpickedGoods,
                handlingUnits = handlingUnits,
                completedAt = completedAt,
            )
        }
    }

    private fun getGoodsInfo(goodsIds: Set<UUID>): Map<UUID, GoodsBaseInfo> =
        goodsIds.associateWith { id ->
            GoodsBaseInfo(
                goodsId = id,
                merchGoodsId = "MG-${id.toString().take(8)}",
                goodsName = "Goods-${id.toString().take(8)}",
                imageUrl = "https://example.com/${id}.png",
                barcodes = listOf("BC-${id.toString().takeLast(6)}")
            )
        }

    private fun countDowntime(lastActivity: Instant, status: PickingTaskStatus): Int =
        if (status == PickingTaskStatus.IN_PROGRESS) {
            val seconds = Instant.now().epochSecond - lastActivity.epochSecond
            (seconds / 60).toInt().coerceAtLeast(0)
        } else 0
}