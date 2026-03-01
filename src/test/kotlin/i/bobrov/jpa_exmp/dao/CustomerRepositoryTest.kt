package i.bobrov.jpa_exmp.dao

import i.bobrov.jpa_exmp.IntegrationTestBase
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@DataJpaTest
@Import() // можно оставить пустым; важно, что поднимется контекст JPA
class CustomerRepositoryTest : IntegrationTestBase() {

    @Autowired
    lateinit var repo: CustomerRepository

    @Test
    fun `data sql should seed customers`() {
        val all = repo.findAll()
        assertThat(all).isNotEmpty
        assertThat(all.map { it.email }).contains("alice@example.com", "bob@example.com")
    }

    @Test
    fun `findByEmail should return customer`() {
        val alice = repo.findByEmail("alice@example.com")
        assertThat(alice).isNotNull
        assertThat(alice!!.fullName).isEqualTo("Alice Johnson")
    }
}