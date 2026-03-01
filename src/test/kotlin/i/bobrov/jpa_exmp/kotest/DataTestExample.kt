package i.bobrov.jpa_exmp.kotest

import io.kotest.core.spec.style.StringSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class DataTestExample : StringSpec({

    withData(
        1 to 2,
        2 to 4,
        3 to 6
    ) { (input, expected) ->
        (input * 2) shouldBe expected
    }
})