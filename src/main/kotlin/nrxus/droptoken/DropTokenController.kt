package nrxus.droptoken

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/drop_token")
class DropTokenController(private val repository: DropTokenRepository) {
    @GetMapping
    fun getGames(): AllGamesResponse =
            AllGamesResponse(repository.findAll().map { it.id.toString() })

    @PostMapping
    fun newGame(@Valid @RequestBody request: NewGameRequest): NewGameResponse {
        val dropToken = repository.save(DropToken(
                player1 = request.players[0],
                player2 = request.players[1],
                state = DropToken.State.IN_PROGRESS
        ))
        return NewGameResponse(dropToken.id.toString())
    }

    @GetMapping("/{id}")
    fun getGame(@PathVariable id: Long): ResponseEntity<GameResponse> =
            repository.findByIdOrNull(id)?.let {
                ResponseEntity.ok(GameResponse(
                        players = listOf(it.player1, it.player2),
                        state = when (it.state) {
                            DropToken.State.DONE -> GameResponse.State.Done(it.winner)
                            DropToken.State.IN_PROGRESS -> GameResponse.State.InProgress()
                        }
                ))
            } ?: ResponseEntity.notFound().build()

    // Responses

    class AllGamesResponse(val games: List<String>)
    class NewGameResponse(val gameId: String)
}