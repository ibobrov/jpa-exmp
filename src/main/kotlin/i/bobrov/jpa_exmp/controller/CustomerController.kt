package i.bobrov.jpa_exmp.controller

import i.bobrov.jpa_exmp.dto.CustomerDto
import i.bobrov.jpa_exmp.service.CustomerService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Customers")
@RestController
@RequestMapping("/api/customers")
class CustomerController(
    private val customerService: CustomerService
) {
    @Operation(summary = "Get all customers")
    @GetMapping
    fun getAll(): List<CustomerDto> =
        customerService.getAll()

    @Operation(summary = "Get customer by id")
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): CustomerDto =
        customerService.getById(id)
}