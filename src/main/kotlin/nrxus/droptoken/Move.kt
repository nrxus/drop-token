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
}