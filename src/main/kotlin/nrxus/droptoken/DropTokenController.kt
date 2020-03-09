package nrxus.droptoken

import com.fasterxml.jackson.annotation.JsonUnwrapped
import org.springframework.http.HttpStatus
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
            service.create(request.players).toString()
    )

    @GetMapping("/{id}")
    fun getGame(@PathVariable id: Long): ResponseEntity<GameState> = service.get(id)?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @PostMapping("/{id}/{player}")
    fun newMove(
            @PathVariable id: Long,
            @PathVariable player: String,
            @RequestBody request: MoveRequest
    ): ResponseEntity<NewMoveResponse> = when (val result = service.newMove(id, player, request.column)) {
        is MoveResult.Success -> ResponseEntity.ok(
                NewMoveResponse.Success("$id/moves/${result.moveNumber}")
        )
        is MoveResult.None -> ResponseEntity.notFound().build()
        is MoveResult.IllegalMove -> ResponseEntity.badRequest()
                .body(NewMoveResponse.Failure(
                        ApiError(HttpStatus.BAD_REQUEST, message = "Illegal Move")
                ))
        is MoveResult.OutOfTurn -> ResponseEntity.status(HttpStatus.CONFLICT)
                .body(NewMoveResponse.Failure(
                        ApiError(HttpStatus.CONFLICT, message = "It is not $player's turn")
                ))
    }

    @GetMapping("/{id}/moves/{moveNumber}")
    fun getMove(
            @PathVariable id: Long,
            @PathVariable moveNumber: Int
    ): ResponseEntity<Move> = service.getMove(id, moveNumber)?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    // Simple Responses

    class AllGamesResponse(val games: List<String>)
    class NewGameResponse(val gameId: String)
    sealed class NewMoveResponse {
        class Success(val move: String) : NewMoveResponse()
        class Failure(@JsonUnwrapped val error: ApiError) : NewMoveResponse()
    }

    // Simple Requests

    class MoveRequest(val column: Int)
}