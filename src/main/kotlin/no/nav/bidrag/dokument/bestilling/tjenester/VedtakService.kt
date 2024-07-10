package no.nav.bidrag.dokument.bestilling.tjenester

import no.nav.bidrag.dokument.bestilling.bestilling.dto.ForskuddInntektgrensePeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.InntektPeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakBarn
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakBarnStonad
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakDetaljer
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakPeriode
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
import no.nav.bidrag.dokument.bestilling.model.legacyKodeBrev
import no.nav.bidrag.dokument.bestilling.model.mapBarnIHusstandPerioder
import no.nav.bidrag.dokument.bestilling.model.mapSivilstand
import no.nav.bidrag.dokument.bestilling.model.tilRolletype
import no.nav.bidrag.dokument.bestilling.model.tilSaksbehandler
import no.nav.bidrag.dokument.bestilling.model.toList
import no.nav.bidrag.dokument.bestilling.model.toSet
import no.nav.bidrag.domene.enums.beregning.Resultatkode
import no.nav.bidrag.domene.enums.vedtak.Innkrevingstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.transport.behandling.felles.grunnlag.BaseGrunnlag
import no.nav.bidrag.transport.behandling.felles.grunnlag.InntektsrapporteringPeriode
import no.nav.bidrag.transport.behandling.felles.grunnlag.bidragsmottaker
import no.nav.bidrag.transport.behandling.felles.grunnlag.hentPersonMedReferanse
import no.nav.bidrag.transport.behandling.felles.grunnlag.innholdTilObjekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.personIdent
import no.nav.bidrag.transport.behandling.felles.grunnlag.personObjekt
import no.nav.bidrag.transport.behandling.felles.grunnlag.søknadsbarn
import no.nav.bidrag.transport.behandling.vedtak.response.StønadsendringDto
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
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
        )
    }

    fun hentStønadsendringerForBarn(
        barnIdent: String,
        vedtakDto: VedtakDto,
    ): List<VedtakBarnStonad> {
        val grunnlagListe = vedtakDto.grunnlagListe
        val stønadsendringerBarn = vedtakDto.stønadsendringListe.filter { it.kravhaver.verdi == barnIdent }
        return stønadsendringerBarn.map { stønadsendring ->
            val vedtakPerioder =
                stønadsendring.periodeListe.map { vedtakPeriode ->
                    val nettoKapitalInntekt = grunnlagListe.hentNettoKapitalinntektForRolle(vedtakPeriode, grunnlagListe.bidragsmottaker!!)

                    val inntekter =
                        grunnlagListe
                            .hentInntekterForPeriode(vedtakPeriode)
                            .map { inntektGrunnlag ->
                                val inntekt = inntektGrunnlag.innholdTilObjekt<InntektsrapporteringPeriode>()
                                if (kapitalinntektTyper.contains(inntekt.inntektsrapportering)) return@map null
                                val gjelderPersonGrunnlag = grunnlagListe.hentPersonMedReferanse(inntektGrunnlag.gjelderReferanse)!!
                                val gjelderPerson = gjelderPersonGrunnlag.personObjekt
                                InntektPeriode(
                                    inntektPerioder = inntekt.periode.toSet(),
                                    inntektOpprinneligPerioder = inntekt.opprinneligPeriode.toSet(),
                                    periode = vedtakPeriode.periode,
                                    typer = inntekt.inntektsrapportering.toSet(),
                                    beløpÅr = inntekt.opprinneligPeriode?.fom?.year ?: inntekt.periode.fom.year,
                                    rolle = gjelderPersonGrunnlag.type.tilRolletype(),
                                    fødselsnummer = gjelderPerson.ident!!.verdi,
                                    beløp = inntekt.beløp,
                                )
                            }.filterNotNull()
                            .sammenstillDeMedSammeBeskrivelse() + nettoKapitalInntekt.toList()

                    val resultatKode = Resultatkode.fraKode(vedtakPeriode.resultatkode)
                    VedtakPeriode(
                        fomDato = vedtakPeriode.periode.fom.atDay(1),
                        // TODO: Er dette riktig??
                        tomDato = vedtakPeriode.periode.til?.atEndOfMonth(),
                        beløp = vedtakPeriode.beløp ?: BigDecimal.ZERO,
                        resultatKode = resultatKode?.legacyKodeBrev ?: vedtakPeriode.resultatkode,
                        inntekter = inntekter + grunnlagListe.hentTotalInntektForPeriode(vedtakPeriode),
                        inntektGrense = sjablongService.hentInntektGrenseForPeriode(getLastDayOfPreviousMonth(vedtakPeriode.periode.til?.atEndOfMonth())),
                        maksInntekt = sjablongService.hentMaksInntektForPeriode(getLastDayOfPreviousMonth(vedtakPeriode.periode.til?.atEndOfMonth())),
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

fun Resultatkode.tilLegacy() =
    when (this) {
        Resultatkode.AVSLAG_OVER_18_ÅR -> "OHS"
        Resultatkode.AVSLAG_HØY_INNTEKT -> "OHI"
        Resultatkode.AVSLAG_IKKE_REGISTRERT_PÅ_ADRESSE -> "OIO"
        else -> this?.legacyKodeBrev
    }
