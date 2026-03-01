package i.bobrov.jpa_exmp.service

import i.bobrov.jpa_exmp.dto.CustomerDto
import i.bobrov.jpa_exmp.dto.toDto
import i.bobrov.jpa_exmp.dao.CustomerRepository
import org.springframework.stereotype.Service

@Service
class CustomerService(
    private val customerRepository: CustomerRepository
) {

    fun getAll(): List<CustomerDto> =
        customerRepository.findAll().map { it.toDto() }

    fun getById(id: Long): CustomerDto =
        customerRepository.findById(id)
            .orElseThrow { NoSuchElementException("Customer $id not found") }
            .toDto()

    fun getByEmail(email: String): CustomerDto =
        customerRepository.findByEmail(email)
            ?.toDto()
            ?: throw NoSuchElementException("Customer with email=$email not found")
}