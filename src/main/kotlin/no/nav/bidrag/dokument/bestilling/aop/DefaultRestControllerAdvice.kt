package no.nav.bidrag.dokument.bestilling.aop

import no.nav.bidrag.dokument.bestilling.model.FantIkkeEnhetException
import no.nav.bidrag.dokument.bestilling.model.FantIkkePersonException
import no.nav.bidrag.dokument.bestilling.model.FantIkkeSakException
import no.nav.bidrag.dokument.bestilling.model.ManglerGjelderException
import no.nav.bidrag.dokument.bestilling.model.ProduksjonAvDokumentStottesIkke
import no.nav.bidrag.dokument.bestilling.model.SamhandlerManglerKontaktinformasjon
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.core.convert.ConversionFailedException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class DefaultRestControllerAdvice {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(DefaultRestControllerAdvice::class.java)
    }

    @ResponseBody
    @ExceptionHandler(value = [
        ManglerGjelderException::class,
        FantIkkeEnhetException::class,
        FantIkkePersonException::class,
        FantIkkeSakException::class,
        SamhandlerManglerKontaktinformasjon::class
    ])
    fun fantIkkeData(exception: RuntimeException): ResponseEntity<*> {
        LOGGER.warn(exception.message)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .header(HttpHeaders.WARNING, exception.message)
            .build<Any>()
    }

    @ResponseBody
    @ExceptionHandler(ProduksjonAvDokumentStottesIkke::class)
    fun ustottetBrev(exception: ProduksjonAvDokumentStottesIkke): ResponseEntity<*> {
        LOGGER.warn(exception.message)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .header(HttpHeaders.WARNING, exception.message)
            .build<Any>()
    }

    @ResponseBody
    @ExceptionHandler(HttpStatusCodeException::class)
    fun handleHttpStatusException(exception: HttpStatusCodeException): ResponseEntity<*> {
        LOGGER.warn("Det skjedde en feil ved kall mot ekstern tjeneste: ${exception.message}", exception)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header(HttpHeaders.WARNING, "Det skjedde en feil ved kall mot ekstern tjeneste: ${exception.message}")
            .build<Any>()
    }

    @ResponseBody
    @ExceptionHandler(JwtTokenUnauthorizedException::class)
    fun handleUnauthorizedException(exception: JwtTokenUnauthorizedException): ResponseEntity<*> {
        LOGGER.warn("Ugyldig eller manglende sikkerhetstoken", exception)
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .header(HttpHeaders.WARNING, "Ugyldig eller manglende sikkerhetstoken")
            .build<Any>()
    }

    @ResponseBody
    @ExceptionHandler(value = [IllegalArgumentException::class, MethodArgumentTypeMismatchException::class, ConversionFailedException::class])
    fun handleInvalidValueExceptions(exception: Exception): ResponseEntity<*> {
        LOGGER.warn("Kallet inneholder ugyldig verdi", exception)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .header(HttpHeaders.WARNING, "Kallet inneholder ugyldig verdi: ${exception.message}")
            .build<Any>()
    }

    @ResponseBody
    @ExceptionHandler(Exception::class)
    fun handleOtherExceptions(exception: Exception): ResponseEntity<*> {
        LOGGER.error("Det skjedde en ukjent feil: ${exception.message}", exception)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header(HttpHeaders.WARNING, "Det skjedde en ukjent feil: ${exception.message}")
            .build<Any>()
    }

}