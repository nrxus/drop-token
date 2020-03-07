package nrxus.droptoken

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory


internal class NewGameRequestTest {
    companion object {
        private lateinit var validatorFactory: ValidatorFactory
        private lateinit var validator: Validator

        @BeforeAll
        @JvmStatic
        fun setup() {
            validatorFactory = Validation.buildDefaultValidatorFactory()
            validator = validatorFactory.validator
        }

        @AfterAll
        @JvmStatic
        fun close() {
            validatorFactory.close()
        }
    }

    @Test
    fun `valid request`() {
        val request = NewGameRequest(4, 4, players = listOf("bob", "alice"))
        val violations = validator.validate(request)
        assertThat(violations).isEmpty()
    }

    @Test
    fun `invalid for less than two players`() {
        val request = NewGameRequest(4, 4, players = listOf("bob"))
        val violations = validator.validate(request)
        assertThat(violations).anyMatch { it.propertyPath.toString() == "players" }
    }

    @Test
    fun `invalid for more than two players`() {
        val request = NewGameRequest(4, 4, players = listOf("bob", "alice", "charlie"))
        val violations = validator.validate(request)
        assertThat(violations).anyMatch { it.propertyPath.toString() == "players" }
    }

    @Test
    fun `invalid for less than 4 columns or rows`() {
        val request = NewGameRequest(3, 3, players = listOf("bob", "alice"))
        val violations = validator.validate(request)
        assertThat(violations).anyMatch { it.propertyPath.toString() == "columns" }
        assertThat(violations).anyMatch { it.propertyPath.toString() == "rows" }
    }

    @Test
    fun `invalid for greater than 4 columns or rows`() {
        val request = NewGameRequest(5, 5, players = listOf("bob", "alice"))
        val violations = validator.validate(request)
        assertThat(violations).anyMatch { it.propertyPath.toString() == "columns" }
        assertThat(violations).anyMatch { it.propertyPath.toString() == "rows" }
    }
}