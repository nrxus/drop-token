package nrxus.droptoken.persistence

import javax.persistence.*

@Entity
class DropToken(
        @ElementCollection
        val originalPlayers: List<String>,

        @ElementCollection
        val currentPlayers: MutableList<String>,

        @Column(nullable = false)
        var state: State,

        @Column(nullable = false)
        var turn: Int,

        @ElementCollection
        // flattened list of all the player tokens
        // a -1 player token means the space is empty
        // an existing player token represents the player # that placed the token
        // based on its original player position
        var tokens: MutableList<Int>,

        @Column(nullable = true)
        var winner: String?,

        @OneToMany(cascade = [CascadeType.ALL], mappedBy = "dropToken", fetch = FetchType.LAZY)
        val moves: MutableList<Move>,

        @Id
        @GeneratedValue
        val id: Long = 0
) {
    companion object {
        fun new(players: List<String>) = DropToken(
                originalPlayers = players,
                currentPlayers = players.toMutableList(),
                state = State.IN_PROGRESS,
                turn = 0,
                tokens = mutableListOf(),
                winner = null,
                moves = mutableListOf()
        )
    }

    enum class State { IN_PROGRESS, DONE }
}