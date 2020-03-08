package nrxus.droptoken

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest
internal class DropTokenControllerTest(@Autowired val mockMvc: MockMvc) {
    @MockkBean
    private lateinit var repository: DropTokenRepository

    @Test
    fun `GET #drop_token`() {
        val dropTokens = listOf(
                DropToken(id = 3, player1 = "a", player2 = "b", state = DropToken.State.IN_PROGRESS),
                DropToken(id = 5, player1 = "c", player2 = "d", state = DropToken.State.DONE)
        )

        every {
            repository.findAll()
        } returns dropTokens

        val response = mockMvc.perform(get("/drop_token")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .response
                .contentAsString

        val actual = """{
            |games: ["${dropTokens[0].id}", "${dropTokens[1].id}"]
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


        val dropToken = DropToken(
                id = 2, player1 = "player1", player2 = "player2", state = DropToken.State.IN_PROGRESS
        )
        every {
            repository.save(any<DropToken>())
        } returns dropToken

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
            |"gameId": "${dropToken.id}"
            |}""".trimMargin()

        JSONAssert.assertEquals(expected, response, JSONCompareMode.NON_EXTENSIBLE)
        verify(exactly = 1) {
            repository.save<DropToken>(match {
                it.player1 == "player1" && it.player2 == "player2" && it.state == DropToken.State.IN_PROGRESS
            })
        }
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

        verify(exactly = 0) { repository.save<DropToken>(any()) }
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

        verify(exactly = 0) { repository.save<DropToken>(any()) }
    }

    @Test
    fun `GET #drop_token#{gameId}`() {
        val dropToken = DropToken(
                id = 3,
                player1 = "alice",
                player2 = "bob",
                state = DropToken.State.DONE,
                winner = "bob"
        )
        every {
            repository.findByIdOrNull(3)
        } returns dropToken

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
            |"winner": "bob"
            |}""".trimMargin()

        JSONAssert.assertEquals(actual, response, JSONCompareMode.NON_EXTENSIBLE)
    }

    @Test
    fun `GET #drop_token#{gameId} not found`() {
        every {
            repository.findByIdOrNull(any())
        } returns null

        mockMvc.perform(get("/drop_token/2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound)
    }
}