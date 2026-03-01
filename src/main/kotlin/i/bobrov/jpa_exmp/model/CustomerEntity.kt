package i.bobrov.jpa_exmp.model

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "customers")
class CustomerEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(name = "full_name", nullable = false)
    var fullName: String,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()
)