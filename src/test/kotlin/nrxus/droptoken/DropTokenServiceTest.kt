package nrxus.droptoken

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.data.repository.findByIdOrNull

internal class DropTokenServiceTest {
    @Test
    fun `allIds returns the id of all the games`() {
        val repository = mockk<DropTokenRepository>()
        val subject = DropTokenService(repository)

        val dropTokens = listOf(
                DropToken(id = 3, player1 = "a", player2 = "b", state = DropToken.State.IN_PROGRESS),
                DropToken(id = 5, player1 = "c", player2 = "d", state = DropToken.State.DONE)
        )

        every { repository.findAll() } returns dropTokens

        val ids = subject.allIds()

        assertThat(ids).containsExactlyElementsOf(dropTokens.map { it.id })
    }

    @Test
    fun `create saves a new game and returns its id`() {
        val repository = mockk<DropTokenRepository>()
        val subject = DropTokenService(repository)

        val dropToken = DropToken(
                id = 2, player1 = "player1", player2 = "player2", state = DropToken.State.IN_PROGRESS
        )
        every { repository.save(any<DropToken>()) } returns dropToken

        val id = subject.create("player1", "player2")

        assertThat(id).isEqualTo(dropToken.id)
        verify(exactly = 1) {
            repository.save<DropToken>(match {
                it.player1 == "player1" && it.player2 == "player2" && it.state == DropToken.State.IN_PROGRESS
            })
        }
    }

    @Test
    fun `get returns the state of a game in progress`() {
        val repository = mockk<DropTokenRepository>()
        val subject = DropTokenService(repository)

        every { repository.findByIdOrNull(3) } returns DropToken(
                id = 3,
                player1 = "alice",
                player2 = "bob",
                state = DropToken.State.IN_PROGRESS,
                winner = null
        )

        val gameState = subject.get(3)

        assertThat(gameState).isNotNull
        assertThat(gameState?.players).containsExactly("alice", "bob")
        when (gameState?.state) {
            is GameState.State.InProgress -> Unit
            else -> fail("Game State should be InProgress")
        }
    }

    @Test
    fun `get returns the state of a game tied`() {
        val repository = mockk<DropTokenRepository>()
        val subject = DropTokenService(repository)

        every { repository.findByIdOrNull(5) } returns DropToken(
                id = 5,
                player1 = "dan",
                player2 = "charlie",
                state = DropToken.State.DONE,
                winner = null
        )

        val gameState = subject.get(5)

        assertThat(gameState).isNotNull
        assertThat(gameState?.players).containsExactly("dan", "charlie")
        when (val state = gameState?.state) {
            is GameState.State.Done -> assertThat(state.winner).isNull()
            else -> fail("Game State should be Done")
        }
    }

    @Test
    fun `get returns the state of a game with a winner`() {
        val repository = mockk<DropTokenRepository>()
        val subject = DropTokenService(repository)

        every { repository.findByIdOrNull(10) } returns DropToken(
                id = 3,
                player1 = "bob",
                player2 = "alice",
                state = DropToken.State.DONE,
                winner = "bob"
        )

        val gameState = subject.get(10)

        assertThat(gameState).isNotNull
        assertThat(gameState?.players).containsExactly("bob", "alice")
        when (val state = gameState?.state) {
            is GameState.State.Done -> assertThat(state.winner).isEqualTo("bob")
            else -> fail("Game State should be Done")
        }
    }

    @Test
    fun `get returns null for a non existing game`() {
        val repository = mockk<DropTokenRepository>()
        val subject = DropTokenService(repository)

        every { repository.findByIdOrNull(any()) } returns null

        val gameState = subject.get(10)

        assertThat(gameState).isNull()
    }
}