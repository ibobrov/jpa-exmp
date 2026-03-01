package i.bobrov.jpa_exmp.controller

import i.bobrov.jpa_exmp.IntegrationTestBase
import org.hamcrest.Matchers.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerIT : IntegrationTestBase() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `GET api customers returns seeded customers`() {
        mockMvc.get("/api/customers")
            .andExpect {
                status { isOk() }
                content { contentTypeCompatibleWith("application/json") }
                jsonPath("$", hasSize<Any>(greaterThanOrEqualTo(2)))
                jsonPath("$[*].email", hasItems("alice@example.com", "bob@example.com"))
            }
    }

    @Test
    fun `GET api customers by id returns one customer`() {
        // Берём id первого сидового клиента через /api/customers
        val result = mockMvc.get("/api/customers")
            .andExpect { status { isOk() } }
            .andReturn()

        // Очень простая проверка без парсинга JSON библиотеками:
        // лучше, конечно, распарсить Jackson'ом, но так тоже ок для мини-примера.
        val body = result.response.contentAsString
        // найдём первое "id": число
        val id = Regex("\"id\"\\s*:\\s*(\\d+)").find(body)?.groupValues?.get(1)?.toLong()
            ?: error("Could not extract id from response: $body")

        mockMvc.get("/api/customers/$id")
            .andExpect {
                status { isOk() }
                jsonPath("$.id", `is`(id.toInt()))
                jsonPath("$.email", not(emptyOrNullString()))
                jsonPath("$.fullName", not(emptyOrNullString()))
            }
    }
}