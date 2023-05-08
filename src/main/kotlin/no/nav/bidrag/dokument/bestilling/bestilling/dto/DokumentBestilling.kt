package no.nav.bidrag.dokument.bestilling.bestilling.dto

import no.nav.bidrag.behandling.felles.enums.InntektType
import no.nav.bidrag.behandling.felles.enums.SivilstandKode
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.behandling.felles.enums.VedtakKilde
import no.nav.bidrag.behandling.felles.enums.VedtakType
import no.nav.bidrag.dokument.bestilling.model.Saksbehandler
import no.nav.bidrag.dokument.bestilling.model.SoknadFra
import no.nav.bidrag.dokument.bestilling.model.SoknadType
import no.nav.bidrag.domain.enums.Rolletype
import java.math.BigDecimal
import java.time.LocalDate

typealias GrunnlagRolleType = no.nav.bidrag.behandling.felles.enums.Rolle
data class GrunnlagInntektType(val inntektType: InntektType? = null, val periodeBeregningsGrunnlag: Boolean? = false) {
    val beskrivelse get() = inntektType?.beskrivelse ?: if (periodeBeregningsGrunnlag == true) "Personens beregningsgrunnlag i perioden" else ""
    val belopstype get() = inntektType?.belopstype ?: if (periodeBeregningsGrunnlag == true) "XINN" else ""
}
data class DokumentBestillingResult(
    val dokumentReferanse: String,
    val journalpostId: String,
    val bestillingSystem: String
)

data class DokumentBestilling(
    var mottaker: Mottaker? = null,
    var gjelder: Gjelder? = null,
    var kontaktInfo: EnhetKontaktInfo? = null,
    val saksbehandler: Saksbehandler? = null,
    var dokumentreferanse: String? = null,
    val tittel: String? = null,
    val enhet: String? = null,
    val saksnummer: String? = null,
    val datoSakOpprettet: LocalDate? = null,
    val spraak: String? = null,
    val roller: Roller = Roller(),
    val rmISak: Boolean? = false,
    var vedtakDetaljer: VedtakDetaljer? = null,
    var sjablonDetaljer: SjablonDetaljer,
    var sakDetaljer: SakDetaljer
)

class Roller : MutableList<Rolle> by mutableListOf() {
    val barn: List<Barn> get() = filterIsInstance<Barn>().sortedBy { it.fodselsdato }
    val bidragsmottaker get() = filterIsInstance<PartInfo>().find { it.rolle == Rolletype.BM }
    val bidragspliktig get() = filterIsInstance<PartInfo>().find { it.rolle == Rolletype.BP }
}
interface Rolle {
    val rolle: Rolletype
    val fodselsnummer: String?
    val navn: String
    val fodselsdato: LocalDate?
}

data class Barn(
    override val rolle: Rolletype = Rolletype.BA,
    override val fodselsnummer: String?,
    override val navn: String,
    override val fodselsdato: LocalDate?,
    val fornavn: String? = null,
    val bidragsbelop: Int? = null,
    val forskuddsbelop: Int? = null,
    val gebyrRm: Int? = null,
    val fodselsnummerRm: String? = null
) : Rolle
data class SoknadsPart(
    val bidragsPliktigInfo: PartInfo? = null,
    val bidragsMottakerInfo: PartInfo? = null
)

data class PartInfo(
    override var rolle: Rolletype,
    override val fodselsnummer: String? = null,
    override val navn: String,
    override val fodselsdato: LocalDate? = null,
    val doedsdato: LocalDate? = null,
    val landkode: String? = null,
    val landkode3: String? = null,
    val datoDod: LocalDate? = null,
    val gebyr: Number? = null,
    val kravFremAv: String? = null
) : Rolle
data class EnhetKontaktInfo(
    val navn: String,
    val telefonnummer: String,
    val postadresse: Adresse,
    val enhetId: String
)

data class Gjelder(
    val fodselsnummer: String,
    val navn: String? = null,
    val adresse: Adresse? = null,
    val rolle: Rolletype?
)

data class Mottaker(
    val fodselsnummer: String,
    val navn: String,
    val spraak: String,
    val adresse: Adresse?,
    val rolle: Rolletype?,
    val fodselsdato: LocalDate?
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

typealias BeløpFraTil = Pair<BigDecimal, BigDecimal>
fun BeløpFraTil.fraVerdi() = this.first
fun BeløpFraTil.tilVerdi() = this.second

data class Soknad(
    val soknadDato: LocalDate,
    val sendtDato: LocalDate,
    val svarFraDato: LocalDate? = null,
    val vedtattDato: LocalDate? = null,
    val virkningDato: LocalDate? = null,
    val type: SoknadType,
    val årsak: String,
    val undergruppe: String,
    val sakstypeOmr: String? = null // TODO: What is this?,
)
data class VedtakPeriode(
    val fomDato: LocalDate,
    val tomDato: LocalDate? = null,
    val beløp: BigDecimal,
    val innkreving: String? = null,
    val resultatKode: String,
    val inntektPerioder: List<InntektPeriode> = emptyList()
)
data class InntektPeriode(
    val fomDato: LocalDate,
    val tomDato: LocalDate? = null,
    val beløpType: GrunnlagInntektType,
    val beløpÅr: Int,
    val fodselsnummer: String?,
    val beløp: BigDecimal,
    val rolle: GrunnlagRolleType,
    val inntektsgrense: Int
)

data class GrunnlagForskuddPeriode(
    val fomDato: LocalDate,
    val tomDato: LocalDate,
    val forsorgerType: ForsorgerType,
    val antallBarn: Int,
    val beløp50Prosent: BeløpFraTil,
    val beløp75Prosent: BeløpFraTil
)

data class VedtakDetaljer(
    val virkningÅrsakKode: String?,
    val virkningDato: LocalDate?,
    val soknadDato: LocalDate?,
    val soktFraDato: LocalDate?,
    val vedtattDato: LocalDate?,
    val vedtakType: VedtakType,
    val søknadType: StonadType?,
    val søknadFra: SoknadFra? = null,
    val kilde: VedtakKilde,
    val vedtakBarn: List<VedtakBarn> = emptyList(),
    var sivilstandPerioder: List<SivilstandPeriode> = emptyList(),
    var grunnlagForskuddPerioder: List<GrunnlagForskuddPeriode> = emptyList()
) {
    fun hentForskuddBarn(fodselsnummer: String): BigDecimal? = vedtakBarn
        .find { it.fodselsnummer == fodselsnummer }
        ?.vedtakDetaljer
        ?.find { it.type == StonadType.FORSKUDD }
        ?.vedtakPerioder?.find { it.tomDato == null }
        ?.beløp
}

data class VedtakBarn(
    val fodselsnummer: String,
    val navn: String?,
    val harSammeAdresse: Boolean,
    val vedtakDetaljer: List<VedtakBarnDetaljer> = emptyList()
)

data class VedtakBarnDetaljer(
    val type: StonadType,
    val vedtakPerioder: List<VedtakPeriode> = emptyList()
)
data class SivilstandPeriode(
    val fomDato: LocalDate,
    val tomDato: LocalDate? = null,
    val sivilstandKode: SivilstandKode,
    val sivilstandBeskrivelse: String
)

enum class ForsorgerType {
    ENSLIG,
    GIFT_SAMBOER
}

data class SjablonDetaljer(
    val multiplikatorInntekstgrenseForskudd: BigDecimal,
    val fastsettelseGebyr: BigDecimal,
    val forskuddInntektIntervall: BigDecimal
)

data class SakDetaljer(
    val harUkjentPart: Boolean,
    val levdeAdskilt: Boolean
)
