package no.nav.bidrag.dokument.bestilling.bestilling.dto

import no.nav.bidrag.dokument.bestilling.konsumer.dto.RolleType
import no.nav.bidrag.dokument.bestilling.model.Saksbehandler
import java.time.LocalDate

data class DokumentBestillingResult(
    val dokumentReferanse: String,
    val journalpostId: String,
    val arkivSystem: String
)

data class DokumentBestilling(
    var mottaker: Mottaker? = null,
    var gjelder: Gjelder? = null,
    var kontaktInfo: EnhetKontaktInfo? = null,
    var saksbehandler: Saksbehandler? = null,
    var dokumentreferanse: String? = null,
    var tittel: String? = null,
    var enhet: String? = null,
    var saksnummer: String? = null,
    var spraak: String? = null,
    var roller: Roller = Roller(),
    var rmISak: Boolean? = false
)

class Roller: MutableList<Rolle> by mutableListOf() {
    val barn: List<Barn> get() =  filterIsInstance<Barn>().sortedBy { it.fodselsdato }
    val bidragsmottaker get() = filterIsInstance<PartInfo>().find { it.rolle == RolleType.BM }
    val bidragspliktig get() = filterIsInstance<PartInfo>().find { it.rolle == RolleType.BP }
}
interface Rolle {
    val rolle: RolleType
    val fodselsnummer: String?
    val navn: String
    val fodselsdato: LocalDate?
}

data class Barn(
    override val rolle: RolleType = RolleType.BA,
    override val fodselsnummer: String?,
    override val navn: String,
    override val fodselsdato: LocalDate?,
    val fornavn: String? = null,
    val bidragsbelop: Number? = null,
    val forskuddsbelop: Number? = null,
    val gebyrRm: Number? = null,
    val fodselsnummerRm: String? = null
): Rolle
data class SoknadsPart(
    val bidragsPliktigInfo: PartInfo? = null,
    val bidragsMottakerInfo: PartInfo? = null,
)

data class PartInfo(
    override var rolle: RolleType,
    override val fodselsnummer: String? = null,
    override val navn: String,
    override val fodselsdato: LocalDate? = null,
    val doedsdato: LocalDate? = null,
    val landkode: String? = null,
    val landkode3: String? = null,
    val datoDod: LocalDate? = null,
    val gebyr: Number? = null,
    val kravFremAv: String? = null
): Rolle
data class EnhetKontaktInfo(
    val navn: String,
    val telefonnummer: String,
    val postadresse: Adresse,
    val enhetId: String
)

data class Gjelder(
    var fodselsnummer: String,
    var navn: String? = null,
    val adresse: Adresse? = null,
    val rolle: RolleType?,
)


data class Mottaker(
    var fodselsnummer: String,
    var navn: String,
    var spraak: String,
    val adresse: Adresse?,
    val rolle: RolleType?,
    val fodselsdato: LocalDate?,
)

data class Adresse(
    val adresselinje1: String,
    val adresselinje2: String? = null,
    val adresselinje3: String? = null,
    val adresselinje4: String? = null,
    val bruksenhetsnummer: String? = null,
    val postnummer: String? = null,
    val poststed: String? = null,
    val landkode: String? = null,
    val landkode3: String? = null,
    val land: String? = null
)

