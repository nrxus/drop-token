package nrxus.droptoken

import com.fasterxml.jackson.annotation.JsonUnwrapped
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid
import javax.validation.constraints.Min

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
    ): ResponseEntity<ApiResult<NewMoveResponse>> = when (val result = service.newMove(id, player, request.column)) {
        is MoveResult.Success -> ResponseEntity.ok(
                ApiResult.Success(NewMoveResponse("$id/moves/${result.moveNumber}"))
        )
        is MoveResult.None -> ResponseEntity.notFound().build()
        is MoveResult.IllegalMove -> ResponseEntity.badRequest()
                .body(ApiResult.Failure(
                        ApiError(HttpStatus.BAD_REQUEST, message = "Illegal Move")
                ))
        is MoveResult.OutOfTurn -> ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResult.Failure(
                        ApiError(HttpStatus.CONFLICT, message = "It is not $player's turn")
                ))
    }

    @GetMapping("/{id}/moves/{moveNumber}")
    fun getMove(
            @PathVariable id: Long,
            @PathVariable @Min(0) moveNumber: Int
    ): ResponseEntity<Move> = service.getMove(id, moveNumber)?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @GetMapping("/{id}/moves")
    fun getMoves(
            @PathVariable id: Long,
            @RequestParam start: Int?,
            @RequestParam until: Int?
    ): ResponseEntity<ApiResult<MovesResponse>> {
        val safeStart = start ?: 0
        if (safeStart < 0) {
            return ResponseEntity.badRequest().body(ApiResult.Failure(
                    ApiError(HttpStatus.BAD_REQUEST, message = "start cannot be less than 0")
            ))
        }

        val safeUntil = when (until) {
            null -> null
            else -> if (until < safeStart) {
                return ResponseEntity.badRequest().body(ApiResult.Failure(
                        ApiError(HttpStatus.BAD_REQUEST, message = "until cannot be less than $safeStart")
                ))
            } else {
                until
            }
        }

        val response: ResponseEntity<ApiResult<MovesResponse>>? = service.getMoves(id, safeStart, safeUntil)
                ?.let { ResponseEntity.ok(ApiResult.Success(MovesResponse(it))) }

        return response ?: ResponseEntity.notFound().build()
    }

    @DeleteMapping("/{id}/{player}")
    fun delete(@PathVariable id: Long, @PathVariable player: String): ResponseEntity<Unit> =
            when(service.delete(id, player)) {
                is DeleteResult.NotFound -> ResponseEntity.notFound().build()
                is DeleteResult.AlreadyDone -> ResponseEntity.status(HttpStatus.GONE).build()
                is DeleteResult.Success -> ResponseEntity.accepted().build()
            }

    // Simple Responses

    class AllGamesResponse(val games: List<String>)
    class NewGameResponse(val gameId: String)
    class NewMoveResponse(val move: String)
    class MovesResponse(val moves: List<Move>)

    // Simple Requests

    class MoveRequest(val column: Int)

    sealed class ApiResult<T> {
        class Success<T>(@JsonUnwrapped val success: T) : ApiResult<T>()
        class Failure<T>(@JsonUnwrapped val error: ApiError) : ApiResult<T>()
    }
}