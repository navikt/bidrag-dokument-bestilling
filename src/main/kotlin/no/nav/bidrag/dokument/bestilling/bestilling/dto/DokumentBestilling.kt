package no.nav.bidrag.dokument.bestilling.bestilling.dto

import no.nav.bidrag.dokument.bestilling.model.Saksbehandler
import no.nav.bidrag.dokument.bestilling.model.tilLegacyKode
import no.nav.bidrag.dokument.bestilling.model.visningsnavnBruker
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.diverse.Språk
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import no.nav.bidrag.domene.enums.rolle.Rolletype
import no.nav.bidrag.domene.enums.rolle.SøktAvType
import no.nav.bidrag.domene.enums.vedtak.Engangsbeløptype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakskilde
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.domene.enums.vedtak.VirkningstidspunktÅrsakstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.BostatusPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SivilstandPeriode
import java.math.BigDecimal
import java.time.LocalDate

data class DokumentBestillingResult(
    val dokumentReferanse: String,
    val journalpostId: String,
    val bestillingSystem: String,
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
    val vedtakDetaljer: VedtakDetaljer? = null,
    val sjablonDetaljer: SjablonDetaljer,
    val sakDetaljer: SakDetaljer,
)

class Roller : MutableList<Rolle> by mutableListOf() {
    val barn: List<Barn> get() = filterIsInstance<Barn>().sortedBy { it.fodselsdato }
    val bidragsmottaker get() = filterIsInstance<PartInfo>().find { it.rolle == Rolletype.BIDRAGSMOTTAKER }
    val bidragspliktig get() = filterIsInstance<PartInfo>().find { it.rolle == Rolletype.BIDRAGSPLIKTIG }
}

interface Rolle {
    val rolle: Rolletype
    val fodselsnummer: String?
    val navn: String
    val fodselsdato: LocalDate?
}

data class Barn(
    override val rolle: Rolletype = Rolletype.BARN,
    override val fodselsnummer: String?,
    override val navn: String,
    override val fodselsdato: LocalDate?,
    val fornavn: String? = null,
    val bidragsbelop: Int? = null,
    val forskuddsbelop: Int? = null,
    val gebyrRm: Int? = null,
    val fodselsnummerRm: String? = null,
) : Rolle

data class SoknadsPart(
    val bidragsPliktigInfo: PartInfo? = null,
    val bidragsMottakerInfo: PartInfo? = null,
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
    val kravFremAv: String? = null,
) : Rolle

data class EnhetKontaktInfo(
    val navn: String,
    val telefonnummer: String,
    val postadresse: Adresse,
    val enhetId: String,
)

data class Gjelder(
    val fodselsnummer: String,
    val navn: String? = null,
    val adresse: Adresse? = null,
    val rolle: Rolletype?,
)

data class Mottaker(
    val fodselsnummer: String,
    val navn: String,
    val spraak: String,
    val adresse: Adresse?,
    val rolle: Rolletype?,
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
    val land: String? = null,
)

data class PeriodeFraTom(val fraDato: LocalDate, val tomDato: LocalDate? = null)
typealias BeløpFraTil = Pair<BigDecimal, BigDecimal>

fun BeløpFraTil.fraVerdi() = this.first

fun BeløpFraTil.tilVerdi() = this.second

data class VedtakPeriode(
    val fomDato: LocalDate,
    val tomDato: LocalDate? = null,
    val beløp: BigDecimal,
    val innkreving: String? = null,
    val resultatKode: String,
    val inntektGrense: BigDecimal,
    val maksInntekt: BigDecimal,
    val inntekter: List<InntektPeriode> = emptyList(),
)

data class InntektPeriode(
    val inntektPerioder: Set<ÅrMånedsperiode> = emptySet(),
    val inntektOpprinneligPerioder: Set<ÅrMånedsperiode> = emptySet(),
    val periode: ÅrMånedsperiode,
    val typer: Set<Inntektsrapportering> = emptySet(),
    val periodeTotalinntekt: Boolean? = false,
    val nettoKapitalInntekt: Boolean? = false,
    val beløpÅr: Int? = null,
    val fødselsnummer: String?,
    val beløp: BigDecimal,
    val rolle: Rolletype,
) {
    val type get() = typer.firstOrNull()
    val inntektPeriode get() = inntektPerioder.minByOrNull { it.fom }
    val opprinneligPeriode get() = inntektOpprinneligPerioder.minByOrNull { it.fom }
    val beskrivelse
        get() =
            when {
                typer.isNotEmpty() -> typer.first().visningsnavnBruker(Språk.NB, beløpÅr ?: opprinneligPeriode?.fom?.year)
                periodeTotalinntekt == true -> "Personens beregningsgrunnlag i perioden"
                nettoKapitalInntekt == true -> "Netto positive kapitalinntekter"
                else -> ""
            }
    val beløpKode
        get() =
            when {
                typer.isNotEmpty() -> typer.first().tilLegacyKode()
                periodeTotalinntekt == true -> "XINN"
                nettoKapitalInntekt == true -> "XKAP"
                else -> ""
            }
}

data class ForskuddInntektgrensePeriode(
    val fomDato: LocalDate,
    val tomDato: LocalDate? = null,
    val forsorgerType: Sivilstandskode,
    val antallBarn: Int,
    val beløp50Prosent: BeløpFraTil,
    val beløp75Prosent: BeløpFraTil,
)

data class VedtakDetaljer(
    val årsakKode: VirkningstidspunktÅrsakstype?,
    val avslagsKode: Resultatkode?,
    val virkningstidspunkt: LocalDate?,
    val mottattDato: LocalDate?,
    val soktFraDato: LocalDate?,
    val vedtattDato: LocalDate?,
    val saksbehandlerInfo: VedtakSaksbehandlerInfo,
    val vedtakType: Vedtakstype,
    val stønadType: Stønadstype?,
    val engangsbelopType: Engangsbeløptype?,
    val søknadFra: SøktAvType? = null,
    val kilde: Vedtakskilde,
    val vedtakBarn: List<VedtakBarn> = emptyList(),
    val barnIHusstandPerioder: List<BarnIHusstandPeriode> = emptyList(),
    val sivilstandPerioder: List<SivilstandPeriode> = emptyList(),
) {
    fun hentForskuddBarn(fodselsnummer: String): BigDecimal? =
        vedtakBarn
            .find { it.fødselsnummer == fodselsnummer }
            ?.stønadsendringer
            ?.find { it.type == Stønadstype.FORSKUDD }
            ?.vedtakPerioder?.find { it.tomDato == null }
            ?.beløp
}

data class BarnIHusstandPeriode(
    val periode: ÅrMånedsperiode,
    val antall: Int,
)

data class VedtakBarn(
    val fødselsnummer: String,
    val navn: String?,
    val bostatusPerioder: List<BostatusPeriode>,
    val stønadsendringer: List<VedtakBarnStonad> = emptyList(),
)

data class VedtakBarnStonad(
    val type: Stønadstype,
    val innkreving: Boolean,
    val vedtakPerioder: List<VedtakPeriode> = emptyList(),
    val forskuddInntektgrensePerioder: List<ForskuddInntektgrensePeriode> = emptyList(),
)

data class SjablonDetaljer(
    val multiplikatorInntekstgrenseForskudd: BigDecimal,
    val fastsettelseGebyr: BigDecimal,
    val forskuddInntektIntervall: BigDecimal,
    val forskuddSats: BigDecimal,
    val multiplikatorInnteksinslagBarn: BigDecimal,
)

data class SakDetaljer(
    val harUkjentPart: Boolean,
    val levdeAdskilt: Boolean,
)

data class VedtakSaksbehandlerInfo(
    val navn: String,
    val ident: String,
)
