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
import no.nav.bidrag.dokument.bestilling.model.mapBarnIHusstandPerioder
import no.nav.bidrag.dokument.bestilling.model.mapSivilstand
import no.nav.bidrag.dokument.bestilling.model.tilRolletype
import no.nav.bidrag.dokument.bestilling.model.tilSaksbehandler
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
class VedtakService(private val bidragVedtakConsumer: BidragVedtakConsumer, private val sjablongService: SjablongService, private val personService: PersonService) {
    fun hentVedtak(vedtakId: String): VedtakDto {
        return bidragVedtakConsumer.hentVedtak(vedtakId) ?: fantIkkeVedtak(vedtakId)
    }

    fun hentVedtakSoknadsbarnFodselsnummer(vedtakId: String): List<String> {
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
            vedtakBarn = vedtakBarnInfo.distinctBy { it.personIdent }.sortedBy { it.personIdent }.map { mapVedtakBarn(it, vedtakDto) },
            barnIHustandPerioder = vedtakDto.grunnlagListe.mapBarnIHusstandPerioder(),
        )
    }

    fun hentGrunnlagForskudd(stonadsendringListe: List<StønadsendringDto>): List<ForskuddInntektgrensePeriode> {
        val erForskudd = stonadsendringListe.any { it.type == Stønadstype.FORSKUDD }
        if (!erForskudd) return emptyList()
        val perioder = stonadsendringListe.flatMap { it.periodeListe }
        val fraDato = perioder.minByOrNull { it.periode.fom }!!.periode.fom
        val tomDato = perioder.sortedByDescending { it.periode.til }.first().periode.til
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
            fodselsnummer = barnIdent,
            navn = personInfo.visningsnavn,
            bostatusPerioder = bostatusSøknadsbarn.bostatus,
            stonader = hentStonader(barnIdent, vedtak),
        )
    }

    fun hentStonader(
        barnFodselsnummer: String,
        vedtakDto: VedtakDto,
    ): List<VedtakBarnStonad> {
        val grunnlagListe = vedtakDto.grunnlagListe
        val stonadListeBarn = vedtakDto.stønadsendringListe.filter { it.kravhaver.verdi == barnFodselsnummer }
        return stonadListeBarn.map { vedtak ->
            val vedtakPerioder =
                vedtak.periodeListe.map { vedtakPeriode ->
                    val nettoKapitalInntekt = grunnlagListe.hentNettoKapitalinntektForRolle(vedtakPeriode, grunnlagListe.bidragsmottaker!!)

                    val nettoKapitalInntekter = nettoKapitalInntekt?.let { listOf(it) } ?: emptyList()
                    val inntekter =
                        grunnlagListe.hentInntekterForPeriode(vedtakPeriode).map { inntektGrunnlag ->
                            val inntekt = inntektGrunnlag.innholdTilObjekt<InntektsrapporteringPeriode>()
                            if (kapitalinntektTyper.contains(inntekt.inntektsrapportering)) return@map null
                            val gjelderPersonGrunnlag = grunnlagListe.hentPersonMedReferanse(inntektGrunnlag.gjelderReferanse)!!
                            val gjelderPerson = gjelderPersonGrunnlag.personObjekt
                            InntektPeriode(
                                inntektPeriode = inntekt.periode,
                                periode = vedtakPeriode.periode,
                                type = inntekt.inntektsrapportering,
                                beløpÅr = inntekt.periode.fom.year,
                                rolle = gjelderPersonGrunnlag.type.tilRolletype(),
                                fødselsnummer = gjelderPerson.ident!!.verdi,
                                beløp = inntekt.beløp,
                            )
                        }.filterNotNull() + nettoKapitalInntekter

                    VedtakPeriode(
                        fomDato = vedtakPeriode.periode.fom.atDay(1),
                        // TODO: Er dette riktig??
                        tomDato = vedtakPeriode.periode.til?.atEndOfMonth(),
                        beløp = vedtakPeriode.beløp ?: BigDecimal(0),
                        resultatKode = Resultatkode.fraKode(vedtakPeriode.resultatkode)?.legacyKode ?: vedtakPeriode.resultatkode,
                        inntekter = inntekter + grunnlagListe.hentTotalInntektForPeriode(vedtakPeriode),
                        inntektGrense = sjablongService.hentInntektGrenseForPeriode(getLastDayOfPreviousMonth(vedtakPeriode.periode.til?.atEndOfMonth())),
                        maksInntekt = sjablongService.hentMaksInntektForPeriode(getLastDayOfPreviousMonth(vedtakPeriode.periode.til?.atEndOfMonth())),
                    )
                }
            VedtakBarnStonad(
                type = vedtak.type,
                innkreving = vedtak.innkreving == Innkrevingstype.MED_INNKREVING,
                forskuddInntektgrensePerioder = hentGrunnlagForskudd(stonadListeBarn),
                vedtakPerioder = vedtakPerioder,
            )
        }
    }
}
