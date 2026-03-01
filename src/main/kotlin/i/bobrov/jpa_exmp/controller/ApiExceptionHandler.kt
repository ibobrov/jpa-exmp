package i.bobrov.jpa_exmp.controller

import i.bobrov.jpa_exmp.common.ApiError
import i.bobrov.jpa_exmp.common.PickingServiceException
import i.bobrov.jpa_exmp.common.Response
import i.bobrov.jpa_exmp.common.Result
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ApiError =
        ApiError(message = ex.message ?: "Not found data")

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(PickingServiceException::class)
    fun handlePicking(ex: PickingServiceException): Response<Nothing> =
        Result.failure(code = ex.code.name, message = ex.message)
}
