package nrxus.droptoken

import com.ninjasquad.springmockk.MockkBean
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
    private lateinit var repository: DropTokenRepository

    @Test
    fun `GET games`() {
        val dropTokens = listOf(DropToken(3), DropToken(5))
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
    fun `POST game success`() {
        val body = """{
            |"players": ["player1", "player2"],
            |"columns": 4,
            |"rows":4
            |}""".trimMargin()


        val dropToken = DropToken(2)
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
        verify(exactly = 1) { repository.save<DropToken>(any()) }
    }

    @Test
    fun `POST fields with invalid data`() {
        val body = """{
            |"players": ["player1"],
            |"columns": 4,
            |"rows":4
            |}""".trimMargin()


        val dropToken = DropToken(2)
        every {
            repository.save(any<DropToken>())
        } returns dropToken

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
    fun `POST missing fields`() {
        val body = """{
            |"players": ["player1", "player2"]
            |}""".trimMargin()


        val dropToken = DropToken(2)
        every {
            repository.save(any<DropToken>())
        } returns dropToken

        mockMvc
                .perform(
                        post("/drop_token")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest)
                .andReturn().response.contentAsString

        verify(exactly = 0) { repository.save<DropToken>(any()) }
    }
}