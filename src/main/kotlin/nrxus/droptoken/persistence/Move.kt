package nrxus.droptoken.persistence

import java.io.Serializable
import javax.persistence.*

@Entity
@IdClass(Move.MoveId::class)
class Move(
        @Column(nullable = false)
        val type: MoveType,

        @Column(nullable = false)
        val player: String,

        @Column(nullable = true)
        val column: Int?,

        @Column(nullable = false)
        val number: Int,

        @ManyToOne(optional = false, fetch = FetchType.LAZY)
        val dropToken: DropToken,

        @Id
        @GeneratedValue
        val id: Long = 0
) {
    class MoveId(val number: Int = 0) : Serializable {
        lateinit var dropToken: DropToken
    }

    enum class MoveType { MOVE, QUIT }
}