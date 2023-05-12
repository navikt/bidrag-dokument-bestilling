package no.nav.bidrag.dokument.bestilling.bestilling

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.date.shouldHaveSameDayAs
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.nav.bidrag.behandling.felles.dto.vedtak.VedtakDto
import no.nav.bidrag.behandling.felles.enums.BostatusKode
import no.nav.bidrag.behandling.felles.enums.InntektType
import no.nav.bidrag.behandling.felles.enums.Rolle
import no.nav.bidrag.behandling.felles.enums.SivilstandKode
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.behandling.felles.enums.VedtakKilde
import no.nav.bidrag.behandling.felles.enums.VedtakType
import no.nav.bidrag.dokument.bestilling.consumer.BidragVedtakConsumer
import no.nav.bidrag.dokument.bestilling.consumer.SjablonConsumer
import no.nav.bidrag.dokument.bestilling.consumer.dto.SjablongerDto
import no.nav.bidrag.dokument.bestilling.model.typeRef
import no.nav.bidrag.dokument.bestilling.tjenester.PersonService
import no.nav.bidrag.dokument.bestilling.tjenester.SjablongService
import no.nav.bidrag.dokument.bestilling.tjenester.VedtakService
import no.nav.bidrag.dokument.bestilling.utils.BARN1
import no.nav.bidrag.dokument.bestilling.utils.BARN2
import no.nav.bidrag.dokument.bestilling.utils.BM1
import no.nav.bidrag.dokument.bestilling.utils.BP1
import no.nav.bidrag.dokument.bestilling.utils.FORSKUDD_INNTEKTGRENSE_2022_2023
import no.nav.bidrag.dokument.bestilling.utils.createPostAdresseResponse
import no.nav.bidrag.dokument.bestilling.utils.readFile
import no.nav.bidrag.dokument.bestilling.utils.readFile2
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.LocalDate

@ExtendWith(MockKExtension::class)
class VedtakServiceTest {

    @MockK
    lateinit var personService: PersonService

    @MockK
    lateinit var vedtakConsumer: BidragVedtakConsumer

    @MockK
    lateinit var sjablonConsumer: SjablonConsumer

    @InjectMockKs
    lateinit var sjablongService: SjablongService

    @InjectMockKs
    lateinit var vedtakService: VedtakService

    @BeforeEach
    fun initMocks() {
        val sjablonResponse = ObjectMapper().findAndRegisterModules().readValue(readFile("api/sjablon_all.json"), typeRef<SjablongerDto>())
        every { sjablonConsumer.hentSjablonger() } returns sjablonResponse
    }

    fun mockDefaultValues() {
        every { personService.hentPerson(BM1.ident.verdi, any()) } returns BM1
        every { personService.hentPerson(BP1.ident.verdi, any()) } returns BP1
        every { personService.hentPerson(BARN1.ident.verdi, any()) } returns BARN1
        every { personService.hentPerson(BARN2.ident.verdi, any()) } returns BARN2
        every { personService.hentPerson(any(), any()) } returns BARN2
        every { personService.hentSpråk(any()) } returns "NB"
        every { personService.hentPersonAdresse(any(), any()) } returns createPostAdresseResponse()
    }

    @Test
    fun `skal mappe vedtakdetaljer`(){
        mockDefaultValues()
        val vedtakResponse = ObjectMapper().findAndRegisterModules().readValue(readFile2("vedtak/vedtak_forskudd_enkel_108.json"), VedtakDto::class.java)
        every { vedtakConsumer.hentVedtak(any()) } returns vedtakResponse

        val vedtakDetaljer = vedtakService.hentVedtakDetaljer("108")

        assertSoftly {
            vedtakDetaljer shouldNotBe null

            vedtakDetaljer.kilde shouldBe VedtakKilde.MANUELT
            vedtakDetaljer.vedtakType shouldBe VedtakType.ENDRING
            vedtakDetaljer.stønadType shouldBe StonadType.FORSKUDD
            vedtakDetaljer.virkningÅrsakKode shouldBe "M"
            vedtakDetaljer.virkningDato!! shouldHaveSameDayAs LocalDate.parse("2023-04-01")
            vedtakDetaljer.soknadDato!! shouldHaveSameDayAs LocalDate.parse("2023-04-10")
            vedtakDetaljer.soktFraDato!! shouldHaveSameDayAs LocalDate.parse("2023-04-01")
            vedtakDetaljer.vedtattDato!! shouldHaveSameDayAs LocalDate.parse("2023-04-26")
            vedtakDetaljer.saksbehandlerInfo.id shouldBe "Z994775"
            vedtakDetaljer.saksbehandlerInfo.navn shouldBe "F_Z994775 E_Z994775"

            vedtakDetaljer.sivilstandPerioder shouldHaveSize 1
            val sivilstandPeriode = vedtakDetaljer.sivilstandPerioder[0]
            sivilstandPeriode.fomDato shouldHaveSameDayAs LocalDate.parse("2023-04-01")
            sivilstandPeriode.tomDato shouldBe null
            sivilstandPeriode.sivilstandKode shouldBe SivilstandKode.ENSLIG
            sivilstandPeriode.sivilstandBeskrivelse shouldBe "Ugift"

            vedtakDetaljer.barnIHustandPerioder shouldHaveSize 1
            val barnIHustandPeriode = vedtakDetaljer.barnIHustandPerioder[0]
            barnIHustandPeriode.fomDato shouldHaveSameDayAs LocalDate.parse("2023-04-01")
            barnIHustandPeriode.tomDato shouldBe null
            barnIHustandPeriode.antall shouldBe 2

            vedtakDetaljer.vedtakBarn shouldHaveSize 2
            val barn1 = vedtakDetaljer.vedtakBarn[0]
            val barn1Bostatus = barn1.bostatusPerioder[0]
            barn1Bostatus.fomDato shouldHaveSameDayAs LocalDate.parse("2023-04-01")
            barn1Bostatus.tomDato shouldBe null
            barn1Bostatus.bostatusKode shouldBe BostatusKode.MED_FORELDRE

            val barn1StonadPeriode = barn1.stonader[0]
            barn1StonadPeriode.type shouldBe StonadType.FORSKUDD
            val barnVedtakPeriode = barn1StonadPeriode.vedtakPerioder[0]
            barnVedtakPeriode.fomDato shouldHaveSameDayAs LocalDate.parse("2023-04-01")
            barnVedtakPeriode.tomDato shouldBe null
            barnVedtakPeriode.beløp shouldBe BigDecimal(1760)
            barnVedtakPeriode.resultatKode shouldBe "100"

            val barn1InntektPeriode1 = barnVedtakPeriode.inntektPerioder[0]
            val barn1InntektBeregningsgrunnlag = barnVedtakPeriode.inntektPerioder[1]

            barn1InntektPeriode1.beløpType.inntektType shouldBe InntektType.INNTEKTSOPPLYSNINGER_ARBEIDSGIVER
            barn1InntektBeregningsgrunnlag.beløpType.periodeBeregningsGrunnlag shouldBe true

            barn1InntektPeriode1.fomDato shouldHaveSameDayAs LocalDate.parse("2023-04-01")
            barn1InntektPeriode1.tomDato shouldBe null
            barn1InntektBeregningsgrunnlag.fomDato shouldHaveSameDayAs LocalDate.parse("2023-04-01")
            barn1InntektBeregningsgrunnlag.tomDato shouldBe null

            barn1InntektPeriode1.periodeFomDato shouldHaveSameDayAs  barnVedtakPeriode.fomDato
            barn1InntektPeriode1.periodeTomDato shouldBe barnVedtakPeriode.tomDato
            barn1InntektBeregningsgrunnlag.periodeFomDato shouldHaveSameDayAs  barnVedtakPeriode.fomDato
            barn1InntektBeregningsgrunnlag.periodeTomDato shouldBe barnVedtakPeriode.tomDato

            barn1InntektPeriode1.beløp shouldBe BigDecimal(150000)
            barn1InntektBeregningsgrunnlag.beløp shouldBe BigDecimal(150000)

            barn1InntektPeriode1.inntektGrense shouldBe FORSKUDD_INNTEKTGRENSE_2022_2023
            barn1InntektBeregningsgrunnlag.inntektGrense shouldBe FORSKUDD_INNTEKTGRENSE_2022_2023

            barn1InntektPeriode1.rolle shouldBe Rolle.BIDRAGSMOTTAKER
            barn1InntektBeregningsgrunnlag.rolle shouldBe Rolle.BIDRAGSMOTTAKER

        }
    }
}