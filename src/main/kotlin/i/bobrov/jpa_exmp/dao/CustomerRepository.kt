package i.bobrov.jpa_exmp.dao

import i.bobrov.jpa_exmp.model.CustomerEntity
import org.springframework.data.jpa.repository.JpaRepository


interface CustomerRepository : JpaRepository<CustomerEntity, Long> {

    fun findByEmail(email: String): CustomerEntity?
}