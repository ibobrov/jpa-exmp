package i.bobrov.jpa_exmp.kotest

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SimpleMathTest : StringSpec({

    "2 + 2 should equal 4" {
        2 + 2 shouldBe 4
    }

    "string length should work" {
        "kotlin".length shouldBe 6
    }
})