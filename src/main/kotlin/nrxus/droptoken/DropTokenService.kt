package nrxus.droptoken

import nrxus.droptoken.persistence.DropToken
import nrxus.droptoken.persistence.DropTokenRepository
import nrxus.droptoken.persistence.MoveRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class DropTokenService(
        private val dropTokenRepository: DropTokenRepository,
        private val moveRepository: MoveRepository
) {
    fun allIds(): List<Long> = dropTokenRepository.findAll().map { it.id }

    fun create(players: List<String>): Long = dropTokenRepository.save(DropToken.new(players)).id

    fun get(id: Long): GameState? = dropTokenRepository.findByIdOrNull(id)?.let {
        GameState(
                players = it.originalPlayers,
                state = when (it.state) {
                    DropToken.State.DONE -> GameState.State.Done(it.winner)
                    DropToken.State.IN_PROGRESS -> GameState.State.InProgress()
                }
        )
    }

    fun newMove(id: Long, player: String, column: Int): MoveResult {
        // out of bounds
        if (column < 0 || column > 3) {
            return MoveResult.IllegalMove
        }

        // check game exists
        val dropToken = when (val dropToken = dropTokenRepository.findByIdOrNull(id)) {
            null -> return MoveResult.None
            else -> dropToken
        }

        // check game is in progress
        if (dropToken.state == DropToken.State.DONE) {
            return MoveResult.IllegalMove
        }

        // check valid player
        if (dropToken.currentPlayers[dropToken.turn] != player) {
            return if (dropToken.currentPlayers.contains(player)) {
                MoveResult.OutOfTurn
            } else {
                MoveResult.None
            }
        }

        // get the original player order in case someone dropped out
        val originalPlayerOrder = dropToken.originalPlayers.indexOf(player)

        val board = SquareBoard(length = 4, tokens = dropToken.tokens)

        if (!board.drop(column, originalPlayerOrder)) {
            return MoveResult.IllegalMove
        }

        val move = nrxus.droptoken.persistence.Move(
                type = nrxus.droptoken.persistence.Move.MoveType.MOVE,
                player = player,
                column = column,
                number = dropToken.moves.size,
                dropToken = dropToken
        )

        dropToken.moves.add(move)
        dropToken.turn = (dropToken.turn + 1) % dropToken.currentPlayers.size

        when (val state = board.state()) {
            is SquareBoard.State.InProgress -> Unit
            is SquareBoard.State.Done -> {
                dropToken.winner = state.winner?.let { dropToken.originalPlayers[it] }
                dropToken.state = DropToken.State.DONE
            }
        }

        dropTokenRepository.save(dropToken)

        return MoveResult.Success(dropToken.moves.size - 1)
    }

    fun getMove(id: Long, move: Int): Move? = moveRepository
            .findByNumberAndDropTokenId(move, id)?.let { Move.fromEntity(it) }

    fun getMoves(id: Long, start: Int, until: Int?): List<Move>? = dropTokenRepository.findByIdOrNull(id)
            ?.let {
                if (start >= it.moves.size) {
                    return null
                }

                val end = until ?: it.moves.size - 1
                if (end >= it.moves.size) {
                    return null
                }

                it.moves.slice(start..end).map { move -> Move.fromEntity(move) }
            }
}

sealed class MoveResult {
    data class Success(val moveNumber: Int) : MoveResult()
    object IllegalMove : MoveResult()
    object OutOfTurn : MoveResult()
    object None : MoveResult()
}