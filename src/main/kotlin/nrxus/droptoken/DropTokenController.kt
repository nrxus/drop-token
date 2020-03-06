package nrxus.droptoken

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DropTokenController(private val repository: DropTokenRepository) {
    @GetMapping("/drop_token")
    fun getGames(): GamesResponse = GamesResponse(repository.findAll().map { it.id.toString() })

    @PostMapping("/drop_token")
    fun newGame(): GameResponse = GameResponse(repository.save(DropToken()).id.toString())

    class GamesResponse(val games: List<String>)
    class GameResponse(val gameId: String)
}