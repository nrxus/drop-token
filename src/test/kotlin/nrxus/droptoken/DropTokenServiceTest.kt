package nrxus.droptoken

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import nrxus.droptoken.persistence.DropToken
import nrxus.droptoken.persistence.DropTokenRepository
import nrxus.droptoken.persistence.Move
import nrxus.droptoken.persistence.MoveRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.data.repository.findByIdOrNull

internal class DropTokenServiceTest {
    private lateinit var dropTokenRepository: DropTokenRepository
    private lateinit var moveRepository: MoveRepository
    private lateinit var subject: DropTokenService


    @BeforeEach
    fun setup() {
        dropTokenRepository = mockk()
        moveRepository = mockk()
        subject = DropTokenService(dropTokenRepository, moveRepository)
    }

    @Test
    fun `allIds returns the id of all the games`() {
        val dropTokens = listOf(dropToken(id = 3), dropToken(id = 5))
        every { dropTokenRepository.findAll() } returns dropTokens

        val ids = subject.allIds()

        assertThat(ids).containsExactlyElementsOf(dropTokens.map { it.id })
    }

    @Test
    fun `create saves a new game and returns its id`() {
        val dropToken = dropToken()
        every { dropTokenRepository.save(any<DropToken>()) } returns dropToken

        val players = listOf("player1", "player2")
        val id = subject.create(players)

        assertThat(id).isEqualTo(dropToken.id)
        verify(exactly = 1) {
            dropTokenRepository.save<DropToken>(match {
                it.currentPlayers == players &&
                        it.originalPlayers == players &&
                        it.state == DropToken.State.IN_PROGRESS &&
                        it.tokens == listOf<Int>() &&
                        it.turn == 0 &&
                        it.moves.isEmpty()
            })
        }
    }

    @Test
    fun `get returns the state of a game in progress`() {
        every { dropTokenRepository.findByIdOrNull(3) } returns dropToken(
                original_players = listOf("alice", "bob"),
                state = DropToken.State.IN_PROGRESS
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
        every { dropTokenRepository.findByIdOrNull(5) } returns dropToken(
                original_players = listOf("dan", "charlie"),
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
        every { dropTokenRepository.findByIdOrNull(10) } returns dropToken(
                original_players = listOf("bob", "alice"),
                current_players = mutableListOf("bob"),
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
        every { dropTokenRepository.findByIdOrNull(any()) } returns null

        val gameState = subject.get(10)

        assertThat(gameState).isNull()
    }

    @Test
    fun `move adds a valid move to the game`() {
        val dropToken = dropToken(
                current_players = mutableListOf("dan", "charlie"),
                turn = 1,
                tokens = mutableListOf(0),
                moves = mutableListOf()
        )

        dropToken.moves.add(Move(Move.MoveType.MOVE, "dan", 0, 0, dropToken))

        every { dropTokenRepository.findByIdOrNull(2) } returns dropToken
        every { dropTokenRepository.save(any<DropToken>()) } returns dropToken

        when (val result = subject.newMove(2, "charlie", 2)) {
            is MoveResult.Success -> assertThat(result.moveNumber).isEqualTo(1)
            else -> fail("expected a MoveResult.OutOfTurn")
        }

        verify(exactly = 1) {
            dropTokenRepository.save<DropToken>(match {
                it.turn == 0
                        && it.tokens == listOf(0, -1, 1) &&
                        it.moves.size == 2 &&
                        it.moves[1].type == Move.MoveType.MOVE &&
                        it.moves[1].player == "charlie" &&
                        it.moves[1].column == 2 &&
                        it.moves[1].number == 1 &&
                        it.state == DropToken.State.IN_PROGRESS
            })
        }
    }

    @Test
    fun `move might save a win`() {
        val dropToken = dropToken(
                current_players = mutableListOf("alice", "dan"),
                turn = 0,
                tokens = mutableListOf(
                        0, 1, 0, 1,
                        1, 0, 0, 1,
                        -1, -1, 0, 1,
                        -1, -1, -1
                )
        )
        every { dropTokenRepository.findByIdOrNull(2) } returns dropToken
        every { dropTokenRepository.save(any<DropToken>()) } returns dropToken

        when (subject.newMove(2, "alice", 3)) {
            is MoveResult.Success -> Unit
            else -> fail("expected a MoveResult.Success")
        }

        verify(exactly = 1) {
            dropTokenRepository.save<DropToken>(match {
                it.state == DropToken.State.DONE && it.winner == "alice"
            })
        }
    }

    @Test
    fun `move might save a tie`() {
        val dropToken = dropToken(
                current_players = mutableListOf("alice", "dan"),
                turn = 1,
                tokens = mutableListOf(
                        0, 1, 0, 1,
                        1, 0, 0, 1,
                        0, 1, 1, 1,
                        -1, 0, 0, 0
                )
        )
        every { dropTokenRepository.findByIdOrNull(2) } returns dropToken
        every { dropTokenRepository.save(any<DropToken>()) } returns dropToken

        when (subject.newMove(2, "dan", 0)) {
            is MoveResult.Success -> Unit
            else -> fail("expected a MoveResult.Success")
        }

        verify(exactly = 1) {
            dropTokenRepository.save<DropToken>(match {
                it.state == DropToken.State.DONE && it.winner == null
            })
        }
    }

    @Test
    fun `move is invalid if game is done`() {
        every { dropTokenRepository.findByIdOrNull(2) } returns dropToken(
                current_players = mutableListOf("bob", "charlie"),
                turn = 0,
                state = DropToken.State.DONE
        )

        // columns already full
        when (subject.newMove(2, "bob", 0)) {
            is MoveResult.IllegalMove -> Unit
            else -> fail("expected a MoveResult.IllegalMove")
        }

        verify(exactly = 0) { dropTokenRepository.save(any<DropToken>()) }
    }

    @Test
    fun `move checks invalid moves`() {
        every { dropTokenRepository.findByIdOrNull(2) } returns dropToken(
                current_players = mutableListOf("bob", "charlie"),
                turn = 0,
                tokens = mutableListOf(0, -1, -1, -1,
                        1, -1, -1, -1,
                        0, -1, -1, -1,
                        1)
        )

        // column number too high
        when (subject.newMove(2, "bob", 4)) {
            is MoveResult.IllegalMove -> Unit
            else -> fail("expected a MoveResult.IllegalMove")
        }

        // column number too low
        when (subject.newMove(2, "bob", -1)) {
            is MoveResult.IllegalMove -> Unit
            else -> fail("expected a MoveResult.IllegalMove")
        }

        // columns already full
        when (subject.newMove(2, "bob", 0)) {
            is MoveResult.IllegalMove -> Unit
            else -> fail("expected a MoveResult.IllegalMove")
        }

        verify(exactly = 0) { dropTokenRepository.save(any<DropToken>()) }
    }

    @Test
    fun `move does not allow for out of order turns`() {
        every { dropTokenRepository.findByIdOrNull(10) } returns dropToken(
                current_players = mutableListOf("alice", "charlie"),
                turn = 1
        )

        when (subject.newMove(10, "alice", 3)) {
            is MoveResult.OutOfTurn -> Unit
            else -> fail("expected a MoveResult.OutOfTurn")
        }

        verify(exactly = 0) { dropTokenRepository.save(any<DropToken>()) }
    }

    @Test
    fun `move checks the game exists`() {
        every { dropTokenRepository.findByIdOrNull(5) } returns null

        when (subject.newMove(5, "bob", 3)) {
            is MoveResult.None -> Unit
            else -> fail("expected a MoveResult.None")
        }

        verify(exactly = 0) { dropTokenRepository.save(any<DropToken>()) }
    }

    @Test
    fun `move checks the player exists in that game`() {
        every { dropTokenRepository.findByIdOrNull(5) } returns dropToken(
                current_players = mutableListOf("alice", "charlie")
        )

        when (subject.newMove(5, "bob", 3)) {
            is MoveResult.None -> Unit
            else -> fail("expected a MoveResult.None")
        }

        verify(exactly = 0) { dropTokenRepository.save(any<DropToken>()) }
    }

    @Test
    fun `getMove converts move type`() {
        every { moveRepository.findByNumberAndDropTokenId(3, 6) } returns Move(
                type = Move.MoveType.MOVE,
                player = "alice",
                column = 3,
                number = 3,
                dropToken = DropToken.new(listOf("alice", "bob"))
        )

        val move = subject.getMove(6, 3)

        assertThat(move?.player).isEqualTo("alice")

        when (val type = move?.type) {
            is nrxus.droptoken.Move.Type.Move -> assertThat(type.column)
            else -> fail("expected a Move type")
        }
    }

    @Test
    fun `getMove converts quit type`() {
        every { moveRepository.findByNumberAndDropTokenId(6, 2) } returns Move(
                type = Move.MoveType.QUIT,
                player = "bob",
                number = 2,
                column = null,
                dropToken = DropToken.new(listOf("alice", "bob"))
        )

        val move = subject.getMove(2, 6)

        assertThat(move?.player).isEqualTo("bob")

        when (move?.type) {
            is nrxus.droptoken.Move.Type.Quit -> Unit
            else -> fail("expected a Quit type")
        }
    }

    @Test
    fun `move returns null not found`() {
        every { moveRepository.findByNumberAndDropTokenId(3, 6) } returns null
        assertThat(subject.getMove(6, 3)).isNull()
    }

    // creates a default DropToken for testing
    // allows for field overriding
    private fun dropToken(
            id: Long? = null,
            original_players: List<String>? = null,
            current_players: MutableList<String>? = null,
            state: DropToken.State? = null,
            winner: String? = null,
            turn: Int? = null,
            tokens: MutableList<Int>? = null,
            moves: MutableList<Move>? = null
    ) = DropToken(
            id = id ?: 3,
            originalPlayers = original_players ?: current_players?.toList() ?: listOf("a", "b"),
            currentPlayers = current_players ?: original_players?.toMutableList() ?: mutableListOf("a", "b"),
            state = state ?: DropToken.State.IN_PROGRESS,
            winner = winner,
            turn = turn ?: 0,
            tokens = tokens ?: mutableListOf(),
            moves = moves ?: mutableListOf()
    )
}