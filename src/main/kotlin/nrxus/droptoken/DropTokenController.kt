package nrxus.droptoken

import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/drop_token")
class DropTokenController(private val repository: DropTokenRepository) {
    @GetMapping
    fun getGames(): GamesResponse =
            GamesResponse(repository.findAll().map { it.id.toString() })

    @PostMapping
    fun newGame(@Valid @RequestBody request: NewGameRequest): GameResponse {
        val dropToken = repository.save(DropToken())
        return GameResponse(dropToken.id.toString())
    }

    class GamesResponse(val games: List<String>)
    class GameResponse(val gameId: String)
}