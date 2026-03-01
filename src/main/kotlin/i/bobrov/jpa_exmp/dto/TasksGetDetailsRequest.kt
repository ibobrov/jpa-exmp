package i.bobrov.jpa_exmp.dto

import java.time.Instant
import java.util.UUID

const val UNKNOWN_STUB = "UNKNOWN"

data class TasksGetDetailsRequest(
    val taskId: UUID
)

data class User(
    val phone: String,
    val fullName: String
)

enum class PickingTaskStatus { IN_PROGRESS, COMPLETED, CANCELLED }
enum class PickingTaskPriority { LOW, MEDIUM, HIGH }
enum class PickingSubMode { UTILIZATION, UTILIZATION_NLO, DEFAULT }

enum class HandlingUnitStatus { CREATED, IN_PROGRESS, CLOSED }

enum class StockQuality { GOOD, BAD }
enum class GoodsShortageReason { DAMAGED, NOT_FOUND }

data class TasksGetDetailsResponse(
    val taskId: UUID,
    val taskNumber: String,
    val status: PickingTaskStatus,
    val performer: User? = null,
    val subMode: PickingSubMode? = null,
    val pickedQuantity: Int,
    val quantity: Int,
    val cancelledQuantity: Int,
    val priority: PickingTaskPriority,
    val pickingDeadline: Instant,
    val startedAt: Instant? = null,
    val downtime: Int,
    val pickingZone: String,
    @Deprecated("Использовать поле bufferZones")
    val bufferZone: String,
    val bufferZones: List<String>,
    val sortMethod: String,
    val creator: User,
    val createdAt: Instant,
    val completedAt: Instant?,
    val unpickedGoods: List<TasksGetDetailsGoods>,
    val handlingUnits: List<TasksGetDetailsHandlingUnit>,
)

data class TasksGetDetailsHandlingUnit(
    val targetCellAddress: String?,
    val status: HandlingUnitStatus,
    val barcode: String,
    val goods: List<TasksGetDetailsGoods>,
    val cargoUnitNumber: String?,
)

data class TasksGetDetailsGoods(
    val goodsId: UUID,
    val shipmentNumber: String,
    val shipmentId: UUID,
    val cellAddress: String,
    val cellHandlingUnitBarcode: String?,
    val merchGoodsId: String,
    val goodsName: String,
    val imageUrl: String,
    val quantity: Int,
    val cancelledQuantity: Int,
    val pickedQuantity: Int,
    val shortageReason: GoodsShortageReason? = null,
    val barcodes: List<String>,
    val pickedAt: Instant? = null,
    val stockQuality: StockQuality,
    val isNlo: Boolean,
)