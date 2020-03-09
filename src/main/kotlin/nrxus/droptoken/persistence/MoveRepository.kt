package nrxus.droptoken.persistence

import org.springframework.data.repository.CrudRepository

interface MoveRepository : CrudRepository<Move, Move.MoveId> {
    fun findByNumberAndDropTokenId(number: Int, dropTokenId: Long): Move?
}