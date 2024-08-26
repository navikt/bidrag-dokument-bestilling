package no.nav.bidrag.dokument.bestilling.tjenester

import no.nav.bidrag.dokument.bestilling.bestilling.dto.BrevSjablonVerdier
import no.nav.bidrag.dokument.bestilling.bestilling.dto.ForskuddInntektgrensePeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.InntektPeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.SærbidragBeregning
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakBarn
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakBarnEngangsbeløp
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakBarnStonad
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakDetaljer
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakPeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakPeriodeReferanse
import no.nav.bidrag.dokument.bestilling.consumer.BidragVedtakConsumer
import no.nav.bidrag.dokument.bestilling.model.fantIkkeVedtak
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
import no.nav.bidrag.dokument.bestilling.model.tilRolletype
import no.nav.bidrag.dokument.bestilling.model.tilSaksbehandler
import no.nav.bidrag.dokument.bestilling.model.toSet
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.beregning.Resultatkode.Companion.erAvslagEllerOpphør
import no.nav.bidrag.domene.enums.beregning.Resultatkode.Companion.erDirekteAvslag
import no.nav.bidrag.domene.enums.beregning.Resultatkode.Companion.tilBisysResultatkode
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.rolle.Rolletype
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import no.nav.bidrag.domene.enums.vedtak.Engangsbeløptype
import no.nav.bidrag.domene.enums.vedtak.Innkrevingstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.transport.behandling.felles.grunnlag.BaseGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.DelberegningUtgift
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.felles.grunnlag.InntektsrapporteringPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.Person
import no.nav.bidrag.transport.behandling.felles.grunnlag.SjablonSjablontallPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.SluttberegningSærbidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.filtrerBasertPåEgenReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnDelberegningBidragspliktigesAndelSærbidrag
import no.nav.bidrag.transport.behandling.felles.grunnlag.finnTotalInntektForRolleEllerIdent
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentAllePersoner
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentPersonMedReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.innholdTilObjekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.personIdent
import no.nav.bidrag.transport.behandling.felles.grunnlag.personObjekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.søknadsbarn
import no.nav.bidrag.transport.behandling.felles.grunnlag.utgiftsposter
import no.nav.bidrag.transport.behandling.vedtak.response.EngangsbeløpDto
import no.nav.bidrag.transport.behandling.vedtak.response.StønadsendringDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
import no.nav.bidrag.transport.behandling.vedtak.response.særbidragsperiode
import no.nav.bidrag.transport.behandling.vedtak.response.typeBehandling
import org.springframework.stereotype.Service
import java.math.BigDecimal

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
            sivilstandPerioder = vedtakDto.grunnlagListe.mapSivilstand(),
            vedtakBarn = vedtakBarnInfo.distinctBy { it.personIdent }.sortedBy { it.personObjekt.fødselsdato }.map { mapVedtakBarn(it, vedtakDto) },
            barnIHusstandPerioder = vedtakDto.grunnlagListe.mapBarnIHusstandPerioder(),
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
                    .filtrerBasertPåEgenReferanse(Grunnlagstype.SJABLON)
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
                inntekter = grunnlagListe.mapInntekter(VedtakPeriodeReferanse(periode, vedtakDto.typeBehandling, engangsbeløp.grunnlagReferanseListe)),
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
        val erResultatGodkjentbeløpLavereEnnForskuddssats = resultatkode == Resultatkode.GODKJENT_BELØP_ER_LAVERE_ENN_FORSKUDDSSATS
        return if (!erDirekteAvslag && !erResultatGodkjentbeløpLavereEnnForskuddssats) {
            val utgiftsposter = grunnlagListe.utgiftsposter
            val sluttberegning = grunnlagListe.filtrerBasertPåEgenReferanse(Grunnlagstype.SLUTTBEREGNING_SÆRBIDRAG).first().innholdTilObjekt<SluttberegningSærbidrag>()
            val delberegningUtgift = grunnlagListe.filtrerBasertPåEgenReferanse(Grunnlagstype.DELBEREGNING_UTGIFT).first().innholdTilObjekt<DelberegningUtgift>()
            val delberegning = grunnlagListe.finnDelberegningBidragspliktigesAndelSærbidrag(grunnlagReferanseListe)!!
            SærbidragBeregning(
                kravbeløp = utgiftsposter.sumOf { it.kravbeløp },
                godkjentbeløp = delberegningUtgift.sumGodkjent,
                andelProsent =
                    if (sluttberegning.resultatKode.erAvslagEllerOpphør()) {
                        BigDecimal.ZERO
                    } else if (delberegning.andelProsent < BigDecimal.ONE) {
                        delberegning.andelProsent.multiply(BigDecimal(100))
                    } else {
                        delberegning.andelProsent
                    },
                resultat = sluttberegning.resultatBeløp,
                resultatKode = sluttberegning.resultatKode,
                beløpDirekteBetaltAvBp = delberegningUtgift.sumBetaltAvBp,
                inntekt =
                    SærbidragBeregning.Inntekt(
                        bmInntekt = grunnlagListe.finnTotalInntektForRolleEllerIdent(grunnlagReferanseListe, Rolletype.BIDRAGSMOTTAKER),
                        bpInntekt = grunnlagListe.finnTotalInntektForRolleEllerIdent(grunnlagReferanseListe, Rolletype.BIDRAGSPLIKTIG),
                        barnInntekt = grunnlagListe.finnTotalInntektForRolleEllerIdent(grunnlagReferanseListe, Rolletype.BARN),
                    ),
            )
        } else if (erResultatGodkjentbeløpLavereEnnForskuddssats) {
            val utgiftsposter = grunnlagListe.utgiftsposter
            val sluttberegning = grunnlagListe.filtrerBasertPåEgenReferanse(Grunnlagstype.SLUTTBEREGNING_SÆRBIDRAG).first().innholdTilObjekt<SluttberegningSærbidrag>()
            val delberegningUtgift = grunnlagListe.filtrerBasertPåEgenReferanse(Grunnlagstype.DELBEREGNING_UTGIFT).first().innholdTilObjekt<DelberegningUtgift>()
            SærbidragBeregning(
                kravbeløp = utgiftsposter.sumOf { it.kravbeløp },
                godkjentbeløp = delberegningUtgift.sumGodkjent,
                resultat = sluttberegning.resultatBeløp,
                resultatKode = sluttberegning.resultatKode,
            )
        } else {
            SærbidragBeregning(
                resultat = beløp ?: BigDecimal.ZERO,
                resultatKode = resultatkode,
            )
        }
    }

    fun hentStønadsendringerForBarn(
        barnIdent: String,
        vedtakDto: VedtakDto,
    ): List<VedtakBarnStonad> {
        val grunnlagListe = vedtakDto.grunnlagListe
        val stønadsendringerBarn = vedtakDto.stønadsendringListe.filter { it.kravhaver.verdi == barnIdent }
        return stønadsendringerBarn.map { stønadsendring ->
            val vedtakPerioder =
                stønadsendring.periodeListe.map { stønadperiode ->

                    val resultatKode = Resultatkode.fraKode(stønadperiode.resultatkode)
                    VedtakPeriode(
                        fomDato = stønadperiode.periode.fom.atDay(1),
                        // TODO: Er dette riktig??
                        tomDato = stønadperiode.periode.til?.atEndOfMonth(),
                        beløp = stønadperiode.beløp ?: BigDecimal.ZERO,
                        resultatKode = resultatKode?.tilBisysResultatkode(vedtakDto.type) ?: stønadperiode.resultatkode,
                        inntekter = grunnlagListe.mapInntekter(VedtakPeriodeReferanse(stønadperiode.periode, vedtakDto.typeBehandling, stønadperiode.grunnlagReferanseListe)),
                        inntektGrense = sjablongService.hentInntektGrenseForPeriode(getLastDayOfPreviousMonth(stønadperiode.periode.til?.atEndOfMonth())),
                        maksInntekt = sjablongService.hentMaksInntektForPeriode(getLastDayOfPreviousMonth(stønadperiode.periode.til?.atEndOfMonth())),
                    )
                }
            VedtakBarnStonad(
                type = stønadsendring.type,
                innkreving = stønadsendring.innkreving == Innkrevingstype.MED_INNKREVING,
                forskuddInntektgrensePerioder = hentGrunnlagForskudd(stønadsendringerBarn),
                vedtakPerioder = vedtakPerioder,
            )
        }
    }
}

fun List<GrunnlagDto>.mapInntekter(periode: VedtakPeriodeReferanse): List<InntektPeriode> {
    val nettoKapitalInntekt = hentNettoKapitalinntektForRolle(periode)

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
                )
            }.filterNotNull()
            .sammenstillDeMedSammeBeskrivelse() + nettoKapitalInntekt

    return inntekter + hentTotalInntektForPeriode(periode)
}

fun List<InntektPeriode>.sammenstillDeMedSammeBeskrivelse() =
    groupBy { it.beskrivelse }.map { (_, inntekter) ->
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
            )
        }
    }
