package no.nav.bidrag.dokument.bestilling.model

import java.time.LocalDate

data class DokumentBestillingResult(
    var dokumentReferanse: String,
    var journalpostId: String,
)

data class DokumentBestilling(
    var mottaker: Mottaker? = null,
    var gjelder: Gjelder? = null,
    var kontaktInfo: EnhetKontaktInfo? = null,
    var parter: List<SoknadsPart> = emptyList(),
    var dokumentReferanse: String? = null,
    var tittel: String? = null,
    var enhet: String? = null,
    var saksnummer: String? = null,
    var spraak: String? = null,
    var roller: List<Rolle> = emptyList()
)

data class Rolle(
    val rolle: RolleType,
    val fodselsnummer: String? = null
)
data class SoknadsPart(
    val bidragsPliktigInfo: PartInfo? = null,
    val bidragsMottakerInfo: PartInfo? = null,
)

data class PartInfo(
    val fnr: String,
    val navn: String,
    val fodselsdato: LocalDate? = null,
    val landkode: String? = null,
    val datoDod: LocalDate? = null,
    val gebyr: Number? = null,
    val kravFremAv: String? = null
)
data class EnhetKontaktInfo(
    val navn: String,
    val telefonnummer: String,
    val returAdresse: Adresse,
    val enhetId: String
)

data class Gjelder(
    var fodselsnummer: String,
    var navn: String,
    val adresse: Adresse,
    val rolle: RolleType?,
)


data class Mottaker(
    var fodselsnummer: String,
    var navn: String,
    val adresse: Adresse,
    val rolle: RolleType?,
    val fodselsdato: LocalDate?,
)

data class Adresse(
    val adresselinje1: String,
    val adresselinje2: String? = null,
    val adresselinje3: String? = null,
    val boligNr: String? = null,
    val postnummer: String? = null,
    val poststed: String? = null,
    val landkode: String? = null
)