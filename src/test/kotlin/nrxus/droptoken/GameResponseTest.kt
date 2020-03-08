package nrxus.droptoken

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

internal class GameResponseTest {
    @Test
    fun `in progress`() {
        val response = GameResponse(
                players = listOf("bob", "alice"),
                state = GameResponse.State.InProgress()
        )

        val serialized = ObjectMapper().writeValueAsString(response)
        val expected = """{
            |"players": ["bob", "alice"],
            |"state": "IN_PROGRESS"
            |}""".trimMargin()

        JSONAssert.assertEquals(serialized, expected, JSONCompareMode.NON_EXTENSIBLE)
    }

    @Test
    fun `done without a winner`() {
        val response = GameResponse(
                players = listOf("bob", "alice"),
                state = GameResponse.State.Done(winner = null)
        )

        val serialized = ObjectMapper().writeValueAsString(response)
        val expected = """{
            |"players": ["bob", "alice"],
            |"state": "DONE",
            |"winner": null
            |}""".trimMargin()

        JSONAssert.assertEquals(serialized, expected, JSONCompareMode.NON_EXTENSIBLE)
    }

    @Test
    fun `done with a winner`() {
        val response = GameResponse(
                players = listOf("bob", "alice"),
                state = GameResponse.State.Done(winner = "alice")
        )

        val serialized = ObjectMapper().writeValueAsString(response)
        val expected = """{
            |"players": ["bob", "alice"],
            |"state": "DONE",
            |"winner": "alice"
            |}""".trimMargin()

        JSONAssert.assertEquals(serialized, expected, JSONCompareMode.NON_EXTENSIBLE)
    }
}