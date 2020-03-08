package nrxus.droptoken

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/drop_token")
class DropTokenController(private val service: DropTokenService) {
    @GetMapping
    fun getGames() = AllGamesResponse(
            service.allIds().map { it.toString() }
    )

    @PostMapping
    fun newGame(@Valid @RequestBody request: NewGameRequest) = NewGameResponse(
            service.create(
                    request.players[0],
                    request.players[1]
            ).toString()
    )

    @GetMapping("/{id}")
    fun getGame(@PathVariable id: Long) = service.get(id)?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    // Responses
    class AllGamesResponse(val games: List<String>)

    class NewGameResponse(val gameId: String)
}