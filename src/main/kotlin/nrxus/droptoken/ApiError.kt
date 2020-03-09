package nrxus.droptoken

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.HttpStatus

class ApiError(
        val status: HttpStatus,
        val message: String,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        val errors: List<SubError> = listOf()
) {
    class SubError(
            val field: String,
            val message: String?
    )
}