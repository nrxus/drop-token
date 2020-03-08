package nrxus.droptoken

import com.ninjasquad.springmockk.MockkBean
import io.mockk.Called
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest
internal class DropTokenControllerTest(@Autowired val mockMvc: MockMvc) {
    @MockkBean
    private lateinit var service: DropTokenService

    @Test
    fun `GET #drop_token`() {
        val ids = listOf(2L, 4L)
        every { service.allIds() } returns ids

        val response = mockMvc.perform(get("/drop_token")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .response
                .contentAsString

        val actual = """{
            |games: ["${ids[0]}", "${ids[1]}"]
            |}""".trimMargin()

        JSONAssert.assertEquals(actual, response, JSONCompareMode.NON_EXTENSIBLE)
    }

    @Test
    fun `POST #drop_token`() {
        val body = """{
            |"players": ["player1", "player2"],
            |"columns": 4,
            |"rows":4
            |}""".trimMargin()


        val id = 3L
        every { service.create(any(), any()) } returns id

        val response = mockMvc
                .perform(
                        post("/drop_token")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .response
                .contentAsString

        val expected = """{
            |"gameId": "$id"
            |}""".trimMargin()

        JSONAssert.assertEquals(expected, response, JSONCompareMode.NON_EXTENSIBLE)
        verify(exactly = 1) { service.create("player1", "player2") }
    }

    @Test
    fun `POST #drop_token fields with invalid data`() {
        val body = """{
            |"players": ["player1"],
            |"columns": 4,
            |"rows":4
            |}""".trimMargin()

        mockMvc
                .perform(
                        post("/drop_token")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest)

        verify { service wasNot Called }
    }

    @Test
    fun `POST #drop_token missing fields`() {
        val body = """{
            |"players": ["player1", "player2"]
            |}""".trimMargin()

        mockMvc
                .perform(
                        post("/drop_token")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest)

        verify { service wasNot Called }
    }

    @Test
    fun `GET #drop_token#{gameId}`() {
        every { service.get(3) } returns GameState(
                players = listOf("alice", "bob"),
                state = GameState.State.Done(winner = "alice")
        )

        val response = mockMvc.perform(get("/drop_token/3")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .response
                .contentAsString

        val actual = """{
            |"players": ["alice", "bob"],
            |"state": "DONE",
            |"winner": "alice"
            |}""".trimMargin()

        JSONAssert.assertEquals(actual, response, JSONCompareMode.NON_EXTENSIBLE)
    }

    @Test
    fun `GET #drop_token#{gameId} not found`() {
        every { service.get(any()) } returns null

        mockMvc.perform(get("/drop_token/2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound)
    }
}