package io.github.edy4c7.locationmapper.web.aspects

import io.github.edy4c7.locationmapper.domains.exceptions.MapImageSourceException
import org.springframework.context.MessageSource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
internal class ExceptionHandlerAdvice(private val messageSource: MessageSource) : ResponseEntityExceptionHandler() {
    @ExceptionHandler(MapImageSourceException::class)
    fun handleMapImageSourceException(ex: MapImageSourceException, req: WebRequest): ResponseEntity<Any> {
        return handleExceptionInternal(
            ex,
            messageSource.getMessage("mapimagesourceexception", null, req.locale),
            HttpHeaders(),
            HttpStatus.SERVICE_UNAVAILABLE,
            req
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleThrowable(ex: Exception, req: WebRequest): ResponseEntity<Any> {
        return handleExceptionInternal(
            ex,
            "Internal error",
            HttpHeaders(),
            HttpStatus.INTERNAL_SERVER_ERROR,
            req
        )
    }

    override fun handleExceptionInternal(
        ex: java.lang.Exception,
        body: Any?,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest,
    ): ResponseEntity<Any> {
        logger.error(ex)
        return super.handleExceptionInternal(ex, body, headers, status, request)
    }
}