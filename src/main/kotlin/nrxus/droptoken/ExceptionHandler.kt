package nrxus.droptoken

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

class SubError(
        val field: String,
        val message: String?
)

class ApiError(
        val status: HttpStatus,
        val message: String,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        val errors: List<SubError> = listOf()
)

@ControllerAdvice
class ExceptionHandler : ResponseEntityExceptionHandler() {
    override fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException, headers: HttpHeaders, status: HttpStatus, request: WebRequest): ResponseEntity<Any> {
        val errors = ex.bindingResult.fieldErrors.map { SubError(it.field, it.defaultMessage) }
        return ResponseEntity.badRequest().body(
                ApiError(status = HttpStatus.BAD_REQUEST, message = "validation error", errors = errors)
        )
    }

    override fun handleHttpMessageNotReadable(ex: HttpMessageNotReadableException, headers: HttpHeaders, status: HttpStatus, request: WebRequest): ResponseEntity<Any> {
        val message = when (val rootCause = ex.rootCause) {
            null -> ex.localizedMessage
            is MismatchedInputException -> rootCause.originalMessage
            else -> rootCause.localizedMessage
        }

        return ResponseEntity.badRequest().body(
                ApiError(status = HttpStatus.BAD_REQUEST, message = message)
        )
    }
}