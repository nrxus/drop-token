package nrxus.droptoken

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class DropToken(
        @Column(nullable = false)
        val player1: String,

        @Column(nullable = false)
        val player2: String,

        @Column(nullable = false)
        val state: State,

        @Column(nullable = true)
        val winner: String? = null,

        @Id
        @GeneratedValue
        val id: Long = 0
) {
    enum class State { IN_PROGRESS, DONE }
}