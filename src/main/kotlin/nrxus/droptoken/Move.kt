package nrxus.droptoken

import com.fasterxml.jackson.annotation.JsonUnwrapped

class Move(
        @field:JsonUnwrapped
        val type: Type,
        val player: String
) {
    sealed class Type {
        class Move(val column: Int) : Type() {
            val type = "MOVE"
        }

        class Quit : Type() {
            val type = "QUIT"
        }
    }

    companion object {
        fun fromEntity(entity: nrxus.droptoken.persistence.Move) = Move(
                player = entity.player,
                type = when (entity.type) {
                    nrxus.droptoken.persistence.Move.MoveType.QUIT -> Move.Type.Quit()
                    nrxus.droptoken.persistence.Move.MoveType.MOVE -> Move.Type.Move(entity.column!!)
                }
        )
    }
}