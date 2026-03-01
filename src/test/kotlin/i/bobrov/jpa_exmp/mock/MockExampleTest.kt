package i.bobrov.jpa_exmp.mock

import i.bobrov.jpa_exmp.dao.CustomerRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class MockExampleTest : StringSpec({

    "should mock repository" {
        val repo = mockk<CustomerRepository>()
        every { repo.findAll() } returns emptyList()

        repo.findAll().size shouldBe 0
    }
})