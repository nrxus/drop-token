package nrxus.droptoken.persistence

import org.springframework.data.repository.CrudRepository

interface DropTokenRepository : CrudRepository<DropToken, Long>