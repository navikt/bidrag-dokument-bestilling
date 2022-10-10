package no.nav.bidrag.dokument.bestilling.model

class ProduksjonAvDokumentStottesIkke(brevKode: BrevKode): RuntimeException("Produksjon av dokument med brevkode=$brevKode støttes ikke")

class FantIkkePersonException(msg: String): RuntimeException(msg)
class FantIkkeSakException(msg: String): RuntimeException(msg)