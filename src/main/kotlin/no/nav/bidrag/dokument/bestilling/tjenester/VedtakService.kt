package no.nav.bidrag.dokument.bestilling.tjenester

import no.nav.bidrag.dokument.bestilling.bestilling.dto.AndelUnderholdskostnadPeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.BidragsevnePeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.BrevSjablonVerdier
import no.nav.bidrag.dokument.bestilling.bestilling.dto.DataPeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.ForskuddInntektgrensePeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.GebyrInfoDto
import no.nav.bidrag.dokument.bestilling.bestilling.dto.InntektPeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.Samværsperiode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.Skatt
import no.nav.bidrag.dokument.bestilling.bestilling.dto.SærbidragBeregning
import no.nav.bidrag.dokument.bestilling.bestilling.dto.UnderholdEgneBarnIHusstand
import no.nav.bidrag.dokument.bestilling.bestilling.dto.UnderholdskostnaderPeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakBarn
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakBarnEngangsbeløp
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakBarnStonad
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakDetaljer
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakPeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakPeriodeReferanse
import no.nav.bidrag.dokument.bestilling.consumer.BidragVedtakConsumer
import no.nav.bidrag.dokument.bestilling.model.fantIkkeVedtak
import no.nav.bidrag.dokument.bestilling.model.finnSjablonMedType
import no.nav.bidrag.dokument.bestilling.model.getLastDayOfPreviousMonth
import no.nav.bidrag.dokument.bestilling.model.hentBarnIHusstandPerioderForBarn
import no.nav.bidrag.dokument.bestilling.model.hentInntekterForPeriode
import no.nav.bidrag.dokument.bestilling.model.hentNettoKapitalinntektForRolle
import no.nav.bidrag.dokument.bestilling.model.hentSøknad
import no.nav.bidrag.dokument.bestilling.model.hentTotalInntektForPeriode
import no.nav.bidrag.dokument.bestilling.model.hentVirkningstidspunkt
import no.nav.bidrag.dokument.bestilling.model.kapitalinntektTyper
import no.nav.bidrag.dokument.bestilling.model.mapBarnIHusstandPerioder
import no.nav.bidrag.dokument.bestilling.model.mapSivilstand
import no.nav.bidrag.dokument.bestilling.model.tilBisysResultatkodeForBrev
import no.nav.bidrag.dokument.bestilling.model.tilRolletype
import no.nav.bidrag.dokument.bestilling.model.tilSaksbehandler
import no.nav.bidrag.dokument.bestilling.model.toSet
import no.nav.bidrag.domene.enums.behandling.TypeBehandling
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.beregning.Resultatkode.Companion.erAvslag
import no.nav.bidrag.domene.enums.beregning.Resultatkode.Companion.erDirekteAvslag
import no.nav.bidrag.domene.enums.beregning.Samværsklasse
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.rolle.Rolletype
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.enums.vedtak.Engangsbeløptype
import no.nav.bidrag.domene.enums.vedtak.Innkrevingstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.felles.grunnlag.BarnetilsynMedStønadPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.BaseGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.BostatusPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBarnIHusstand
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragsevne
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBidragspliktigesAndel
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningBoforhold
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSamværsfradrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningSumInntekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUnderholdskostnad
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUtgift
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningVoksneIHusstand
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.Grunnlagsreferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.InntektsrapporteringPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.Person
import no.nav.bidrag.transport.behandling.felles.grunnlag.SamværsperiodeGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonBidragsevnePeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSamværsfradragPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningBarnebidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningSærbidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnDelberegningBidragspliktigesAndelSærbidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnGrunnlagSomErReferertAv
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnGrunnlagSomErReferertFraGrunnlagsreferanseListe
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnOgKonverterGrunnlagSomErReferertAv
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnOgKonverterGrunnlagSomErReferertFraGrunnlagsreferanseListe
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnTotalInntektForRolleEllerIdent
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentAllePersoner
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentPersonMedReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.innholdTilObjekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.personIdent
import no.nav.bidrag.transport.behandling.felles.grunnlag.personObjekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.søknadsbarn
import no.nav.bidrag.transport.behandling.felles.grunnlag.tilGrunnlagstype
import no.nav.bidrag.transport.behandling.felles.grunnlag.utgiftsposter
import no.nav.bidrag.transport.behandling.vedtak.response.EngangsbeløpDto
import no.nav.bidrag.transport.behandling.vedtak.response.StønadsendringDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.transport.behandling.vedtak.response.særbidragsperiode
import no.nav.bidrag.transport.behandling.vedtak.response.typeBehandling
import org.springframework.stereotype.Service
import java.math.BigDecimal

val resultatkoderOpphør =
    listOf(
        Resultatkode.OPPHØR,
        Resultatkode.PARTEN_BER_OM_OPPHØR,
    )
val særbidragDirekteAvslagskoderSomInneholderUtgifter =
    listOf(Resultatkode.GODKJENT_BELØP_ER_LAVERE_ENN_FORSKUDDSSATS, Resultatkode.ALLE_UTGIFTER_ER_FORELDET)

val Stønadstype.erBidrag get() = listOf(Stønadstype.BIDRAG, Stønadstype.BIDRAG18AAR).contains(this)

@Service
class VedtakService(
    private val bidragVedtakConsumer: BidragVedtakConsumer,
    private val sjablongService: SjablongService,
    private val personService: PersonService,
) {
    fun hentVedtak(vedtakId: String): VedtakDto = bidragVedtakConsumer.hentVedtak(vedtakId) ?: fantIkkeVedtak(vedtakId)

    fun hentIdentSøknadsbarn(vedtakId: String): List<String> {
        val vedtakDto = hentVedtak(vedtakId)
        val vedtakBarnInfo = vedtakDto.grunnlagListe.søknadsbarn
        return vedtakBarnInfo.map { it.personIdent!! }
    }

    fun hentVedtakRoller(vedtakId: String): List<Person> {
        val vedtakDto = hentVedtak(vedtakId)
        return vedtakDto.grunnlagListe
            .hentAllePersoner()
            .filter { listOf(Grunnlagstype.PERSON_SØKNADSBARN, Grunnlagstype.PERSON_BIDRAGSPLIKTIG, Grunnlagstype.PERSON_SØKNADSBARN).contains(it.type) }
            .map { it.innholdTilObjekt<Person>() }
    }

    fun hentVedtakDetaljer(vedtakId: String): VedtakDetaljer {
        val vedtakDto = hentVedtak(vedtakId)
        val virkningstidspunktInfo = vedtakDto.hentVirkningstidspunkt()
        val soknadInfo = vedtakDto.hentSøknad()
        val vedtakBarnInfo = vedtakDto.grunnlagListe.søknadsbarn
        return VedtakDetaljer(
            årsakKode = virkningstidspunktInfo?.årsak,
            avslagsKode = virkningstidspunktInfo?.avslag,
            virkningstidspunkt = virkningstidspunktInfo?.virkningstidspunkt,
            mottattDato = soknadInfo.mottattDato,
            soktFraDato = soknadInfo.søktFraDato,
            vedtattDato = vedtakDto.opprettetTidspunkt.toLocalDate(),
            kilde = vedtakDto.kilde,
            vedtakType = vedtakDto.type,
            type = vedtakDto.typeBehandling,
            saksbehandlerInfo = vedtakDto.tilSaksbehandler(),
            engangsbelopType = vedtakDto.engangsbeløpListe.firstOrNull()?.type,
            stønadType = vedtakDto.stønadsendringListe.firstOrNull()?.type,
            søknadFra = soknadInfo.søktAv,
            gebyr = vedtakDto.hentGebyrInfo(),
            sivilstandPerioder = vedtakDto.grunnlagListe.mapSivilstand(),
            vedtakBarn = vedtakBarnInfo.distinctBy { it.personIdent }.sortedBy { it.personObjekt.fødselsdato }.map { mapVedtakBarn(it, vedtakDto) },
            barnIHusstandPerioder = vedtakDto.grunnlagListe.mapBarnIHusstandPerioder(),
        )
    }

    fun VedtakDto.hentGebyrInfo(): GebyrInfoDto {
        val engangsbeløpGebyrBm = engangsbeløpListe.find { it.type == Engangsbeløptype.GEBYR_MOTTAKER }
        val engangsbeløpGebyrBp = engangsbeløpListe.find { it.type == Engangsbeløptype.GEBYR_SKYLDNER }
        return GebyrInfoDto(
            bmGebyr = engangsbeløpGebyrBm?.beløp,
            bpGebyr = engangsbeløpGebyrBp?.beløp,
        )
    }

    fun hentGrunnlagForskudd(stonadsendringListe: List<StønadsendringDto>): List<ForskuddInntektgrensePeriode> {
        val erForskudd = stonadsendringListe.any { it.type == Stønadstype.FORSKUDD }
        if (!erForskudd) return emptyList()
        val perioder = stonadsendringListe.flatMap { it.periodeListe }
        val fraDato = perioder.minByOrNull { it.periode.fom }!!.periode.fom
        val tomDato =
            perioder
                .sortedByDescending { it.periode.til }
                .first()
                .periode.til
        return sjablongService.hentForskuddInntektgrensePerioder(fraDato.atDay(1), tomDato?.atEndOfMonth())
    }

    fun mapVedtakBarn(
        soknadBarn: BaseGrunnlag,
        vedtak: VedtakDto,
    ): VedtakBarn {
        val barnIdent = soknadBarn.personIdent!!
        val bostatusSøknadsbarn = vedtak.grunnlagListe.hentBarnIHusstandPerioderForBarn(barnIdent)
        val personInfo = personService.hentPerson(barnIdent)
        return VedtakBarn(
            fødselsnummer = barnIdent,
            navn = personInfo.visningsnavn,
            løpendeBidrag =
                vedtak.stønadsendringListe
                    .filter { it.type == Stønadstype.BIDRAG }
                    .find { it.kravhaver.verdi == soknadBarn.personIdent }
                    ?.periodeListe
                    ?.maxByOrNull { it.periode.fom }
                    ?.beløp,
            bostatusPerioder = bostatusSøknadsbarn?.bostatus ?: emptyList(),
            stønadsendringer = hentStønadsendringerForBarn(barnIdent, vedtak),
            engangsbeløper = hentEngagsbeløpForBarn(barnIdent, vedtak),
        )
    }

    fun hentEngagsbeløpForBarn(
        barnIdent: String,
        vedtakDto: VedtakDto,
    ): List<VedtakBarnEngangsbeløp> {
        val grunnlagListe = vedtakDto.grunnlagListe
        val engangsbeløpBarn = vedtakDto.engangsbeløpListe.filter { it.kravhaver.verdi == barnIdent }
        return engangsbeløpBarn.map { engangsbeløp ->
            val periode = vedtakDto.særbidragsperiode!!
            val sjablonForskudd =
                grunnlagListe
                    .filtrerBasertPåEgenReferanse(Grunnlagstype.SJABLON_SJABLONTALL)
                    .map { it.innholdTilObjekt<SjablonSjablontallPeriode>() }
                    .find { it.sjablon == SjablonTallNavn.FORSKUDDSSATS_BELØP }
                    ?.verdi ?: sjablongService.hentForsuddsatsForPeriode(periode.til).verdi
            val inntektsgrense = sjablongService.hentInntektGrenseForPeriode(periode.til)
            val resultatkode = Resultatkode.fraKode(engangsbeløp.resultatkode)
            val erDirekteAvslag = resultatkode!!.erDirekteAvslag()
            VedtakBarnEngangsbeløp(
                type = engangsbeløp.type,
                sjablon =
                    BrevSjablonVerdier(
                        forskuddSats = sjablonForskudd,
                        inntektsgrense = inntektsgrense,
                    ),
                periode = periode,
                erDirekteAvslag = erDirekteAvslag,
                medInnkreving = engangsbeløp.innkreving == Innkrevingstype.MED_INNKREVING,
                inntekter = grunnlagListe.mapInntekter(VedtakPeriodeReferanse(periode, vedtakDto.typeBehandling, engangsbeløp.grunnlagReferanseListe), BigDecimal.ZERO),
                særbidragBeregning =
                    if (engangsbeløp.type == Engangsbeløptype.SÆRBIDRAG) {
                        engangsbeløp.hentSærbidragBeregning(erDirekteAvslag, grunnlagListe)
                    } else {
                        null
                    },
            )
        }
    }

    fun EngangsbeløpDto.hentSærbidragBeregning(
        erDirekteAvslag: Boolean,
        grunnlagListe: List<GrunnlagDto>,
    ): SærbidragBeregning {
        val resultatkode = Resultatkode.fraKode(resultatkode)!!
        val erDirekteAvslagskoderSomInneholderUtgifter = særbidragDirekteAvslagskoderSomInneholderUtgifter.contains(resultatkode)
        return if (!erDirekteAvslag && !erDirekteAvslagskoderSomInneholderUtgifter) {
            val utgiftsposter = grunnlagListe.utgiftsposter
            val sluttberegning = grunnlagListe.filtrerBasertPåEgenReferanse(Grunnlagstype.SLUTTBEREGNING_SÆRBIDRAG).first().innholdTilObjekt<SluttberegningSærbidrag>()
            val delberegningUtgift = grunnlagListe.filtrerBasertPåEgenReferanse(Grunnlagstype.DELBEREGNING_UTGIFT).first().innholdTilObjekt<DelberegningUtgift>()
            val delberegning = grunnlagListe.finnDelberegningBidragspliktigesAndelSærbidrag(grunnlagReferanseListe)!!
            SærbidragBeregning(
                kravbeløp = utgiftsposter.sumOf { it.kravbeløp },
                godkjentbeløp = delberegningUtgift.sumGodkjent,
                andelProsent =
                    if (sluttberegning.resultatKode.erAvslag()) {
                        BigDecimal.ZERO
                    } else if (delberegning.andelProsent < BigDecimal.ONE) {
                        delberegning.andelProsent.multiply(BigDecimal(100))
                    } else {
                        delberegning.andelProsent
                    },
                resultat = sluttberegning.resultatBeløp ?: BigDecimal.ZERO,
                resultatKode = sluttberegning.resultatKode,
                beløpDirekteBetaltAvBp = delberegningUtgift.sumBetaltAvBp,
                inntekt =
                    SærbidragBeregning.Inntekt(
                        bmInntekt = grunnlagListe.finnTotalInntektForRolleEllerIdent(grunnlagReferanseListe, Rolletype.BIDRAGSMOTTAKER),
                        bpInntekt = grunnlagListe.finnTotalInntektForRolleEllerIdent(grunnlagReferanseListe, Rolletype.BIDRAGSPLIKTIG),
                        barnInntekt = grunnlagListe.finnTotalInntektForRolleEllerIdent(grunnlagReferanseListe, Rolletype.BARN),
                    ),
            )
        } else if (erDirekteAvslagskoderSomInneholderUtgifter) {
            val utgiftsposter = grunnlagListe.utgiftsposter
            val beregningResultatkode =
                grunnlagListe
                    .filtrerBasertPåEgenReferanse(Grunnlagstype.SLUTTBEREGNING_SÆRBIDRAG)
                    .firstOrNull()
                    ?.innholdTilObjekt<SluttberegningSærbidrag>()
                    ?.resultatKode ?: resultatkode
            val sumGodkjent =
                grunnlagListe
                    .filtrerBasertPåEgenReferanse(Grunnlagstype.DELBEREGNING_UTGIFT)
                    .firstOrNull()
                    ?.innholdTilObjekt<DelberegningUtgift>()
                    ?.sumGodkjent ?: grunnlagListe.utgiftsposter.sumOf { it.godkjentBeløp }
            SærbidragBeregning(
                kravbeløp = utgiftsposter.sumOf { it.kravbeløp },
                godkjentbeløp = sumGodkjent,
                resultat = BigDecimal.ZERO,
                resultatKode = beregningResultatkode,
            )
        } else {
            SærbidragBeregning(
                resultat = beløp ?: BigDecimal.ZERO,
                resultatKode = resultatkode,
            )
        }
    }

    fun VedtakDto.erDirekteAvslag(stønadsendringDto: StønadsendringDto): Boolean {
        if (hentVirkningstidspunkt()?.avslag != null) return true
        if (stønadsendringDto.periodeListe.size > 1) return false
        val periode = stønadsendringDto.periodeListe.first()
        val resultatKode = Resultatkode.fraKode(periode.resultatkode)
        return resultatKode?.erDirekteAvslag() == true
    }

    fun hentStønadsendringerForBarn(
        barnIdent: String,
        vedtakDto: VedtakDto,
    ): List<VedtakBarnStonad> {
        val grunnlagListe = vedtakDto.grunnlagListe
        val stønadsendringerBarn = vedtakDto.stønadsendringListe.filter { it.kravhaver.verdi == barnIdent }
        return stønadsendringerBarn.map { stønadsendring ->
            val erDirekteAvslag = vedtakDto.erDirekteAvslag(stønadsendring)
            val allePerioderAvslag = stønadsendring.periodeListe.all { Resultatkode.fraKode(it.resultatkode)?.erAvslag() == true }

            val erForskudd = stønadsendring.type == Stønadstype.FORSKUDD
            val vedtakPerioder =
                stønadsendring.periodeListe.filter { it.resultatkode != Resultatkode.OPPHØR.name }.mapNotNull { stønadperiode ->
                    val innteksgrense = sjablongService.hentInntektGrenseForPeriode(getLastDayOfPreviousMonth(stønadperiode.periode.til?.atEndOfMonth()))
                    val resultatKode = Resultatkode.fraKode(stønadperiode.resultatkode)
                    val referanse = VedtakPeriodeReferanse(stønadperiode.periode, resultatKode, vedtakDto.typeBehandling, stønadperiode.grunnlagReferanseListe)
                    val sluttberegning = grunnlagListe.finnOgKonverterGrunnlagSomErReferertFraGrunnlagsreferanseListe<SluttberegningBarnebidrag>(Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG, stønadperiode.grunnlagReferanseListe).firstOrNull()

                    val erAvslagUtenGrunnlag = sluttberegning?.innhold?.erResultatAvslag == true || resultatKode?.erDirekteAvslag() == true
                    if (erAvslagUtenGrunnlag && !erDirekteAvslag) return@mapNotNull null
                    VedtakPeriode(
                        fomDato = stønadperiode.periode.fom.atDay(1),
                        // TODO: Er dette riktig??
                        tomDato = stønadperiode.periode.til?.atEndOfMonth(),
                        // Bruker beløp 0.1 for å få alle beløpene i samme tabell hvis det er miks mellom perioder med avslag og innvilgelse
                        beløp =
                            stønadperiode.beløp?.let { if (it == BigDecimal.ZERO) BigDecimal("0.1") else it }
                                ?: if (erDirekteAvslag || allePerioderAvslag || erForskudd) BigDecimal.ZERO else BigDecimal("0.1"),
                        andelUnderhold = if (!erAvslagUtenGrunnlag) grunnlagListe.tilAndelUnderholdskostnadPeriode(referanse) else null,
                        underhold = if (!erAvslagUtenGrunnlag) grunnlagListe.tilUnderholdskostnadPeriode(referanse) else null,
                        bidragsevne = if (!erAvslagUtenGrunnlag) grunnlagListe.finnDelberegningBidragsevne(referanse) else null,
                        samvær = if (!erAvslagUtenGrunnlag) grunnlagListe.mapSamvær(referanse) else null,
                        resultatKode =
                            if (stønadsendring.type.erBidrag) {
                                grunnlagListe.tilBisysResultatkode(referanse, vedtakDto.type) ?: stønadperiode.resultatkode
                            } else {
                                resultatKode?.tilBisysResultatkodeForBrev(vedtakDto.type) ?: stønadperiode.resultatkode
                            },
                        inntekter = grunnlagListe.mapInntekter(referanse, innteksgrense),
                        inntektGrense = innteksgrense,
                        maksInntekt = sjablongService.hentMaksInntektForPeriode(getLastDayOfPreviousMonth(stønadperiode.periode.til?.atEndOfMonth())),
                    )
                }
            VedtakBarnStonad(
                type = stønadsendring.type,
                direkteAvslag = erDirekteAvslag,
                innkreving = stønadsendring.innkreving == Innkrevingstype.MED_INNKREVING,
                forskuddInntektgrensePerioder = hentGrunnlagForskudd(stønadsendringerBarn),
                vedtakPerioder = vedtakPerioder,
            )
        }
    }
}

fun List<GrunnlagDto>.tilBisysResultatkode(
    periode: VedtakPeriodeReferanse,
    type: Vedtakstype,
): String? {
    if (periode.resultatKode?.erDirekteAvslag() == true) return periode.resultatKode.tilBisysResultatkodeForBrev(type)
    val sluttberegning = finnOgKonverterGrunnlagSomErReferertFraGrunnlagsreferanseListe<SluttberegningBarnebidrag>(Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG, periode.grunnlagReferanseListe).first()
    return sluttberegning.innhold.bisysResultatkode
}

fun List<GrunnlagDto>.tilAndelUnderholdskostnadPeriode(periode: VedtakPeriodeReferanse): AndelUnderholdskostnadPeriode? {
    if (periode.typeBehandling != TypeBehandling.BIDRAG || periode.resultatKode?.erDirekteAvslag() == true) return null
    val bpsAndel = finnOgKonverterGrunnlagSomErReferertFraGrunnlagsreferanseListe<DelberegningBidragspliktigesAndel>(Grunnlagstype.DELBEREGNING_BIDRAGSPLIKTIGES_ANDEL, periode.grunnlagReferanseListe).firstOrNull() ?: return null
    val delberegningU =
        finnOgKonverterGrunnlagSomErReferertFraGrunnlagsreferanseListe<DelberegningUnderholdskostnad>(Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD, periode.grunnlagReferanseListe).firstOrNull() ?: return null
    return AndelUnderholdskostnadPeriode(
        periode = periode.periode,
        inntektBarn = finnTotalInntektForRolle(periode.grunnlagReferanseListe, Rolletype.BARN),
        barnEndeligInntekt = bpsAndel.innhold.barnEndeligInntekt,
        andelFaktor = bpsAndel.innhold.andelProsent,
        beløpUnderholdskostnad = delberegningU.innhold.underholdskostnad,
        beløpBpsAndel = bpsAndel.innhold.andelBeløp,
        inntektBP =
            finnTotalInntektForRolle(
                periode.grunnlagReferanseListe,
                Rolletype.BIDRAGSPLIKTIG,
            ),
        inntektBM =
            finnTotalInntektForRolle(
                periode.grunnlagReferanseListe,
                Rolletype.BIDRAGSMOTTAKER,
            ),
    )
}

fun List<GrunnlagDto>.tilUnderholdskostnadPeriode(periode: VedtakPeriodeReferanse): UnderholdskostnaderPeriode? {
    if (periode.typeBehandling != TypeBehandling.BIDRAG || periode.resultatKode?.erDirekteAvslag() == true) return null
    val delberegningU =
        finnOgKonverterGrunnlagSomErReferertFraGrunnlagsreferanseListe<DelberegningUnderholdskostnad>(Grunnlagstype.DELBEREGNING_UNDERHOLDSKOSTNAD, periode.grunnlagReferanseListe).firstOrNull() ?: return null
    val gjelder = hentPersonMedReferanse(delberegningU.gjelderBarnReferanse)!!
    val barnetilsynPeriode = finnOgKonverterGrunnlagSomErReferertAv<BarnetilsynMedStønadPeriode>(Grunnlagstype.BARNETILSYN_MED_STØNAD_PERIODE, delberegningU.grunnlag)
    return UnderholdskostnaderPeriode(
        periode = periode.periode,
        tilsynstype = barnetilsynPeriode.firstOrNull()?.innhold?.tilsynstype,
        skolealder = barnetilsynPeriode.firstOrNull()?.innhold?.skolealder,
        harBarnetilsyn = barnetilsynPeriode.isNotEmpty(),
        delberegning = delberegningU.innhold,
        rolletype = gjelder.type.tilRolletype(),
        gjelderIdent = gjelder.personIdent!!,
    )
}

fun List<GrunnlagDto>.finnDelberegningBidragsevne(periode: VedtakPeriodeReferanse): BidragsevnePeriode? {
    if (periode.typeBehandling != TypeBehandling.BIDRAG || periode.resultatKode?.erDirekteAvslag() == true) return null
    val sluttberegning = finnOgKonverterGrunnlagSomErReferertFraGrunnlagsreferanseListe<SluttberegningBarnebidrag>(Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG, periode.grunnlagReferanseListe).first()
    val delberegningBidragsevne = finnOgKonverterGrunnlagSomErReferertAv<DelberegningBidragsevne>(Grunnlagstype.DELBEREGNING_BIDRAGSEVNE, sluttberegning.grunnlag).first()
    val delberegningBoforhold = finnOgKonverterGrunnlagSomErReferertAv<DelberegningBoforhold>(Grunnlagstype.DELBEREGNING_BOFORHOLD, delberegningBidragsevne.grunnlag).first()
    val delberegningVoksneIHusstand = finnOgKonverterGrunnlagSomErReferertAv<DelberegningVoksneIHusstand>(Grunnlagstype.DELBEREGNING_VOKSNE_I_HUSSTAND, delberegningBoforhold.grunnlag).first()
    val delberegningBarnIHusstanden = finnOgKonverterGrunnlagSomErReferertAv<DelberegningBarnIHusstand>(Grunnlagstype.DELBEREGNING_BARN_I_HUSSTAND, delberegningBoforhold.grunnlag).first()

    val bosstatusPeridoer = finnOgKonverterGrunnlagSomErReferertAv<BostatusPeriode>(Grunnlagstype.BOSTATUS_PERIODE, delberegningBoforhold.grunnlag)
    val sjablonBidragsevne =
        finnOgKonverterGrunnlagSomErReferertFraGrunnlagsreferanseListe<SjablonBidragsevnePeriode>(Grunnlagstype.SJABLON_BIDRAGSEVNE, delberegningBidragsevne.grunnlag.grunnlagsreferanseListe)
            .firstOrNull() ?: return null
    val sjablonUnderholdEgnebarnIHusstand =
        finnSjablonMedType(SjablonTallNavn.UNDERHOLD_EGNE_BARN_I_HUSSTAND_BELØP, delberegningBidragsevne.grunnlag.grunnlagsreferanseListe)
            ?: return null

    val antallBarnDeltBossted = bosstatusPeridoer.count { it.innhold.bostatus == Bostatuskode.DELT_BOSTED }
    val sjabloner = finnOgKonverterGrunnlagSomErReferertAv<SjablonSjablontallPeriode>(Grunnlagstype.SJABLON_SJABLONTALL, sluttberegning.grunnlag)
    val sjablonKlasseFradrag =
        sjabloner
            .find { it.innhold.sjablon == SjablonTallNavn.PERSONFRADRAG_KLASSE1_BELØP || it.innhold.sjablon == SjablonTallNavn.PERSONFRADRAG_KLASSE2_BELØP }
    return BidragsevnePeriode(
        periode = periode.periode,
        beløpBidrag = sluttberegning.innhold.resultatBeløp ?: BigDecimal.ZERO,
        sjabloner =
            BidragsevnePeriode.BidragsevneSjabloner(
                beløpKlassfradrag = sjablonKlasseFradrag!!.innhold.verdi,
                underholdBeløp = sjablonBidragsevne.innhold.underholdBeløp,
                boutgiftBeløp = sjablonBidragsevne.innhold.boutgiftBeløp,
                beløpMinstefradrag =
                    sjabloner
                        .find { it.innhold.sjablon == SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELØP }!!
                        .innhold.verdi,
                beløpUnderholdEgneBarnIHusstanden =
                    sjabloner
                        .find { it.innhold.sjablon == SjablonTallNavn.UNDERHOLD_EGNE_BARN_I_HUSSTAND_BELØP }!!
                        .innhold.verdi,
            ),
        bidragsevne = delberegningBidragsevne.innhold.beløp,
        underholdEgneBarnIHusstand =
            UnderholdEgneBarnIHusstand(
                årsbeløp = delberegningBidragsevne.innhold.underholdBarnEgenHusstand,
                sjablon = sjablonUnderholdEgnebarnIHusstand.verdi,
                antallBarnIHusstanden = delberegningBarnIHusstanden.innhold.antallBarn,
                antallBarnDeltBossted = antallBarnDeltBossted,
            ),
        harFullEvne = !sluttberegning.innhold.bidragJustertNedTilEvne,
        harDelvisEvne = sluttberegning.innhold.bidragJustertNedTilEvne && sluttberegning.innhold.resultatBeløp!! > BigDecimal.ZERO,
        inntektBP = finnTotalInntektForRolle(periode.grunnlagReferanseListe, Rolletype.BIDRAGSPLIKTIG),
        borMedAndreVoksne = delberegningVoksneIHusstand.innhold.borMedAndreVoksne,
        skatt =
            Skatt(
                sumSkattFaktor = delberegningBidragsevne.innhold.skatt.sumSkattFaktor,
                sumSkatt = delberegningBidragsevne.innhold.skatt.sumSkatt,
                skattAlminneligInntekt = delberegningBidragsevne.innhold.skatt.skattAlminneligInntekt,
                trinnskatt = delberegningBidragsevne.innhold.skatt.trinnskatt,
                trygdeavgift = delberegningBidragsevne.innhold.skatt.trygdeavgift,
            ),
    )
}

fun List<GrunnlagDto>.finnTotalInntektForRolle(
    grunnlagsreferanseListe: List<Grunnlagsreferanse>,
    rolletype: Rolletype? = null,
): BigDecimal {
    val sluttberegning =
        finnSluttberegningIReferanser(grunnlagsreferanseListe)
            ?: return BigDecimal.ZERO
    val gjelderReferanse = hentAllePersoner().find { it.type == rolletype?.tilGrunnlagstype() }?.referanse
    val delberegningSumInntekter = finnGrunnlagSomErReferertAv(Grunnlagstype.DELBEREGNING_SUM_INNTEKT, sluttberegning)
    val delberegningSumInntektForRolle =
        if (gjelderReferanse.isNullOrEmpty()) {
            delberegningSumInntekter.firstOrNull()
        } else {
            delberegningSumInntekter.find {
                it.gjelderReferanse ==
                    gjelderReferanse
            }
        }
    return delberegningSumInntektForRolle?.innholdTilObjekt<DelberegningSumInntekt>()?.totalinntekt
        ?: BigDecimal.ZERO
}

fun List<GrunnlagDto>.mapSamvær(periode: VedtakPeriodeReferanse): Samværsperiode? =
    if (periode.typeBehandling == TypeBehandling.BIDRAG && periode.resultatKode?.erDirekteAvslag() == false) {
        Samværsperiode(
            samværsfradragBeløp = finnSamværsfradrag(periode.grunnlagReferanseListe),
            samværsklasse = finnSamværsklasse(periode.grunnlagReferanseListe),
            aldersgruppe = finnSamværAldersgruppe(periode.grunnlagReferanseListe),
            periode = periode.periode,
        )
    } else {
        null
    }

fun List<GrunnlagDto>.finnSamværsklasse(
    grunnlagsreferanseListe: List<Grunnlagsreferanse>,
): Samværsklasse {
    val samværsperiode =
        finnGrunnlagSomErReferertFraGrunnlagsreferanseListe(
            Grunnlagstype.SAMVÆRSPERIODE,
            grunnlagsreferanseListe,
        ).firstOrNull()

    return samværsperiode!!.innholdTilObjekt<SamværsperiodeGrunnlag>().samværsklasse
}

fun List<GrunnlagDto>.finnSamværAldersgruppe(grunnlagsreferanseListe: List<Grunnlagsreferanse>): Pair<Int, Int?>? {
    val samværsfradrag =
        finnGrunnlagSomErReferertFraGrunnlagsreferanseListe(Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG, grunnlagsreferanseListe).firstOrNull()

    val sjablonSamværsfradrag = finnOgKonverterGrunnlagSomErReferertAv<SjablonSamværsfradragPeriode>(Grunnlagstype.SJABLON_SAMVARSFRADRAG, samværsfradrag!!).firstOrNull()
    val alderTom = sjablonSamværsfradrag?.innhold?.alderTom ?: return null
    val alderFom =
        when (alderTom) {
            5 -> 0
            99 -> 19
            14 -> 11
            18 -> 13
            else -> alderTom - 4
        }
    return Pair(alderFom, if (alderTom == 99) null else alderTom)
}

fun List<GrunnlagDto>.finnSamværsfradrag(grunnlagsreferanseListe: List<Grunnlagsreferanse>): BigDecimal {
    val sluttberegning =
        finnSluttberegningIReferanser(grunnlagsreferanseListe)
            ?: return BigDecimal.ZERO
    val samværsfradrag =
        finnGrunnlagSomErReferertAv(Grunnlagstype.DELBEREGNING_SAMVÆRSFRADRAG, sluttberegning).firstOrNull()
    return samværsfradrag?.innholdTilObjekt<DelberegningSamværsfradrag>()?.beløp
        ?: BigDecimal.ZERO
}

fun List<GrunnlagDto>.mapInntekter(
    periode: VedtakPeriodeReferanse,
    innteksgrense: BigDecimal,
): List<InntektPeriode> {
    val nettoKapitalInntekt = hentNettoKapitalinntektForRolle(periode, innteksgrense)

    val inntekter =
        hentInntekterForPeriode(periode)
            .map { inntektGrunnlag ->
                val inntekt = inntektGrunnlag.innholdTilObjekt<InntektsrapporteringPeriode>()
                if (kapitalinntektTyper.contains(inntekt.inntektsrapportering)) return@map null
                val gjelderPersonGrunnlag = hentPersonMedReferanse(inntektGrunnlag.gjelderReferanse)!!
                val gjelderPerson = gjelderPersonGrunnlag.personObjekt
                InntektPeriode(
                    inntektPerioder = inntekt.periode.toSet(),
                    inntektOpprinneligPerioder = inntekt.opprinneligPeriode.toSet(),
                    periode = periode.periode,
                    typer = inntekt.inntektsrapportering.toSet(),
                    beløpÅr = inntekt.opprinneligPeriode?.fom?.year ?: inntekt.periode.fom.year,
                    rolle = gjelderPersonGrunnlag.type.tilRolletype(),
                    fødselsnummer = gjelderPerson.ident!!.verdi,
                    beløp = inntekt.beløp,
                    innteksgrense = innteksgrense,
                )
            }.filterNotNull()
            .sammenstillDeMedSammeBeskrivelse() + nettoKapitalInntekt

    return inntekter + hentTotalInntektForPeriode(periode, innteksgrense)
}

fun List<AndelUnderholdskostnadPeriode>.sammenstillDeMedSammeVerdiAndelUnderhold() =
    this
        .grupperPerioder()
        .map {
            it.reduce { acc, underhold ->
                underhold.copy(
                    periode = ÅrMånedsperiode(acc.periode.fom, underhold.periode.til),
                )
            }
        }.sortedBy { it.periode.fom }

fun List<UnderholdskostnaderPeriode>.sammenstillDeMedSammeVerdiUnderhold() =
    this
        .groupBy { it.rolletype }
        .flatMap { (_, underholdList) ->
            underholdList.grupperPerioder().map {
                it.reduce { acc, underhold ->
                    underhold.copy(
                        periode = ÅrMånedsperiode(acc.periode.fom, underhold.periode.til),
                    )
                }
            }
        }.sortedBy { it.periode.fom }

fun List<InntektPeriode>.sammenstillDeMedSammeVerdiInntekter() =
    this
        .groupBy { it.rolle }
        .flatMap { (_, rolleInntektList) ->
            rolleInntektList.grupperPerioder().map {
                it.reduce { acc, inntekt ->
                    inntekt.copy(
                        periode = ÅrMånedsperiode(acc.periode.fom, inntekt.periode.til),
                        innteksgrense = maxOf(acc.innteksgrense, inntekt.innteksgrense),
                        beløpÅr = acc.beløpÅr,
                        inntektPerioder = acc.inntektPerioder,
                    )
                }
            }
        }.sortedWith(compareBy({ it.rolle }, { it.periode.fom }, { it.beløp }))

fun <T : DataPeriode> List<T>.grupperPerioder(): List<List<T>> {
    if (this.isEmpty()) return emptyList()

    val sortedList = this.sortedBy { it.kopierTilGenerisk().toString() }
    val result = mutableListOf<List<T>>()
    var currentGroup = mutableListOf<T>()
    for (periode in sortedList) {
        if (currentGroup.isEmpty() || currentGroup.last().erLik(periode) && currentGroup.last().periode.til == periode.periode.fom) {
            currentGroup.add(periode)
        } else {
            result.add(currentGroup)
            currentGroup = mutableListOf(periode)
        }
    }
    if (currentGroup.isNotEmpty()) {
        result.add(currentGroup)
    }

    return result
}

fun List<Samværsperiode>.sammenstillDeMedSammeVerdi() =
    this
        .grupperPerioder()
        .map { samværList ->
            samværList.reduce { acc, samvær ->
                Samværsperiode(
                    samværsfradragBeløp = acc.samværsfradragBeløp,
                    samværsklasse = acc.samværsklasse,
                    aldersgruppe = acc.aldersgruppe,
                    periode = ÅrMånedsperiode(acc.periode.fom, samvær.periode.til),
                )
            }
        }.sortedBy { it.periode.fom }

fun List<InntektPeriode>.sammenstillDeMedSammeBeskrivelse() =
    groupBy { Pair(it.beskrivelse, it.rolle) }.map { (_, inntekter) ->
        inntekter.reduce { acc, inntekt ->
            InntektPeriode(
                inntektPerioder = acc.inntektPerioder + inntekt.inntektPerioder,
                inntektOpprinneligPerioder = acc.inntektOpprinneligPerioder + inntekt.inntektOpprinneligPerioder,
                periode = acc.periode,
                typer = acc.typer + inntekt.typer,
                periodeTotalinntekt = acc.periodeTotalinntekt,
                nettoKapitalInntekt = acc.nettoKapitalInntekt,
                beløpÅr = acc.beløpÅr,
                fødselsnummer = acc.fødselsnummer,
                beløp = acc.beløp + inntekt.beløp,
                rolle = acc.rolle,
                innteksgrense = acc.innteksgrense,
            )
        }
    }

fun List<GrunnlagDto>.finnSluttberegningIReferanser(grunnlagsreferanseListe: List<Grunnlagsreferanse>) =
    find {
        listOf(
            Grunnlagstype.SLUTTBEREGNING_FORSKUDD,
            Grunnlagstype.SLUTTBEREGNING_SÆRBIDRAG,
            Grunnlagstype.SLUTTBEREGNING_BARNEBIDRAG,
        ).contains(it.type) &&
            grunnlagsreferanseListe.contains(it.referanse)
    }
