package no.nav.bidrag.dokument.bestilling.model

import no.nav.bidrag.dokument.bestilling.bestilling.dto.BrevKode
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

class ProduksjonAvDokumentStottesIkke(brevKode: BrevKode) : RuntimeException("Produksjon av dokument med brevkode=$brevKode støttes ikke")

class FantIkkePersonException(msg: String) : RuntimeException(msg)
class FantIkkeEnhetException(msg: String) : RuntimeException(msg)
class FantIkkeSakException(msg: String) : RuntimeException(msg)
class HentSakFeiletException(msg: String, throwable: Throwable) : RuntimeException(msg, throwable)
class HentVedtakFeiletException(msg: String, throwable: Throwable) : RuntimeException(msg, throwable)
class HentPersonFeiletException(msg: String, throwable: Throwable) : RuntimeException(msg, throwable)
class SamhandlerManglerKontaktinformasjon(msg: String) : RuntimeException(msg)
class ManglerGjelderException(msg: String) : RuntimeException(msg)
class BestillingManglerMottaker() : RuntimeException("Bestilling mangler mottaker")
fun fantIkkeVedtak(vedtakId: String): Nothing =
    throw HttpClientErrorException(HttpStatus.BAD_REQUEST, "Fant ikke vedtak med id $vedtakId")

fun fantIkkeSak(saksnummer: String): Nothing =
    throw HttpClientErrorException(HttpStatus.BAD_REQUEST, "Fant ikke sak med id $saksnummer")