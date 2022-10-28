package no.nav.bidrag.dokument.bestilling.aop

import no.nav.bidrag.dokument.bestilling.model.FantIkkePersonException
import no.nav.bidrag.dokument.bestilling.model.FantIkkeSakException
import no.nav.bidrag.dokument.bestilling.model.ProduksjonAvDokumentStottesIkke
import no.nav.bidrag.dokument.bestilling.model.SamhandlerManglerKontaktinformasjon
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class DefaultRestControllerAdvice {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(DefaultRestControllerAdvice::class.java)
    }

    @ResponseBody
    @ExceptionHandler(value = [FantIkkePersonException::class, FantIkkeSakException::class, SamhandlerManglerKontaktinformasjon::class])
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
    @ExceptionHandler(Exception::class)
    fun handleOtherExceptions(exception: Exception): ResponseEntity<*> {
        LOGGER.warn("Det skjedde en ukjent feil", exception)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header(HttpHeaders.WARNING, "Det skjedde en ukjent feil: ${exception.message}")
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


}