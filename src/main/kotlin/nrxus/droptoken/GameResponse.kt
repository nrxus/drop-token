package nrxus.droptoken

import com.fasterxml.jackson.annotation.JsonUnwrapped

class GameResponse(
        val players: List<String>,
        @field:JsonUnwrapped
        val state: State
) {
    sealed class State {
        class InProgress : State() {
            val state = "IN_PROGRESS"
        }

        class Done(val winner: String?) : State() {
            val state = "DONE"
        }
    }
}