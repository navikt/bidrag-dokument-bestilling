package no.nav.bidrag.dokument.bestilling.model

class ProduksjonAvDokumentStottesIkke(brevKode: BrevKode): RuntimeException("Produksjon av dokument med brevkode=$brevKode støttes ikke")

class FantIkkePersonException(msg: String): RuntimeException(msg)
class FantIkkeEnhetException(msg: String): RuntimeException(msg)
class FantIkkeSakException(msg: String): RuntimeException(msg)
class HentSakFeiletException(msg: String, throwable: Throwable): RuntimeException(msg, throwable)
class HentPersonFeiletException(msg: String, throwable: Throwable): RuntimeException(msg, throwable)
class SamhandlerManglerKontaktinformasjon(msg: String): RuntimeException(msg)
class ManglerGjelderException(msg: String): RuntimeException(msg)