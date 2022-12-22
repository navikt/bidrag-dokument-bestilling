package no.nav.bidrag.dokument.bestilling.konsumer.dto

import no.nav.bidrag.dokument.bestilling.model.Ident

data class HentSakResponse (
    val eierfogd: String? = null,
    val saksnummer: String? = null,
    val saksstatus: String? = null,
    val kategori: String? = null,
    val erParagraf19: Boolean? = false,
    val begrensetTilgang: Boolean? = false,
    val roller: List<SakRolle> = emptyList()
)

data class SakRolle(
    val foedselsnummer: Ident? = null,
    val rolleType: RolleType? = null
)

enum class RolleType(var beskrivelse: String) {
    BA("Barn"),
    BM("Bidragsmottaker"),
    BP("Bidragspliktig"),
    FR("Feilregistrert"),
    RM("Reell mottaker"),
    UKJENT("Ukjent"),
}