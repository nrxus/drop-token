package nrxus.droptoken

import org.springframework.data.repository.CrudRepository

interface DropTokenRepository : CrudRepository<DropToken, Long> {}