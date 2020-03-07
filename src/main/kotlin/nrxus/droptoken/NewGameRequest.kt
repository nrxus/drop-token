package nrxus.droptoken

import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.Size

class NewGameRequest(
        @field:Min(value = 4, message = "columns must be 4")
        @field:Max(value = 4, message = "columns must be 4")
        val columns: Int,

        @field:Min(value = 4, message = "rows must be 4")
        @field:Max(value = 4, message = "rows must be 4")
        val rows: Int,

        @field:Size(min = 2, max = 2, message = "there must be exactly two players")
        val players: List<String>
)