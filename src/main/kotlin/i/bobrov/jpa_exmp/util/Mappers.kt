package i.bobrov.jpa_exmp.util

import i.bobrov.jpa_exmp.dto.TasksGetDetailsGoods
import i.bobrov.jpa_exmp.tasks.CellDto
import i.bobrov.jpa_exmp.tasks.GoodsBaseInfo
import i.bobrov.jpa_exmp.tasks.TaskBodyDetails

fun TaskBodyDetails.toTasksGetDetailsGoods(
    goodsBaseInfo: GoodsBaseInfo,
    pickedQuantity: Int,
    pickedAt: java.time.Instant?,
    cellDto: CellDto
): TasksGetDetailsGoods =
    TasksGetDetailsGoods(
        goodsId = goodsId,
        shipmentNumber = shipmentNumber,
        shipmentId = shipmentId,
        cellAddress = cellDto.address,
        cellHandlingUnitBarcode = handlingUnitBarcode,
        merchGoodsId = goodsBaseInfo.merchGoodsId,
        goodsName = goodsBaseInfo.goodsName,
        imageUrl = goodsBaseInfo.imageUrl,
        quantity = quantity,
        cancelledQuantity = cancelledQuantity,
        pickedQuantity = pickedQuantity,
        shortageReason = shortageReason,
        barcodes = goodsBaseInfo.barcodes,
        pickedAt = pickedAt,
        stockQuality = stockQuality,
        isNlo = isNlo
    )