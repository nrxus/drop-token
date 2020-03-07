package nrxus.droptoken

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
class DropTokenController(private val repository: DropTokenRepository) {
    @GetMapping("/drop_token")
    fun getGames(): GamesResponse =
            GamesResponse(repository.findAll().map { it.id.toString() })

    @PostMapping("/drop_token")
    fun newGame(@Valid @RequestBody request: NewGameRequest): GameResponse {
        val dropToken = repository.save(DropToken())
        return GameResponse(dropToken.id.toString())
    }

    class GamesResponse(val games: List<String>)
    class GameResponse(val gameId: String)
}