package i.bobrov.jpa_exmp.common

import i.bobrov.jpa_exmp.dto.UNKNOWN_STUB
import i.bobrov.jpa_exmp.dto.User
import i.bobrov.jpa_exmp.tasks.CellDto

import java.util.UUID
import org.springframework.stereotype.Component

data class ZoneDto(val id: UUID, val code: String)
data class SortMethodDto(val id: UUID, val name: String)
data class CargoUnitData(val cargoUnitNumber: String)

interface VirtualTopologyServiceClient {
    fun getCellsByIds(warehouseId: UUID, ids: Set<UUID>): Map<UUID, CellDto>
    fun getZoneByIds(ids: Set<UUID>): Map<UUID, ZoneDto>
}

interface ShipmentsService {
    fun getSortMethodByIds(warehouseId: UUID, ids: Set<UUID>): Map<UUID, SortMethodDto>
}

interface UserProfileService {
    fun getProfilesByIds(ids: List<UUID>): Map<UUID, User>
}

interface PackingServiceClient {
    fun getByHandlingUnitsCached(huIds: Set<UUID>): Map<UUID, List<CargoUnitData>>
}

interface CellsCacheById {
    fun getAll(ids: Set<UUID>): Map<UUID, CellDto>
    fun putAll(values: Map<UUID, CellDto>)
}

interface WarehouseTopologyGateway {
    fun fetchCells(warehouseId: UUID, cellIds: Set<UUID>): Map<UUID, CellDto>
}

@Component
class InMemoryCellsCacheById : CellsCacheById {
    private val map = java.util.concurrent.ConcurrentHashMap<UUID, CellDto>()

    override fun getAll(ids: Set<UUID>): Map<UUID, CellDto> =
        ids.mapNotNull { id -> map[id]?.let { id to it } }.toMap()

    override fun putAll(values: Map<UUID, CellDto>) {
        map.putAll(values)
    }
}

@Component
class WarehouseTopologyGatewayStub : WarehouseTopologyGateway {
    override fun fetchCells(warehouseId: UUID, cellIds: Set<UUID>): Map<UUID, CellDto> =
        cellIds.associateWith { id -> CellDto(id, "CELL-${id.toString().take(6)}") }
}

@Component
class VirtualTopologyServiceClientStub(
    private val cellsCacheById: CellsCacheById,
    private val warehouseTopologyGateway: WarehouseTopologyGateway,
) : VirtualTopologyServiceClient {


    override fun getCellsByIds(warehouseId: UUID, cellIds: Set<UUID>): Map<UUID, CellDto> {
        val cachedCells = cellsCacheById.getAll(cellIds)
        val missedCellIds = cellIds - cachedCells.keys

        return if (missedCellIds.isEmpty()) {
            cachedCells
        } else {
            // В проде тут был бы Contextualized + feign + otherwise
            val missedCells = warehouseTopologyGateway.fetchCells(warehouseId, missedCellIds)

            cellsCacheById.putAll(missedCells)

            cachedCells + missedCells
        }
    }

    override fun getZoneByIds(ids: Set<UUID>): Map<UUID, ZoneDto> =
        ids.associateWith { ZoneDto(it, "ZONE-${it.toString().take(4)}") }
}

@Component
class ShipmentsServiceStub : ShipmentsService {
    override fun getSortMethodByIds(warehouseId: UUID, ids: Set<UUID>): Map<UUID, SortMethodDto> =
        ids.associateWith { SortMethodDto(it, "Sort-${it.toString().take(4)}") }
}

@Component
class UserProfileServiceStub : UserProfileService {
    override fun getProfilesByIds(ids: List<UUID>): Map<UUID, User> =
        ids.associateWith { User(phone = UNKNOWN_STUB, fullName = "User-${it.toString().take(6)}") }
}

@Component
class PackingServiceClientStub : PackingServiceClient {
    override fun getByHandlingUnitsCached(huIds: Set<UUID>): Map<UUID, List<CargoUnitData>> =
        huIds.associateWith { listOf(CargoUnitData(cargoUnitNumber = "CU-${it.toString().take(6)}")) }
}