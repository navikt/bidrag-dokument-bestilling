package no.nav.bidrag.dokument.bestilling.model

import no.nav.bidrag.dokument.bestilling.bestilling.dto.BrevKode

class ProduksjonAvDokumentStottesIkke(brevKode: BrevKode): RuntimeException("Produksjon av dokument med brevkode=$brevKode støttes ikke")

class FantIkkePersonException(msg: String): RuntimeException(msg)
class FantIkkeEnhetException(msg: String): RuntimeException(msg)
class FantIkkeSakException(msg: String): RuntimeException(msg)
class HentSakFeiletException(msg: String, throwable: Throwable): RuntimeException(msg, throwable)
class HentPersonFeiletException(msg: String, throwable: Throwable): RuntimeException(msg, throwable)
class SamhandlerManglerKontaktinformasjon(msg: String): RuntimeException(msg)
class ManglerGjelderException(msg: String): RuntimeException(msg)
class ForsendelseFraHendelseManglerDokument(msg: String): RuntimeException(msg)
class ForsendelseFraHendelseManglerNødvendigDetaljer(msg: String): RuntimeException(msg)
class BestillingManglerMottaker(): RuntimeException("Bestilling mangler mottaker")
class UgyldigBestillingAvDokument(msg: String): RuntimeException(msg)