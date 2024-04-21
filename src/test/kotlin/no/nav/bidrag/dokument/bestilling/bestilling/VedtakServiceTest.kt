package no.nav.bidrag.dokument.bestilling.bestilling

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.nav.bidrag.behandling.felles.enums.SivilstandKode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.InntektPeriode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.PeriodeFraTom
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakBarnStonad
import no.nav.bidrag.dokument.bestilling.bestilling.dto.VedtakDetaljer
import no.nav.bidrag.dokument.bestilling.consumer.BidragVedtakConsumer
import no.nav.bidrag.dokument.bestilling.consumer.SjablonConsumer
import no.nav.bidrag.dokument.bestilling.consumer.dto.SjablongerDto
import no.nav.bidrag.dokument.bestilling.model.tilLocalDateFom
import no.nav.bidrag.dokument.bestilling.model.tilLocalDateTil
import no.nav.bidrag.dokument.bestilling.model.typeRef
import no.nav.bidrag.dokument.bestilling.tjenester.PersonService
import no.nav.bidrag.dokument.bestilling.tjenester.SjablongService
import no.nav.bidrag.dokument.bestilling.tjenester.VedtakService
import no.nav.bidrag.dokument.bestilling.utils.BARN1
import no.nav.bidrag.dokument.bestilling.utils.BARN2
import no.nav.bidrag.dokument.bestilling.utils.BM1
import no.nav.bidrag.dokument.bestilling.utils.BP1
import no.nav.bidrag.dokument.bestilling.utils.FORSKUDD_INNTEKTGRENSE_2019_2020
import no.nav.bidrag.dokument.bestilling.utils.FORSKUDD_INNTEKTGRENSE_2020_2021
import no.nav.bidrag.dokument.bestilling.utils.FORSKUDD_INNTEKTGRENSE_2021_2022
import no.nav.bidrag.dokument.bestilling.utils.FORSKUDD_INNTEKTGRENSE_2022_2023
import no.nav.bidrag.dokument.bestilling.utils.FORSKUDD_MAKS_INNTEKT_FORSKUDD_MOTTAKER_2019_2020
import no.nav.bidrag.dokument.bestilling.utils.FORSKUDD_MAKS_INNTEKT_FORSKUDD_MOTTAKER_2020_2021
import no.nav.bidrag.dokument.bestilling.utils.FORSKUDD_MAKS_INNTEKT_FORSKUDD_MOTTAKER_2021_2022
import no.nav.bidrag.dokument.bestilling.utils.FORSKUDD_MAKS_INNTEKT_FORSKUDD_MOTTAKER_2022_2023
import no.nav.bidrag.dokument.bestilling.utils.SAKSBEHANDLER_IDENT
import no.nav.bidrag.dokument.bestilling.utils.SAKSBEHANDLER_NAVN
import no.nav.bidrag.dokument.bestilling.utils.createPostAdresseResponse
import no.nav.bidrag.dokument.bestilling.utils.readFile
import no.nav.bidrag.domene.enums.inntekt.Inntektsrapportering
import no.nav.bidrag.domene.enums.person.Bostatuskode
import no.nav.bidrag.domene.enums.person.Sivilstandskode
import no.nav.bidrag.domene.enums.rolle.Rolletype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakskilde
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.transport.behandling.vedtak.response.VedtakDto
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
        every { personService.hentSpråk(any()) } returns "NB"
        every { personService.hentPersonAdresse(any(), any()) } returns createPostAdresseResponse()
    }

    @Test
    fun `skal mappe vedtakdetaljer for vedtak med en periode`() {
        mockDefaultValues()
        val virkningDato = LocalDate.parse("2023-04-01")
        val vedtakResponse = ObjectMapper().findAndRegisterModules().readValue(readFile("vedtak/vedtak_forskudd_enkel_108.json"), VedtakDto::class.java)
        every { vedtakConsumer.hentVedtak(eq("108")) } returns vedtakResponse

        val vedtakDetaljer = vedtakService.hentVedtakDetaljer("108")

        assertSoftly {
            vedtakDetaljer shouldNotBe null

            vedtakDetaljer.kilde shouldBe Vedtakskilde.MANUELT
            vedtakDetaljer.vedtakType shouldBe Vedtakstype.ENDRING
            vedtakDetaljer.stønadType shouldBe Stønadstype.FORSKUDD
            vedtakDetaljer.årsakKode shouldBe "M"
            vedtakDetaljer.virkningstidspunkt!! shouldBe virkningDato
            vedtakDetaljer.mottattDato!! shouldBe LocalDate.parse("2023-04-10")
            vedtakDetaljer.soktFraDato!! shouldBe LocalDate.parse("2023-04-01")
            vedtakDetaljer.vedtattDato!! shouldBe LocalDate.parse("2023-04-26")
            vedtakDetaljer.saksbehandlerInfo.ident shouldBe SAKSBEHANDLER_IDENT
            vedtakDetaljer.saksbehandlerInfo.navn shouldBe SAKSBEHANDLER_NAVN

            // Sivilstand
            vedtakDetaljer.sivilstandPerioder shouldHaveSize 1
            val sivilstandPeriode = vedtakDetaljer.hentSivilstandPeriodeForDato(PeriodeFraTom(virkningDato))!!
            sivilstandPeriode.periode.tilLocalDateFom() shouldBe LocalDate.parse("2023-04-01")
            sivilstandPeriode.periode.til shouldBe null
            sivilstandPeriode.sivilstand shouldBe SivilstandKode.ENSLIG

            // Bostatus perioder
            vedtakDetaljer.barnIHustandPerioder shouldHaveSize 1
            val barnIHustandPeriode = vedtakDetaljer.hentBarnIHustandPeriodeForDato(virkningDato)!!
            barnIHustandPeriode.fomDato shouldBe LocalDate.parse("2023-04-01")
            barnIHustandPeriode.tomDato shouldBe null
            barnIHustandPeriode.antall shouldBe 2

            vedtakDetaljer.vedtakBarn shouldHaveSize 2
            val barn1 = vedtakDetaljer.vedtakBarn[0]
            val barn1Bostatus = barn1.bostatusPerioder[0]
            barn1Bostatus.periode.tilLocalDateFom() shouldBe LocalDate.parse("2023-04-01")
            barn1Bostatus.periode.til shouldBe null
            barn1Bostatus.bostatus shouldBe Bostatuskode.MED_FORELDER

            val barn1StonadPeriode = barn1.stonader[0]
            barn1StonadPeriode.type shouldBe Stønadstype.FORSKUDD
            val barnVedtakPeriode = barn1StonadPeriode.vedtakPerioder[0]
            barnVedtakPeriode.fomDato shouldBe LocalDate.parse("2023-04-01")
            barnVedtakPeriode.tomDato shouldBe null
            barnVedtakPeriode.beløp shouldBe BigDecimal(1760)
            barnVedtakPeriode.resultatKode shouldBe "100"
            barnVedtakPeriode.inntektGrense shouldBe FORSKUDD_INNTEKTGRENSE_2022_2023
            barnVedtakPeriode.maksInntekt shouldBe FORSKUDD_MAKS_INNTEKT_FORSKUDD_MOTTAKER_2022_2023

            // Inntekt periode
            val barn1InntektPeriode1 = barnVedtakPeriode.inntekter[0]
            val barn1InntektBeregningsgrunnlag = barnVedtakPeriode.inntekter[1]

            barn1InntektPeriode1.type shouldBe Inntektsrapportering.INNTEKTSOPPLYSNINGER_ARBEIDSGIVER
            barn1InntektBeregningsgrunnlag.periodeTotalinntekt shouldBe true

            barn1InntektPeriode1.inntektPeriode?.tilLocalDateFom() shouldBe LocalDate.parse("2023-04-01")
            barn1InntektPeriode1.inntektPeriode?.til shouldBe null

            barn1InntektPeriode1.periode.fom shouldBe barnVedtakPeriode.fomDato
            barn1InntektPeriode1.periode.til shouldBe barnVedtakPeriode.tomDato
            barn1InntektBeregningsgrunnlag.periode.fom shouldBe barnVedtakPeriode.fomDato
            barn1InntektBeregningsgrunnlag.periode.til shouldBe barnVedtakPeriode.tomDato

            barn1InntektPeriode1.beløp shouldBe BigDecimal(150000)
            barn1InntektBeregningsgrunnlag.beløp shouldBe BigDecimal(150000)

            barn1InntektPeriode1.rolle shouldBe Rolletype.BIDRAGSMOTTAKER
            barn1InntektBeregningsgrunnlag.rolle shouldBe Rolletype.BIDRAGSMOTTAKER
        }
    }

    @Test
    fun `skal mappe vedtakdetaljer for vedtak med flere perioder`() {
        mockDefaultValues()
        val virkningDato = LocalDate.parse("2020-01-01")
        val periode1 = PeriodeFraTom(virkningDato, LocalDate.parse("2020-07-01"))
        val periode2 = PeriodeFraTom(LocalDate.parse("2020-07-01"), LocalDate.parse("2021-07-01"))
        val periode3 = PeriodeFraTom(LocalDate.parse("2021-07-01"), LocalDate.parse("2022-07-01"))
        val periode4 = PeriodeFraTom(LocalDate.parse("2022-07-01"), null)
        val vedtakResponse = ObjectMapper().findAndRegisterModules().readValue(readFile("vedtak/vedtak_forskudd_flere_perioder_186.json"), VedtakDto::class.java)
        every { vedtakConsumer.hentVedtak(eq("186")) } returns vedtakResponse

        val vedtakDetaljer = vedtakService.hentVedtakDetaljer("186")

        assertSoftly {
            vedtakDetaljer shouldNotBe null

            vedtakDetaljer.kilde shouldBe Vedtakskilde.MANUELT
            vedtakDetaljer.vedtakType shouldBe Vedtakstype.FASTSETTELSE
            vedtakDetaljer.stønadType shouldBe Stønadstype.FORSKUDD
            vedtakDetaljer.årsakKode shouldBe "H"
            vedtakDetaljer.virkningstidspunkt!! shouldBe virkningDato
            vedtakDetaljer.mottattDato!! shouldBe LocalDate.parse("2023-01-11")
            vedtakDetaljer.soktFraDato!! shouldBe LocalDate.parse("2020-01-01")
            vedtakDetaljer.vedtattDato!! shouldBe LocalDate.parse("2023-05-15")
            vedtakDetaljer.saksbehandlerInfo.ident shouldBe SAKSBEHANDLER_IDENT
            vedtakDetaljer.saksbehandlerInfo.navn shouldBe SAKSBEHANDLER_NAVN

            // Sivilstand
            vedtakDetaljer.sivilstandPerioder shouldHaveSize 3
            vedtakDetaljer.validerSivilstandPeriode(periode1, Sivilstandskode.GIFT_SAMBOER, "Gift")
            vedtakDetaljer.validerSivilstandPeriode(PeriodeFraTom(periode1.tomDato!!, periode3.tomDato), Sivilstandskode.ENSLIG, "Skilt")
            vedtakDetaljer.validerSivilstandPeriode(periode4, Sivilstandskode.ENSLIG, "Enke")

            // Bostatus perioder
            vedtakDetaljer.barnIHustandPerioder shouldHaveSize 2
            vedtakDetaljer.validerBostatus(virkningDato, LocalDate.parse("2022-07-01"), 1)
            vedtakDetaljer.validerBostatus(LocalDate.parse("2022-07-01"), null, 3)

            vedtakDetaljer.vedtakBarn shouldHaveSize 1
            val barn1 = vedtakDetaljer.vedtakBarn[0]
            val barn1Bostatus = barn1.bostatusPerioder[0]
            barn1Bostatus.periode.tilLocalDateFom() shouldBe LocalDate.parse("2020-01-01")
            barn1Bostatus.periode.tilLocalDateTil() shouldBe null
            barn1Bostatus.bostatus shouldBe Bostatuskode.MED_FORELDER

            val barn1StonadPeriode = barn1.stonader[0]
            barn1StonadPeriode.type shouldBe Stønadstype.FORSKUDD
            val vedtakPeriode1 = barn1StonadPeriode.hentVedtakPeriodeForDato(virkningDato)!!
            vedtakPeriode1.fomDato shouldBe periode1.fraDato
            vedtakPeriode1.tomDato shouldBe periode1.tomDato
            vedtakPeriode1.beløp shouldBe BigDecimal(820)
            vedtakPeriode1.resultatKode shouldBe "50"
            vedtakPeriode1.inntektGrense shouldBe FORSKUDD_INNTEKTGRENSE_2019_2020
            vedtakPeriode1.maksInntekt shouldBe FORSKUDD_MAKS_INNTEKT_FORSKUDD_MOTTAKER_2019_2020

            val vedtakPeriode2 = barn1StonadPeriode.hentVedtakPeriodeForDato(LocalDate.parse("2020-07-01"))!!
            vedtakPeriode2.fomDato shouldBe periode2.fraDato
            vedtakPeriode2.tomDato shouldBe periode2.tomDato
            vedtakPeriode2.beløp shouldBe BigDecimal(1250)
            vedtakPeriode2.resultatKode shouldBe "75"
            vedtakPeriode2.inntektGrense shouldBe FORSKUDD_INNTEKTGRENSE_2020_2021
            vedtakPeriode2.maksInntekt shouldBe FORSKUDD_MAKS_INNTEKT_FORSKUDD_MOTTAKER_2020_2021

            val vedtakPeriode3 = barn1StonadPeriode.hentVedtakPeriodeForDato(LocalDate.parse("2021-07-01"))!!
            vedtakPeriode3.fomDato shouldBe periode3.fraDato
            vedtakPeriode3.tomDato shouldBe periode3.tomDato
            vedtakPeriode3.beløp shouldBe BigDecimal(1280)
            vedtakPeriode3.resultatKode shouldBe "75"
            vedtakPeriode3.inntektGrense shouldBe FORSKUDD_INNTEKTGRENSE_2021_2022
            vedtakPeriode3.maksInntekt shouldBe FORSKUDD_MAKS_INNTEKT_FORSKUDD_MOTTAKER_2021_2022

            val vedtakPeriode4 = barn1StonadPeriode.hentVedtakPeriodeForDato(LocalDate.parse("2022-07-01"))!!
            vedtakPeriode4.fomDato shouldBe periode4.fraDato
            vedtakPeriode4.tomDato shouldBe periode4.tomDato
            vedtakPeriode4.beløp shouldBe BigDecimal(1320)
            vedtakPeriode4.resultatKode shouldBe "75"
            vedtakPeriode4.inntektGrense shouldBe FORSKUDD_INNTEKTGRENSE_2022_2023
            vedtakPeriode4.maksInntekt shouldBe FORSKUDD_MAKS_INNTEKT_FORSKUDD_MOTTAKER_2022_2023
//
            // Inntekt periode
            val vedtakPeriode1Inntekter = vedtakPeriode1.inntekter
            vedtakPeriode1Inntekter shouldHaveSize 3

            vedtakPeriode1Inntekter[0].type shouldBe Inntektsrapportering.SMÅBARNSTILLEGG
            vedtakPeriode1Inntekter[0].beløp shouldBe BigDecimal(5000)
            vedtakPeriode1Inntekter[0].periode.tilLocalDateFom() shouldBe vedtakPeriode1.fomDato
            vedtakPeriode1Inntekter[0].periode.tilLocalDateTil() shouldBe vedtakPeriode1.tomDato

            vedtakPeriode1Inntekter[1].type shouldBe Inntektsrapportering.DOKUMENTASJON_MANGLER_SKJØNN
            vedtakPeriode1Inntekter[1].beløp shouldBe BigDecimal(350000)

            vedtakPeriode1Inntekter[2].type shouldBe null
            vedtakPeriode1Inntekter[2].periodeTotalinntekt shouldBe true
            vedtakPeriode1Inntekter[2].beløp shouldBe (vedtakPeriode1Inntekter[0].beløp + vedtakPeriode1Inntekter[1].beløp)

            vedtakPeriode1Inntekter.find { it.type == Inntektsrapportering.KAPITALINNTEKT_EGNE_OPPLYSNINGER || it.nettoKapitalInntekt == true } shouldBe null
            vedtakPeriode2.inntekter.find { it.type == Inntektsrapportering.KAPITALINNTEKT_EGNE_OPPLYSNINGER || it.nettoKapitalInntekt == true } shouldBe null
            vedtakPeriode3.inntekter.find { it.type == Inntektsrapportering.KAPITALINNTEKT_EGNE_OPPLYSNINGER || it.nettoKapitalInntekt == true } shouldBe null

            val vedtakPeriode4Inntekter = vedtakPeriode4.inntekter
            vedtakPeriode4Inntekter shouldHaveSize 5

            vedtakPeriode4Inntekter[0].type shouldBe Inntektsrapportering.SMÅBARNSTILLEGG
            vedtakPeriode4Inntekter[1].type shouldBe Inntektsrapportering.PERSONINNTEKT_EGNE_OPPLYSNINGER
            vedtakPeriode4Inntekter[2].type shouldBe Inntektsrapportering.UTVIDET_BARNETRYGD
            vedtakPeriode4Inntekter[3].type shouldBe null
            vedtakPeriode4Inntekter[3].nettoKapitalInntekt shouldBe true
            vedtakPeriode4Inntekter[3].beløp shouldBe BigDecimal(19000)

            vedtakPeriode4Inntekter[4].type shouldBe null
            vedtakPeriode4Inntekter[4].periodeTotalinntekt shouldBe true
            vedtakPeriode4Inntekter[4].beløp shouldBe BigDecimal(432000)
        }
    }

    @Test
    fun `skal mappe vedtakdetaljer for vedtak med flere perioder og barn`() {
        mockDefaultValues()
        val virkningDato = LocalDate.parse("2020-01-01")
        val periode1 = PeriodeFraTom(virkningDato, LocalDate.parse("2020-07-01"))
        val periode2 = PeriodeFraTom(LocalDate.parse("2020-07-01"), LocalDate.parse("2021-01-01"))
        val periode3 = PeriodeFraTom(LocalDate.parse("2021-01-01"), LocalDate.parse("2021-07-01"))
        val periode4 = PeriodeFraTom(LocalDate.parse("2021-07-01"), LocalDate.parse("2022-01-01"))
        val periode5 = PeriodeFraTom(LocalDate.parse("2022-01-01"), LocalDate.parse("2022-07-01"))
        val periode6 = PeriodeFraTom(LocalDate.parse("2022-07-01"), null)
        val antallPerioder = 6
        val vedtakResponse = ObjectMapper().findAndRegisterModules().readValue(readFile("vedtak/vedtak_forskudd_flere_perioder_og_barn_192.json"), VedtakDto::class.java)
        every { vedtakConsumer.hentVedtak(eq("192")) } returns vedtakResponse

        val vedtakDetaljer = vedtakService.hentVedtakDetaljer("192")

        assertSoftly {
            vedtakDetaljer shouldNotBe null

            vedtakDetaljer.kilde shouldBe Vedtakskilde.MANUELT
            vedtakDetaljer.vedtakType shouldBe Vedtakstype.FASTSETTELSE
            vedtakDetaljer.stønadType shouldBe Stønadstype.FORSKUDD
            vedtakDetaljer.årsakKode shouldBe "H"
            vedtakDetaljer.virkningstidspunkt!! shouldBe virkningDato
            vedtakDetaljer.mottattDato!! shouldBe LocalDate.parse("2023-01-15")
            vedtakDetaljer.soktFraDato!! shouldBe LocalDate.parse("2020-01-01")
            vedtakDetaljer.vedtattDato!! shouldBe LocalDate.parse("2023-05-19")
            vedtakDetaljer.saksbehandlerInfo.ident shouldBe SAKSBEHANDLER_IDENT
            vedtakDetaljer.saksbehandlerInfo.navn shouldBe SAKSBEHANDLER_NAVN

            // Sivilstand
            vedtakDetaljer.sivilstandPerioder shouldHaveSize 3
            vedtakDetaljer.validerSivilstandPeriode(PeriodeFraTom(periode1.fraDato!!, periode3.tomDato), Sivilstandskode.ENSLIG, "Enke")
            vedtakDetaljer.validerSivilstandPeriode(PeriodeFraTom(periode3.tomDato!!, periode4.tomDato), Sivilstandskode.ENSLIG, "Skilt")
            vedtakDetaljer.validerSivilstandPeriode(PeriodeFraTom(periode4.tomDato!!, null), Sivilstandskode.SAMBOER, "Samboer")

            // Bostatus perioder
            vedtakDetaljer.barnIHustandPerioder shouldHaveSize 2
            vedtakDetaljer.validerBostatus(virkningDato, LocalDate.parse("2021-01-01"), 2)
            vedtakDetaljer.validerBostatus(LocalDate.parse("2021-01-01"), null, 3)

            vedtakDetaljer.vedtakBarn shouldHaveSize 2
            val barn2 = vedtakDetaljer.vedtakBarn[0]
            val barn1 = vedtakDetaljer.vedtakBarn[0]
            barn1.bostatusPerioder shouldHaveSize antallPerioder
            val barn1Bostatus = barn1.bostatusPerioder[antallPerioder - 1] // TODO: Er dette riktig??
            barn1Bostatus.periode.tilLocalDateFom() shouldBe LocalDate.parse("2022-07-01")
            barn1Bostatus.periode.tilLocalDateTil() shouldBe null
            barn1Bostatus.bostatus shouldBe Bostatuskode.MED_FORELDER

            val barn1StonadPeriode = barn1.stonader[0]
            barn1StonadPeriode.type shouldBe Stønadstype.FORSKUDD
            val vedtakPeriode1 = barn1StonadPeriode.hentVedtakPeriodeForDato(virkningDato)!!
            vedtakPeriode1.fomDato shouldBe periode1.fraDato
            vedtakPeriode1.tomDato shouldBe periode1.tomDato
            vedtakPeriode1.beløp shouldBe BigDecimal(1230)
            vedtakPeriode1.resultatKode shouldBe "75"
            vedtakPeriode1.inntektGrense shouldBe FORSKUDD_INNTEKTGRENSE_2019_2020
            vedtakPeriode1.maksInntekt shouldBe FORSKUDD_MAKS_INNTEKT_FORSKUDD_MOTTAKER_2019_2020

            val vedtakPeriode6 = barn1StonadPeriode.hentVedtakPeriodeForDato(LocalDate.parse("2022-07-01"))!!
            vedtakPeriode6.fomDato shouldBe periode6.fraDato
            vedtakPeriode6.tomDato shouldBe periode6.tomDato
            vedtakPeriode6.beløp shouldBe BigDecimal(1320)
            vedtakPeriode6.resultatKode shouldBe "75"
            vedtakPeriode6.inntektGrense shouldBe FORSKUDD_INNTEKTGRENSE_2022_2023
            vedtakPeriode6.maksInntekt shouldBe FORSKUDD_MAKS_INNTEKT_FORSKUDD_MOTTAKER_2022_2023
//
            // Inntekt periode
            val vedtakPeriode1Inntekter = vedtakPeriode1.inntekter
            vedtakPeriode1Inntekter shouldHaveSize 3

            vedtakPeriode1Inntekter[0].type shouldBe Inntektsrapportering.SMÅBARNSTILLEGG
            vedtakPeriode1Inntekter[0].beløp shouldBe BigDecimal(5000)
            vedtakPeriode1Inntekter[0].periode.tilLocalDateFom() shouldBe vedtakPeriode1.fomDato
            vedtakPeriode1Inntekter[0].periode.tilLocalDateTil() shouldBe vedtakPeriode1.tomDato

            vedtakPeriode1Inntekter.hentGrunnlag()!!.beløp shouldBe BigDecimal(325000)
            vedtakPeriode1Inntekter.hentNettoKapital() shouldBe null

            val vedtakPeriode4Inntekter = vedtakPeriode6.inntekter
            vedtakPeriode4Inntekter shouldHaveSize 5

            vedtakPeriode4Inntekter[0].type shouldBe Inntektsrapportering.INNTEKTSOPPLYSNINGER_ARBEIDSGIVER
            vedtakPeriode4Inntekter.hentGrunnlag()!!.beløp shouldBe BigDecimal(438000)
            vedtakPeriode4Inntekter.hentNettoKapital()!!.beløp shouldBe BigDecimal(28000)

            // Validate barn 2

            val barn2StonadPeriode = barn2.stonader[0]
            barn2StonadPeriode.type shouldBe Stønadstype.FORSKUDD
            val vedtakPeriode1barn2 = barn2StonadPeriode.hentVedtakPeriodeForDato(virkningDato)!!
            vedtakPeriode1barn2.fomDato shouldBe periode1.fraDato
            vedtakPeriode1barn2.tomDato shouldBe periode1.tomDato
            vedtakPeriode1barn2.beløp shouldBe BigDecimal(1230)
            vedtakPeriode1barn2.resultatKode shouldBe "75"
            vedtakPeriode1barn2.inntektGrense shouldBe FORSKUDD_INNTEKTGRENSE_2019_2020
            vedtakPeriode1barn2.maksInntekt shouldBe FORSKUDD_MAKS_INNTEKT_FORSKUDD_MOTTAKER_2019_2020

            val vedtakPeriode6barn2 = barn2StonadPeriode.hentVedtakPeriodeForDato(LocalDate.parse("2022-07-01"))!!
            vedtakPeriode6barn2.fomDato shouldBe periode6.fraDato
            vedtakPeriode6barn2.tomDato shouldBe periode6.tomDato
            vedtakPeriode6barn2.beløp shouldBe BigDecimal(1320)
            vedtakPeriode6barn2.resultatKode shouldBe "75"
            vedtakPeriode6barn2.inntektGrense shouldBe FORSKUDD_INNTEKTGRENSE_2022_2023
            vedtakPeriode6barn2.maksInntekt shouldBe FORSKUDD_MAKS_INNTEKT_FORSKUDD_MOTTAKER_2022_2023
//
            // Inntekt periode
            val vedtakPeriode1InntekterBarn2 = vedtakPeriode1barn2.inntekter
            vedtakPeriode1InntekterBarn2 shouldHaveSize 3

            vedtakPeriode1InntekterBarn2[0].type shouldBe Inntektsrapportering.SMÅBARNSTILLEGG
            vedtakPeriode1InntekterBarn2[0].beløp shouldBe BigDecimal(5000)
            vedtakPeriode1InntekterBarn2[0].periode.tilLocalDateFom() shouldBe vedtakPeriode1.fomDato
            vedtakPeriode1InntekterBarn2[0].periode.tilLocalDateTil() shouldBe vedtakPeriode1.tomDato

            vedtakPeriode1InntekterBarn2.hentGrunnlag()!!.beløp shouldBe BigDecimal(325000)
            vedtakPeriode1InntekterBarn2.hentNettoKapital() shouldBe null

            val vedtakPeriode4InntekterBarn2 = vedtakPeriode6barn2.inntekter
            vedtakPeriode4InntekterBarn2 shouldHaveSize 5

            vedtakPeriode4InntekterBarn2[0].type shouldBe Inntektsrapportering.INNTEKTSOPPLYSNINGER_ARBEIDSGIVER
            vedtakPeriode4InntekterBarn2.hentGrunnlag()!!.beløp shouldBe BigDecimal(438000)
            vedtakPeriode4InntekterBarn2.hentNettoKapital()!!.beløp shouldBe BigDecimal(28000)
        }
    }

    fun List<InntektPeriode>.hentGrunnlag() = find { it.periodeTotalinntekt == true }

    fun List<InntektPeriode>.hentNettoKapital() = find { it.nettoKapitalInntekt == true }

    fun VedtakBarnStonad.vedtakPeriode(
        fomDato: LocalDate,
        tomDato: LocalDate? = null,
        beløp: BigDecimal,
        resultatKode: String,
    ) {
        val vedtakPeriode = hentVedtakPeriodeForDato(fomDato)!!
        vedtakPeriode.fomDato shouldBe fomDato
        vedtakPeriode.tomDato shouldBe tomDato
        vedtakPeriode.beløp shouldBe beløp
        vedtakPeriode.resultatKode shouldBe resultatKode
        vedtakPeriode.inntektGrense shouldBe if (tomDato == null) FORSKUDD_INNTEKTGRENSE_2022_2023 else FORSKUDD_INNTEKTGRENSE_2021_2022
        vedtakPeriode.maksInntekt shouldBe if (tomDato == null) FORSKUDD_MAKS_INNTEKT_FORSKUDD_MOTTAKER_2022_2023 else FORSKUDD_MAKS_INNTEKT_FORSKUDD_MOTTAKER_2021_2022
    }

    fun VedtakDetaljer.validerBostatus(
        fomDato: LocalDate,
        tomDato: LocalDate? = null,
        antallBarn: Int,
    ) {
        val barnIHustandPeriode = hentBarnIHustandPeriodeForDato(fomDato)!!
        barnIHustandPeriode.fomDato shouldBe fomDato
        barnIHustandPeriode.tomDato shouldBe tomDato
        barnIHustandPeriode.antall shouldBe antallBarn
    }

    fun VedtakDetaljer.validerSivilstandPeriode(
        periode: PeriodeFraTom,
        sivilstandKode: Sivilstandskode,
        beskrivelse: String,
    ) {
        val sivilstandPeriode = hentSivilstandPeriodeForDato(periode)!!
        sivilstandPeriode.periode.tilLocalDateFom() shouldBe periode.fraDato
        sivilstandPeriode.periode.tilLocalDateTil() shouldBe periode.tomDato
        sivilstandPeriode.sivilstand shouldBe sivilstandKode
    }

    fun VedtakBarnStonad.hentVedtakPeriodeForDato(dato: LocalDate) = this.vedtakPerioder.sortedByDescending { it.fomDato }.find { erInnenforPeriode(PeriodeFraTom(it.fomDato, it.tomDato), dato) }

    fun VedtakDetaljer.hentSivilstandPeriodeForDato(periode: PeriodeFraTom) = this.sivilstandPerioder.sortedByDescending { it.periode.fom }.find { it.periode.tilLocalDateFom() <= periode.fraDato && (it.periode.til == null || periode.tomDato == null || it.periode.tilLocalDateTil()!! >= periode.tomDato) }

    fun VedtakDetaljer.hentBarnIHustandPeriodeForDato(dato: LocalDate) = this.barnIHustandPerioder.sortedByDescending { it.fomDato }.find { it.fomDato <= dato && (it.tomDato == null || it.tomDato!! > dato) }

    fun erInnenforPeriode(
        periode: PeriodeFraTom,
        dato: LocalDate,
    ) = periode.fraDato <= dato && (periode.tomDato == null || periode.tomDato!! > dato)
}
