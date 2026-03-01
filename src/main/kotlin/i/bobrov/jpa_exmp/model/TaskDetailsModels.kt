package i.bobrov.jpa_exmp.tasks

import i.bobrov.jpa_exmp.dto.*
import java.time.Instant
import java.util.UUID

data class GoodsBaseInfo(
    val goodsId: UUID,
    val merchGoodsId: String,
    val goodsName: String,
    val imageUrl: String,
    val barcodes: List<String> = listOf("0000000000")
)

data class CellDto(val id: UUID, val address: String)

data class TaskBodyDetails(
    val bodyId: UUID,
    val goodsId: UUID,
    val shipmentNumber: String,
    val shipmentId: UUID,
    val pickingCellId: UUID,
    val quantity: Int,
    val cancelledQuantity: Int,
    val shortageReason: GoodsShortageReason?,
    val updatedAt: Instant?,
    val handlingUnitBarcode: String?,
    val stockQuality: StockQuality,
    val isNlo: Boolean
) {
    fun getExpectedQuantity(): Int = quantity
}

data class HandlingUnitBodyDetails(
    val pickedQuantity: Int,
    val lastPicked: Instant
)

data class HandlingUnitDetails(
    val huId: UUID,
    val targetCellId: UUID?,
    val status: HandlingUnitStatus,
    val barcode: String,
    val createdAt: Instant,
    val taskBodies: List<Pair<TaskBodyDetails, HandlingUnitBodyDetails>>
)

data class TaskDetailsResult(
    val taskId: UUID,
    val taskNumber: String,
    val status: PickingTaskStatus,
    val performerId: UUID?,
    val warehouseId: UUID,
    val priority: PickingTaskPriority,
    val pickingDeadline: Instant,
    val startedAt: Instant?,
    val pickingZoneId: UUID,
    val buffers: List<String>?,
    val creatorId: UUID,
    val createdAt: Instant,
    val sortMethodId: UUID,
    val completedAt: Instant?,
    val subMode: PickingSubMode?,

    val unpickedBodies: List<TaskBodyDetails>,
    val handlingUnits: Map<UUID, HandlingUnitDetails>
)