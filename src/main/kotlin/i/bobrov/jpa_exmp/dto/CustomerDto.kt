package i.bobrov.jpa_exmp.dto

import i.bobrov.jpa_exmp.model.CustomerEntity
import java.time.OffsetDateTime

data class CustomerDto(
    val id: Long,
    val email: String,
    val fullName: String,
    val createdAt: OffsetDateTime,
)

fun CustomerEntity.toDto() = CustomerDto(
    id = requireNotNull(id),
    email = email,
    fullName = fullName,
    createdAt = createdAt
)