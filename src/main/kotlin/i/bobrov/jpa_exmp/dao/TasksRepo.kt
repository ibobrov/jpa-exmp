package i.bobrov.jpa_exmp.dao

import i.bobrov.jpa_exmp.dto.*
import i.bobrov.jpa_exmp.tasks.HandlingUnitBodyDetails
import i.bobrov.jpa_exmp.tasks.HandlingUnitDetails
import i.bobrov.jpa_exmp.tasks.TaskBodyDetails
import i.bobrov.jpa_exmp.tasks.TaskDetailsResult
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

@Repository
class TasksRepo(
    private val jdbc: NamedParameterJdbcTemplate,
) {

    fun findTaskDetailsById(taskId: UUID): TaskDetailsResult? {
        val sql = """
      WITH pickeddata AS (
        SELECT
          picking_task_handling_unit_id,
          picking_task_body_id,
          max(created_at) AS last_picked,
          sum(quantity) AS quantity
        FROM picking_task_handling_unit_bodies
        WHERE picking_task_id = :task_id
        GROUP BY picking_task_handling_unit_id, picking_task_body_id
      )
      SELECT
        pt.picking_task_id,
        pt.task_number,
        pt.status,
        pt.performer_id,
        pt.warehouse_id,
        pt.priority,
        pt.picking_deadline_date,
        pt.started_at,
        pt.picking_zone_id,
        pt.buffers,
        pt.creator_id,
        pt.created_at,
        pt.sort_method_id,
        pt.completed_at,
        pt.sub_mode,

        ptb.picking_task_body_id AS ptb_id,
        ptb.goods_id,
        s.number AS shipment_number,
        ptb.shipment_id,
        ptb.picking_cell_id,
        ptb.quantity,
        ptb.cancelled_quantity,
        ptb.shortage_reason,
        ptb.updated_at AS ptb_updated_at,
        ptb.handling_unit_barcode,
        ptb.stock_quality,
        ptb.is_nlo,

        hu.picking_task_handling_unit_id AS hu_id,
        hu.target_cell_id AS hu_target_cell_id,
        hu.status AS hu_status,
        hu.barcode AS hu_barcode,
        hu.created_at AS hu_created_at,

        pd.quantity AS picked_quantity,
        pd.last_picked
      FROM picking_tasks pt
      JOIN picking_task_bodies ptb ON pt.picking_task_id = ptb.picking_task_id
      JOIN shipments s ON ptb.shipment_id = s.shipment_id
      LEFT JOIN pickeddata pd ON pd.picking_task_body_id = ptb.picking_task_body_id
      LEFT JOIN picking_task_handling_units hu ON hu.picking_task_handling_unit_id = pd.picking_task_handling_unit_id
      WHERE pt.picking_task_id = :task_id
    """.trimIndent()

        val rows = jdbc.query(sql, mapOf("task_id" to taskId)) { rs, _ -> rs }
        if (rows.isEmpty()) return null

        // Превращаем ResultSet rows -> нормальные модели
        // (простая реализация: читаем rs как снимок через helper)
        return extract(rows.map { itToMap(it) })
    }

    private fun itToMap(rs: ResultSet): Map<String, Any?> {
        val md = rs.metaData
        val map = LinkedHashMap<String, Any?>()
        for (i in 1..md.columnCount) {
            map[md.getColumnLabel(i)] = rs.getObject(i)
        }
        return map
    }

    private fun extract(rows: List<Map<String, Any?>>): TaskDetailsResult {
        val first = rows.first()

        fun uuid(key: String): UUID = UUID.fromString(first[key].toString())
        fun uuidN(key: String): UUID? = first[key]?.toString()?.let(UUID::fromString)
        fun str(key: String): String = first[key].toString()
        fun instantN(key: String): Instant? = (first[key] as? java.sql.Timestamp)?.toInstant()
        fun instant(key: String): Instant = instantN(key)!!

        val taskId = UUID.fromString(first["picking_task_id"].toString())

        val unpicked = mutableListOf<TaskBodyDetails>()
        val huMap = LinkedHashMap<UUID, MutableList<Pair<TaskBodyDetails, HandlingUnitBodyDetails>>>()
        val huMeta =
            LinkedHashMap<UUID, Triple<UUID?, Pair<HandlingUnitStatus, String>, Instant>>() // targetCellId, (status, barcode), createdAt

        rows.forEach { r ->
            val body = TaskBodyDetails(
                bodyId = UUID.fromString(r["ptb_id"].toString()),
                goodsId = UUID.fromString(r["goods_id"].toString()),
                shipmentNumber = r["shipment_number"].toString(),
                shipmentId = UUID.fromString(r["shipment_id"].toString()),
                pickingCellId = UUID.fromString(r["picking_cell_id"].toString()),
                quantity = (r["quantity"] as Number).toInt(),
                cancelledQuantity = (r["cancelled_quantity"] as Number).toInt(),
                shortageReason = r["shortage_reason"]?.toString()?.let { GoodsShortageReason.valueOf(it) },
                updatedAt = (r["ptb_updated_at"] as? Timestamp)?.toInstant(),
                handlingUnitBarcode = r["handling_unit_barcode"]?.toString(),
                stockQuality = StockQuality.valueOf(r["stock_quality"].toString()),
                isNlo = (r["is_nlo"] as Boolean)
            )

            val huIdRaw = r["hu_id"]?.toString()
            val pickedQty = (r["picked_quantity"] as? Number)?.toInt()
            val lastPicked = (r["last_picked"] as? java.sql.Timestamp)?.toInstant()

            if (huIdRaw == null || pickedQty == null || lastPicked == null) {
                // значит это "неупакованный" товар
                unpicked.add(body)
            } else {
                val huId = UUID.fromString(huIdRaw)
                val hud = HandlingUnitBodyDetails(
                    pickedQuantity = pickedQty, lastPicked = lastPicked
                )
                huMap.computeIfAbsent(huId) { mutableListOf() }.add(body to hud)

                val target = r["hu_target_cell_id"]?.toString()?.let(UUID::fromString)
                val st = HandlingUnitStatus.valueOf(r["hu_status"].toString())
                val bc = r["hu_barcode"].toString()
                val createdAt = (r["hu_created_at"] as java.sql.Timestamp).toInstant()
                huMeta[huId] = Triple(target, st to bc, createdAt)
            }
        }

        val handlingUnits = huMap.mapValues { (huId, pairs) ->
            val meta = huMeta.getValue(huId)
            HandlingUnitDetails(
                huId = huId,
                targetCellId = meta.first,
                status = meta.second.first,
                barcode = meta.second.second,
                createdAt = meta.third,
                taskBodies = pairs
            )
        }

        val buffers = (first["buffers"] as? java.sql.Array)?.array?.let { it as Array<*> }?.map { it.toString() }

        return TaskDetailsResult(
            taskId = taskId,
            taskNumber = str("task_number"),
            status = PickingTaskStatus.valueOf(str("status")),
            performerId = uuidN("performer_id"),
            warehouseId = UUID.fromString(first["warehouse_id"].toString()),
            priority = PickingTaskPriority.valueOf(str("priority")),
            pickingDeadline = instant("picking_deadline_date"),
            startedAt = instantN("started_at"),
            pickingZoneId = UUID.fromString(first["picking_zone_id"].toString()),
            buffers = buffers,
            creatorId = UUID.fromString(first["creator_id"].toString()),
            createdAt = instant("created_at"),
            sortMethodId = UUID.fromString(first["sort_method_id"].toString()),
            completedAt = instantN("completed_at"),
            subMode = first["sub_mode"]?.toString()?.let { PickingSubMode.valueOf(it) },

            unpickedBodies = unpicked.distinctBy { it.bodyId },
            handlingUnits = handlingUnits
        )
    }
}