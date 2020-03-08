package nrxus.droptoken

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class DropTokenService(private val repository: DropTokenRepository) {
    fun allIds(): List<Long> = repository.findAll().map { it.id }

    fun create(player1: String, player2: String): Long = repository.save(DropToken(
            player1 = player1,
            player2 = player2,
            state = DropToken.State.IN_PROGRESS
    )).id

    fun get(id: Long): GameState? = repository.findByIdOrNull(id)?.let {
        GameState(
                players = listOf(it.player1, it.player2),
                state = when (it.state) {
                    DropToken.State.DONE -> GameState.State.Done(it.winner)
                    DropToken.State.IN_PROGRESS -> GameState.State.InProgress()
                }
        )
    }
}