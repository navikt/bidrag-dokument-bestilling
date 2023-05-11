package no.nav.bidrag.dokument.bestilling.model

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.behandling.felles.dto.vedtak.VedtakDto
import no.nav.bidrag.behandling.felles.enums.GrunnlagType
import no.nav.bidrag.behandling.felles.enums.Rolle
import no.nav.bidrag.behandling.felles.grunnlag.BarnIHusstand
import no.nav.bidrag.behandling.felles.grunnlag.BarnInfo
import no.nav.bidrag.behandling.felles.grunnlag.Bostatus
import no.nav.bidrag.behandling.felles.grunnlag.PersonInfo
import no.nav.bidrag.behandling.felles.grunnlag.Sivilstand
import no.nav.bidrag.behandling.felles.grunnlag.SoknadInfo
import no.nav.bidrag.behandling.felles.grunnlag.SoknadsbarnInfo
import no.nav.bidrag.behandling.felles.grunnlag.VedtakInfo
import no.nav.bidrag.behandling.felles.grunnlag.delberegning.SluttberegningBBM
import no.nav.bidrag.behandling.felles.grunnlag.inntekt.Inntekt
import no.nav.bidrag.dokument.bestilling.bestilling.dto.GrunnlagInntektType
import no.nav.bidrag.dokument.bestilling.bestilling.dto.InntektPeriode
import java.math.BigDecimal

val objectMapper = ObjectMapper().findAndRegisterModules()
var reader = ObjectMapper().readerFor(object : TypeReference<List<String>>() {})
fun VedtakDto.hentInntekter(referanser: List<String>, resultatKode: String): List<Inntekt> {
    val sluttBeregninger = hentGrunnagDetaljer(GrunnlagType.SLUTTBEREGNING_BBM, SluttberegningBBM::class.java, referanser).filter { it.resultatKode == resultatKode }

    val referanserGrunnlag: List<String> = sluttBeregninger.flatMap { it.grunnlagReferanseListe }
    return hentGrunnagDetaljer(GrunnlagType.INNTEKT, Inntekt::class.java, referanserGrunnlag)
}
fun SluttberegningBBM.hentSoknadsBarnInfo(vedtakDto: VedtakDto): SoknadsbarnInfo {
    val referanserGrunnlag: List<String> = grunnlagReferanseListe
    return vedtakDto.hentGrunnagDetaljer(GrunnlagType.SOKNADSBARN_INFO, SoknadsbarnInfo::class.java, referanserGrunnlag).first()
}
fun SluttberegningBBM.hentInntekter(vedtakDto: VedtakDto): List<Inntekt> {
    val referanserGrunnlag: List<String> = grunnlagReferanseListe
    return vedtakDto.hentGrunnagDetaljer(GrunnlagType.INNTEKT, Inntekt::class.java, referanserGrunnlag)
}

fun hentInntekter(vedtakDto: VedtakDto, refListe: List<String>): List<Inntekt> {
    return vedtakDto.hentGrunnagDetaljer(GrunnlagType.INNTEKT, Inntekt::class.java, refListe)
}
fun VedtakDto.hentSluttberegninger(referanser: List<String>, resultatKode: String): List<SluttberegningBBM> {
    return hentGrunnagDetaljer(GrunnlagType.SLUTTBEREGNING_BBM, SluttberegningBBM::class.java, referanser).filter { it.resultatKode == resultatKode }
}

fun VedtakDto.hentSluttberegning(referanser: List<String>): List<SluttberegningBBM> {
    return hentGrunnagDetaljer(GrunnlagType.SLUTTBEREGNING_BBM, SluttberegningBBM::class.java, referanser)
}

fun VedtakDto.hentSoknadBarnInfo(referanser: List<String>): List<SoknadsbarnInfo> {
    val sluttBeregninger = hentGrunnagDetaljer(GrunnlagType.SLUTTBEREGNING_BBM, SluttberegningBBM::class.java, referanser)

    val referanserGrunnlag: List<String> = sluttBeregninger.flatMap { it.grunnlagReferanseListe }
    return hentGrunnagDetaljer(GrunnlagType.SOKNADSBARN_INFO, SoknadsbarnInfo::class.java, referanserGrunnlag)
}

fun <T> VedtakDto.hentGrunnagDetaljer(grunnlagType: GrunnlagType, clazz: Class<T>, referanser: List<String> = emptyList()): List<T> = grunnlagListe
    .filter { it.type == grunnlagType }
    .filter { referanser.isEmpty() || referanser.contains(it.referanse) }
    .map { objectMapper.readValue(it.innhold.toString(), clazz) }
fun VedtakDto.hentBarnInfo(): List<BarnInfo> = hentGrunnagDetaljer(GrunnlagType.BARN_INFO, BarnInfo::class.java)
fun VedtakDto.hentBarnInfoForFnr(fnr: String): BarnInfo? = hentGrunnagDetaljer(GrunnlagType.BARN_INFO, BarnInfo::class.java).find { it.fnr == fnr }
fun VedtakDto.hentSøknadBarnInfo(): List<SoknadsbarnInfo> = hentGrunnagDetaljer(GrunnlagType.SOKNADSBARN_INFO, SoknadsbarnInfo::class.java)
fun VedtakDto.hentBostatus(fodselsnummer: String): List<Bostatus> = hentGrunnagDetaljer(GrunnlagType.SOKNADSBARN_INFO, SoknadsbarnInfo::class.java).find { it.fnr == fodselsnummer }?.let { sb -> hentGrunnagDetaljer(GrunnlagType.BOSTATUS, Bostatus::class.java).filter { it.soknadsbarnId == sb.soknadsbarnId } } ?: emptyList()
fun VedtakDto.hentPersonInfo(rolle: Rolle): PersonInfo? = hentGrunnagDetaljer(GrunnlagType.PERSON_INFO, PersonInfo::class.java).find { it.rolle == rolle }
fun VedtakDto.hentVedtakInfo(): VedtakInfo? = grunnlagListe.find { it.type == GrunnlagType.VEDTAK_INFO }?.let { objectMapper.readValue(it.innhold.toString(), VedtakInfo::class.java) }
fun VedtakDto.hentSoknadInfo(): SoknadInfo? = grunnlagListe.find { it.type == GrunnlagType.SOKNAD_INFO }?.let { objectMapper.readValue(it.innhold.toString(), SoknadInfo::class.java) }
fun VedtakDto.hentSivilstand(): List<Sivilstand> = hentGrunnagDetaljer(GrunnlagType.SIVILSTAND, Sivilstand::class.java)
fun VedtakDto.hentInntekter(): List<Inntekt> = hentGrunnagDetaljer(GrunnlagType.INNTEKT, Inntekt::class.java)
fun VedtakDto.hentBarnIHustand(): List<BarnIHusstand> = hentGrunnagDetaljer(GrunnlagType.BARN_I_HUSSTAND, BarnIHusstand::class.java)

fun List<InntektPeriode>.hentBeregningsgrunnlag(): MutableList<InntektPeriode> {
    val beregningsGrunnlagListe = mutableListOf<InntektPeriode>()
    hentTotalInntektPeriodeForRolle(Rolle.BIDRAGSMOTTAKER)?.let { inntektPeriode -> beregningsGrunnlagListe.add(inntektPeriode) }
    hentTotalInntektPeriodeForRolle(Rolle.BIDRAGSPLIKTIG)?.let { inntektPeriode -> beregningsGrunnlagListe.add(inntektPeriode) }
    hentTotalInntektPeriodeForRolle(Rolle.SOKNADSBARN)?.let { inntektPeriode -> beregningsGrunnlagListe.add(inntektPeriode) }
    return beregningsGrunnlagListe
}

fun List<InntektPeriode>.hentTotalInntektPeriodeForRolle(rolle: Rolle): InntektPeriode? = totalBelopForRolle(Rolle.BIDRAGSMOTTAKER)?.let { totalBelop ->
    find { it.rolle == rolle }?.copy(
        beløp = totalBelop,
        beløpType = GrunnlagInntektType(periodeBeregningsGrunnlag = true)
    )
}
fun List<InntektPeriode>.totalBelopForRolle(rolle: Rolle): BigDecimal = filter { it.rolle == rolle }.map { it.beløp }.reduce { acc, num -> acc + num }
