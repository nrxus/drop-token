package nrxus.droptoken

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class DropToken(
        @Id
        @GeneratedValue
        val id: Long = 0
)