package no.nav.bidrag.dokument.bestilling.model

import no.nav.bidrag.dokument.bestilling.bestilling.dto.BarnIHustandPeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.InntektPeriode
import no.nav.bidrag.dokument.bestilling.tjenester.hentInnslagKapitalinntekt
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.enums.rolle.Rolletype
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.BaseGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BostatusPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBarnIHusstand
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSumInntekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.InntektsrapporteringPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.Person
import no.nav.bidrag.transport.behandling.felles.grunnlag.SivilstandPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.SøknadGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.VirkningstidspunktGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragsmottaker
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerBasertPåFremmedReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentPersonMedReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.innholdTilObjekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.personIdent
import no.nav.bidrag.transport.behandling.felles.grunnlag.personObjekt
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakPeriodeDto
import java.math.BigDecimal

val kapitalinntektTyper = listOf(Inntektsrapportering.KAPITALINNTEKT, Inntektsrapportering.KAPITALINNTEKT_EGNE_OPPLYSNINGER)
val grunnlagstyperRolle =
    listOf(
        Grunnlagstype.PERSON_SØKNADSBARN,
        Grunnlagstype.PERSON_BIDRAGSMOTTAKER,
        Grunnlagstype.PERSON_BIDRAGSPLIKTIG,
    )
val inntektsrapporteringSomKreverBarn =
    listOf(
        Inntektsrapportering.BARNETILLEGG,
        Inntektsrapportering.KONTANTSTØTTE,
    )

data class VedtakSaksbehandlerInfo(
    val navn: String,
    val ident: String,
)

fun List<GrunnlagDto>.hentRoller(): List<Person> =
    filter { grunnlagstyperRolle.contains(it.type) }.map { it.innholdTilObjekt<Person>() }

fun VedtakDto.tilSaksbehandler() =
    VedtakSaksbehandlerInfo(
        navn = opprettetAvNavn ?: "",
        ident = opprettetAv,
    )

fun VedtakDto.hentVirkningstidspunkt(): VirkningstidspunktGrunnlag? {
    return grunnlagListe.filtrerBasertPåEgenReferanse(Grunnlagstype.VIRKNINGSTIDSPUNKT)
        .firstOrNull()?.innholdTilObjekt<VirkningstidspunktGrunnlag>()
}

fun VedtakDto.hentSøknad(): SøknadGrunnlag {
    return grunnlagListe.filtrerBasertPåEgenReferanse(Grunnlagstype.SØKNAD).first()
        .innholdTilObjekt<SøknadGrunnlag>()
}

fun List<GrunnlagDto>.mapSivilstand(): List<SivilstandPeriode> =
    filtrerBasertPåEgenReferanse(Grunnlagstype.SIVILSTAND_PERIODE)
        .map { it.innholdTilObjekt<SivilstandPeriode>() }

fun List<GrunnlagDto>.mapHusstandsbarn(): List<Husstandsbarn> =
    filtrerBasertPåEgenReferanse(Grunnlagstype.BOSTATUS_PERIODE)
        .groupBy { it.gjelderReferanse }.map { (gjelderPersonReferanse, grunnlag) ->
            val person = hentPersonMedReferanse(gjelderPersonReferanse) ?: throw RuntimeException("Mangler person grunnlag for referanse $gjelderPersonReferanse")
            Husstandsbarn(
                person.personObjekt,
                grunnlag.innholdTilObjekt<BostatusPeriode>(),
            )
        }

fun List<GrunnlagDto>.mapBarnIHusstandPerioder(): List<BarnIHustandPeriode> {
    val barnIHusstand =
        filtrerBasertPåEgenReferanse(Grunnlagstype.DELBEREGNING_BARN_I_HUSSTAND)
            .map { it.innholdTilObjekt<DelberegningBarnIHusstand>() }

    return barnIHusstand.sortedBy { it.periode.fom }.map {
        BarnIHustandPeriode(
            it.periode.tilLocalDateFom(),
            it.periode.tilLocalDateTil(),
            it.antallBarn,
        )
    }
}

fun List<GrunnlagDto>.hentBarnIHusstandPerioderForBarn(ident: String): Husstandsbarn {
    return mapHusstandsbarn().find { it.gjelderBarn.ident?.verdi == ident }!!
}

fun List<GrunnlagDto>.hentDelberegningInntektForPeriode(periode: VedtakPeriodeDto): BaseGrunnlag? {
    val sluttberegning = filtrerBasertPåEgenReferanser(Grunnlagstype.SLUTTBEREGNING_FORSKUDD, periode.grunnlagReferanseListe).firstOrNull() ?: return null
    return filtrerBasertPåEgenReferanser(Grunnlagstype.DELBEREGNING_SUM_INNTEKT, sluttberegning.grunnlagsreferanseListe).firstOrNull()
}

fun List<GrunnlagDto>.hentInntekterForPeriode(
    periode: VedtakPeriodeDto,
    person: BaseGrunnlag? = null,
): List<BaseGrunnlag> {
    val sluttberegning = filtrerBasertPåEgenReferanser(Grunnlagstype.SLUTTBEREGNING_FORSKUDD, periode.grunnlagReferanseListe).firstOrNull() ?: return emptyList()
    val delberegningInntekt =
        filtrerBasertPåEgenReferanser(Grunnlagstype.DELBEREGNING_SUM_INNTEKT, sluttberegning.grunnlagsreferanseListe)
    return filtrerBasertPåEgenReferanse(Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE).filter { inntekt ->
        delberegningInntekt.any { it.grunnlagsreferanseListe.contains(inntekt.referanse) } && (person == null || inntekt.gjelderReferanse == person.referanse)
    }
}

fun List<GrunnlagDto>.hentKapitalInntekterForPeriode(
    periode: VedtakPeriodeDto,
    rolle: BaseGrunnlag? = null,
): List<InntektsrapporteringPeriode> {
    return hentInntekterForPeriode(periode, rolle)
        .map { it.innholdTilObjekt<InntektsrapporteringPeriode>() }
        .filter { kapitalinntektTyper.contains(it.inntektsrapportering) }
}

fun List<InntektsrapporteringPeriode>.totalKapitalinntekt(): BigDecimal =
    filter { kapitalinntektTyper.contains(it.inntektsrapportering) }.map { it.beløp }
        .reduceOrNull { acc, num -> acc + num } ?: BigDecimal.ZERO

fun List<GrunnlagDto>.hentNettoKapitalinntektForRolle(
    vedtakPeriodeDto: VedtakPeriodeDto,
    rolle: BaseGrunnlag,
): InntektPeriode? =
    hentKapitalInntekterForPeriode(vedtakPeriodeDto, rolle).totalKapitalinntekt()
        .takeIf { it > BigDecimal.ZERO }
        ?.let { totalBelop ->
            val rollePersonInfo = rolle.personObjekt
            val innslagKapitalInntektSjablonVerdi =
                finnSjablonMedType(SjablonTallNavn.INNSLAG_KAPITALINNTEKT_BELØP)?.verdi ?: hentInnslagKapitalinntekt(vedtakPeriodeDto.periode.tilLocalDateTil())
            val nettoKapitalinntekt = totalBelop - innslagKapitalInntektSjablonVerdi
            nettoKapitalinntekt.takeIf { it > BigDecimal.ZERO }?.let {
                InntektPeriode(
                    periode = vedtakPeriodeDto.periode,
                    nettoKapitalInntekt = true,
                    rolle = rolle.type.tilRolletype(),
                    fødselsnummer = rollePersonInfo.ident!!.verdi,
                    beløp = it,
                )
            }
        }

data class Husstandsbarn(
    val gjelderBarn: Person,
    val bostatus: List<BostatusPeriode>,
)

fun List<BaseGrunnlag>.finnGrunnlagMedType(
    type: Grunnlagstype,
    fraGrunnlag: BaseGrunnlag,
): BaseGrunnlag? {
    val grunnlag = filtrerBasertPåEgenReferanser(type, fraGrunnlag.grunnlagsreferanseListe)
    if (grunnlag.isEmpty()) {
        fraGrunnlag.grunnlagsreferanseListe.filter {
            val nesteGrunnlag = filtrerBasertPåEgenReferanse(null, it)
            return finnGrunnlagMedType(type, nesteGrunnlag.first())
        }
    }
    return null
}

fun List<BaseGrunnlag>.finnSjablonMedType(type: SjablonTallNavn) =
    filtrerBasertPåEgenReferanse(Grunnlagstype.SJABLON)
        .map { it.innholdTilObjekt<SjablonGrunnlag>() }.find { it.sjablon == type }

fun List<BaseGrunnlag>.filtrerBasertPåEgenReferanser(
    type: Grunnlagstype,
    referanser: List<Grunnlagsreferanse>,
): List<BaseGrunnlag> {
    return filtrerBasertPåEgenReferanse(type)
        .filter { referanser.contains(it.referanse) }
}

fun Grunnlagstype.tilRolletype() =
    when (this) {
        Grunnlagstype.PERSON_BIDRAGSPLIKTIG -> Rolletype.BIDRAGSPLIKTIG
        Grunnlagstype.PERSON_SØKNADSBARN -> Rolletype.BARN
        Grunnlagstype.PERSON_BIDRAGSMOTTAKER -> Rolletype.BIDRAGSMOTTAKER
        Grunnlagstype.PERSON_REELL_MOTTAKER -> Rolletype.REELMOTTAKER
        else -> throw RuntimeException("Mangler grunnlagsmapping for rolletype $this")
    }

fun ÅrMånedsperiode.tilLocalDateFom() = fom.atDay(1)

fun ÅrMånedsperiode?.tilLocalDateTil() = this?.til?.atEndOfMonth()

fun List<GrunnlagDto>.hentTotalInntektForPeriode(vedtakPeriode: VedtakPeriodeDto): List<InntektPeriode> {
    return hentDelberegningInntektForPeriode(vedtakPeriode)?.let { inntektPeriode ->
        val sluttberegning = this.filtrerBasertPåFremmedReferanse(null, inntektPeriode.referanse).firstOrNull()
        val delberegningInntekt = inntektPeriode.innholdTilObjekt<DelberegningSumInntekt>()
        val gjelderPersonGrunnlag = bidragsmottaker!!
        listOf(
            InntektPeriode(
                periode = vedtakPeriode.periode,
                beløp = delberegningInntekt.totalinntekt,
                periodeTotalinntekt = true,
                beløpÅr = vedtakPeriode.periode.fom.year,
                // TODO Gjelder bare for Forskudd
                rolle = Rolletype.BIDRAGSMOTTAKER,
                fødselsnummer = gjelderPersonGrunnlag.personIdent,
            ),
        )
    } ?: emptyList()
}
