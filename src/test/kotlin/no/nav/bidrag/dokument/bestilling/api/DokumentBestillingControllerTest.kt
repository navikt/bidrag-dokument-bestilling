package no.nav.bidrag.dokument.bestilling.api

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeIn
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import no.nav.bidrag.commons.web.EnhetFilter.Companion.X_ENHET_HEADER
import no.nav.bidrag.dokument.bestilling.api.dto.DokumentBestillingForespørsel
import no.nav.bidrag.dokument.bestilling.api.dto.DokumentBestillingResponse
import no.nav.bidrag.dokument.bestilling.api.dto.MottakerTo
import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentMal
import no.nav.bidrag.dokument.bestilling.bestilling.dto.PeriodeFraTom
import no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto.BidragBarn
import no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto.BrevBestilling
import no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto.BrevKontaktinfo
import no.nav.bidrag.dokument.bestilling.consumer.dto.fornavnEtternavn
import no.nav.bidrag.dokument.bestilling.model.MAX_DATE
import no.nav.bidrag.dokument.bestilling.utils.ANNEN_MOTTAKER
import no.nav.bidrag.dokument.bestilling.utils.BARN1
import no.nav.bidrag.dokument.bestilling.utils.BARN2
import no.nav.bidrag.dokument.bestilling.utils.BM1
import no.nav.bidrag.dokument.bestilling.utils.BP1
import no.nav.bidrag.dokument.bestilling.utils.FASTSETTELSE_GEBYR_2023
import no.nav.bidrag.dokument.bestilling.utils.FORSKUDD_INNTEKTGRENSE_2019_2020
import no.nav.bidrag.dokument.bestilling.utils.FORSKUDD_INNTEKTGRENSE_2020_2021
import no.nav.bidrag.dokument.bestilling.utils.FORSKUDD_INNTEKTGRENSE_2021_2022
import no.nav.bidrag.dokument.bestilling.utils.FORSKUDD_INNTEKTGRENSE_2022_2023
import no.nav.bidrag.dokument.bestilling.utils.FORSKUDD_MAKS_INNTEKT_FORSKUDD_MOTTAKER_2019_2020
import no.nav.bidrag.dokument.bestilling.utils.FORSKUDD_MAKS_INNTEKT_FORSKUDD_MOTTAKER_2022_2023
import no.nav.bidrag.dokument.bestilling.utils.SAKSBEHANDLER_NAVN
import no.nav.bidrag.dokument.bestilling.utils.SAK_OPPRETTET_DATO
import no.nav.bidrag.dokument.bestilling.utils.SAMHANDLER_IDENT
import no.nav.bidrag.dokument.bestilling.utils.createEnhetKontaktInformasjon
import no.nav.bidrag.dokument.bestilling.utils.createOpprettJournalpostResponse
import no.nav.bidrag.dokument.bestilling.utils.createPostAdresseResponse
import no.nav.bidrag.dokument.bestilling.utils.createPostAdresseResponseUtenlandsk
import no.nav.bidrag.dokument.bestilling.utils.createSakResponse
import no.nav.bidrag.domain.enums.Rolletype
import no.nav.bidrag.transport.sak.RolleDto
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.time.LocalDate

fun BidragBarn.hentInntektPerioder(periodeFraTom: PeriodeFraTom) = inntektPerioder.filter { it.fomDato == periodeFraTom.fraDato && it.tomDato == periodeFraTom.tomDato }

class DokumentBestillingControllerTest : AbstractControllerTest() {

    @Test
    fun `skal returnere liste over brevkoder som er støttet`() {
        val response = httpHeaderTestRestTemplate.exchange(
            "${rootUri()}/brevkoder",
            HttpMethod.OPTIONS,
            null,
            List::class.java
        )

        response.body?.forEach {
            it shouldBeIn DokumentMal.values().map { bk -> bk.name }
            it shouldNotBeIn DokumentMal.values().filter { bk -> !bk.enabled }.map { bk -> bk.name }
        }

        response.body?.shouldHaveSize(DokumentMal.values().filter { it.enabled }.size)
    }

    @Test
    @Disabled
    fun `skal validere XML for forskudd vedtakbrev med flere perioder`() {
        stubDefaultValues()
        stubUtils.stubHentPerson("16451299577", ANNEN_MOTTAKER)
        stubUtils.stubHentPerson("25451755601", ANNEN_MOTTAKER)
        stubUtils.stubHentVedtak("vedtak_forskudd_flere_perioder_186.json")
        val enhetKontaktInfo = createEnhetKontaktInformasjon()
        val bmAdresse = createPostAdresseResponse()
        val dokumentMal = DokumentMal.BI01A01
        val tittel = "Tittel på dokument"
        val saksnummer = "123213"
        val mottakerId = BM1.ident
        val gjelderId = BP1.ident

        stubUtils.stubHentAdresse(postAdresse = bmAdresse)
        stubUtils.stubEnhetKontaktInfo(enhetKontaktInfo)

        val request = DokumentBestillingForespørsel(
            mottakerId = mottakerId.verdi,
            gjelderId = gjelderId.verdi,
            saksnummer = saksnummer,
            tittel = tittel,
            vedtakId = "12312",
            enhet = "4806",
            spraak = "NB",
            dokumentreferanse = "BIF12321321321"

        )

        jmsTestConsumer.withOnlinebrev {
            val response = httpHeaderTestRestTemplate.exchange(
                "${rootUri()}/bestill/${dokumentMal.name}",
                HttpMethod.POST,
                HttpEntity(request),
                DokumentBestillingResponse::class.java
            )

            response.statusCode shouldBe HttpStatus.OK

            val message: String = this.getMessageAsString()
            val validateToMessage = readFile("xml/brev_vedtak_forskudd_186.xml")
            message shouldBeEqualComparingTo validateToMessage
        }
    }

    @Test
    fun `skal produsere XML for forskudd vedtakbrev med flere perioder`() {
        stubDefaultValues()
        stubUtils.stubHentPerson("16451299577", ANNEN_MOTTAKER)
        stubUtils.stubHentPerson("25451755601", ANNEN_MOTTAKER)
        stubUtils.stubHentVedtak("vedtak_forskudd_flere_perioder_186.json")
        val enhetKontaktInfo = createEnhetKontaktInformasjon()
        val bmAdresse = createPostAdresseResponse()
        val dokumentMal = DokumentMal.BI01A01
        val tittel = "Tittel på dokument"
        val saksnummer = "123213"
        val mottakerId = BM1.ident
        val gjelderId = BP1.ident

        stubUtils.stubHentAdresse(postAdresse = bmAdresse)
        stubUtils.stubEnhetKontaktInfo(enhetKontaktInfo)

        val request = DokumentBestillingForespørsel(
            mottakerId = mottakerId.verdi,
            gjelderId = gjelderId.verdi,
            saksnummer = saksnummer,
            tittel = tittel,
            vedtakId = "12312",
            enhet = "4806",
            spraak = "NB",
            dokumentreferanse = "BIF12321321321"

        )

        jmsTestConsumer.withOnlinebrev {
            val response = httpHeaderTestRestTemplate.exchange(
                "${rootUri()}/bestill/${dokumentMal.name}",
                HttpMethod.POST,
                HttpEntity(request),
                DokumentBestillingResponse::class.java
            )

            response.statusCode shouldBe HttpStatus.OK

            val message: BrevBestilling = this.getMessageAsObject(BrevBestilling::class.java)!!
            assertSoftly {
                verifyBrevbestillingHeaders(message, dokumentMal)
                message.brev?.tknr shouldBe enhetKontaktInfo.enhetIdent
                message.brev?.spraak shouldBe "NB"
                message.brev?.brevref shouldBe "BIF12321321321"

                message.brev?.kontaktInfo?.avsender?.navn shouldBe enhetKontaktInfo.enhetNavn
                message.brev?.kontaktInfo?.tlfAvsender?.telefonnummer shouldBe "55553333"
                message.brev?.kontaktInfo?.returAdresse?.enhet shouldBe "4806"
                message.brev?.kontaktInfo?.returAdresse?.navn shouldBe enhetKontaktInfo.enhetNavn
                message.brev?.kontaktInfo?.returAdresse?.adresselinje1 shouldBe enhetKontaktInfo.postadresse?.adresselinje1
                message.brev?.kontaktInfo?.returAdresse?.adresselinje2 shouldBe enhetKontaktInfo.postadresse?.adresselinje2
                message.brev?.kontaktInfo?.returAdresse?.postnummer shouldBe enhetKontaktInfo.postadresse?.postnummer
                message.brev?.kontaktInfo?.returAdresse?.poststed shouldBe enhetKontaktInfo.postadresse?.poststed
                message.brev?.kontaktInfo?.returAdresse?.land shouldBe enhetKontaktInfo.postadresse?.land
                message.brev?.kontaktInfo?.returAdresse?.shouldBeEqualToComparingFields(message.brev?.kontaktInfo?.postadresse as BrevKontaktinfo.Adresse)

                message.brev?.mottaker?.navn shouldBe BM1.navn?.verdi
                message.brev?.mottaker?.adresselinje1 shouldBe bmAdresse.adresselinje1?.verdi
                message.brev?.mottaker?.adresselinje2 shouldBe bmAdresse.adresselinje2?.verdi
                message.brev?.mottaker?.adresselinje3 shouldBe "3030 Drammen"
                message.brev?.mottaker?.boligNr shouldBe bmAdresse.bruksenhetsnummer?.verdi
                message.brev?.mottaker?.postnummer shouldBe bmAdresse.postnummer?.verdi
                message.brev?.mottaker?.spraak shouldBe "NB"
                message.brev?.mottaker?.rolle shouldBe "01"
                message.brev?.mottaker?.fodselsnummer shouldBe BM1.ident.verdi
                message.brev?.mottaker?.fodselsdato shouldBe BM1.fødselsdato?.verdi

                message.brev?.parter?.bmfnr shouldBe BM1.ident.verdi
                message.brev?.parter?.bmnavn shouldBe BM1.fornavnEtternavn()
                message.brev?.parter?.bpfnr shouldBe BP1.ident.verdi
                message.brev?.parter?.bpnavn shouldBe BP1.fornavnEtternavn()
                message.brev?.parter?.bmfodselsdato shouldBe BM1.fødselsdato?.verdi
                message.brev?.parter?.bpfodselsdato shouldBe BP1.fødselsdato?.verdi
                message.brev?.parter?.bmkravkfremav shouldBe ""
                message.brev?.parter?.bmgebyr shouldBe ""
                message.brev?.parter?.bmlandkode shouldBe ""
                message.brev?.parter?.bpkravfremav shouldBe ""
                message.brev?.parter?.bpgebyr shouldBe ""
                message.brev?.parter?.bplandkode shouldBe ""
                message.brev?.parter?.bmdatodod shouldBe null
                message.brev?.parter?.bpdatodod shouldBe null

                message.brev?.barnISak?.shouldHaveAtLeastSize(2)

                val barnISak1 = message.brev?.barnISak?.get(0)!!
                barnISak1.fnr shouldBe BARN2.ident.verdi
                barnISak1.navn shouldBe BARN2.fornavnEtternavn()
                barnISak1.fDato shouldBe BARN2.fødselsdato?.verdi
                barnISak1.personIdRm shouldBe ""
                barnISak1.belopGebyrRm shouldBe ""
                barnISak1.belForskudd shouldBe null
                barnISak1.belBidrag shouldBe null

                val barnISak2 = message.brev?.barnISak?.get(1)!!
                barnISak2.fDato shouldBe BARN1.fødselsdato?.verdi
                barnISak2.fnr shouldBe BARN1.ident.verdi
                barnISak2.navn shouldBe BARN1.fornavnEtternavn()
                barnISak2.personIdRm shouldBe ""
                barnISak2.belopGebyrRm shouldBe ""
                barnISak2.belForskudd shouldBe BigDecimal(1320).setScale(2)
                barnISak2.belBidrag shouldBe null

                message.brev?.soknadBost?.saksnr shouldBe saksnummer
                message.brev?.soknadBost?.sakstype shouldBe "E"
                message.brev?.soknadBost?.rmISak shouldBe false
                message.brev?.soknadBost?.sendtDato shouldBe LocalDate.now()

                message.brev?.saksbehandler?.navn shouldBe "Saksbehandler Mellomnavn Saksbehandlersen"

                val virkningDato = LocalDate.parse("2020-01-01")
                val periode1 = PeriodeFraTom(virkningDato, LocalDate.parse("2020-06-30"))
                val periode2 = PeriodeFraTom(LocalDate.parse("2020-07-01"), LocalDate.parse("2021-06-30"))
                val periode3 = PeriodeFraTom(LocalDate.parse("2021-07-01"), LocalDate.parse("2022-06-30"))
                val periode4 = PeriodeFraTom(LocalDate.parse("2022-07-01"), MAX_DATE)

                // Valider forskudd vedtak resultater
                val soknadDato = LocalDate.parse("2023-01-11")

                val soknad = message.brev?.soknad!!
                soknad.soknDato shouldBe soknadDato
                soknad.type shouldBe "FO"
                soknad.aarsakKd shouldBe "H"
                soknad.undergrp shouldBe "S"
                soknad.saksnr shouldBe saksnummer
                soknad.sendtDato shouldBe LocalDate.parse("2023-05-15")
                soknad.vedtattDato shouldBe LocalDate.parse("2023-05-15")
                soknad.virkningDato shouldBe virkningDato

                val soknadBost = message.brev?.soknadBost!!
                soknadBost.hgKode shouldBe "FO"
                soknadBost.ugKode shouldBe "S"
                soknadBost.datoSakReg shouldBe SAK_OPPRETTET_DATO
                soknadBost.gebyrsats shouldBe FASTSETTELSE_GEBYR_2023.toBigDecimal().setScale(1)
                soknadBost.virkningsDato shouldBe virkningDato
                soknadBost.mottatDato shouldBe soknadDato
                soknadBost.soknGrKode shouldBe "FO"
                soknadBost.soknFraKode shouldBe "MO"
                soknadBost.soknType shouldBe "FA"

                message.brev?.vedtak!! shouldHaveSize 4

                val vedtakPeriode1 = message.brev?.vedtak!![0]
                vedtakPeriode1.belopBidrag shouldBe BigDecimal(820)
                vedtakPeriode1.fomDato shouldBe periode1.fraDato
                vedtakPeriode1.tomDato shouldBe periode1.tomDato
                vedtakPeriode1.fnr shouldBe BARN1.ident.verdi
                vedtakPeriode1.resultatKode shouldBe "50"

                val vedtakPeriode2 = message.brev?.vedtak!![1]
                vedtakPeriode2.belopBidrag shouldBe BigDecimal(1250)
                vedtakPeriode2.fomDato shouldBe periode2.fraDato
                vedtakPeriode2.tomDato shouldBe periode2.tomDato
                vedtakPeriode2.fnr shouldBe BARN1.ident.verdi
                vedtakPeriode2.resultatKode shouldBe "75"

                val vedtakPeriode3 = message.brev?.vedtak!![2]
                vedtakPeriode3.belopBidrag shouldBe BigDecimal(1280)
                vedtakPeriode3.fomDato shouldBe periode3.fraDato
                vedtakPeriode3.tomDato shouldBe periode3.tomDato
                vedtakPeriode3.fnr shouldBe BARN1.ident.verdi
                vedtakPeriode3.resultatKode shouldBe "75"

                val vedtakPeriode4 = message.brev?.vedtak!![3]
                vedtakPeriode4.belopBidrag shouldBe BigDecimal(1320)
                vedtakPeriode4.fomDato shouldBe periode4.fraDato
                vedtakPeriode4.tomDato shouldBe periode4.tomDato
                vedtakPeriode4.fnr shouldBe BARN1.ident.verdi
                vedtakPeriode4.resultatKode shouldBe "75"

                message.brev?.forskuddVedtakPeriode!!.size shouldBe 4
                val forskuddVedtakPeriode1 = message.brev?.forskuddVedtakPeriode!![0]
                forskuddVedtakPeriode1.fomDato shouldBe periode1.fraDato
                forskuddVedtakPeriode1.tomDato shouldBe periode1.tomDato
                forskuddVedtakPeriode1.beløp shouldBe BigDecimal(820)
                forskuddVedtakPeriode1.fnr shouldBe BARN1.ident.verdi
                forskuddVedtakPeriode1.resultatKode shouldBe "50"
                forskuddVedtakPeriode1.prosent shouldBe "050"
                forskuddVedtakPeriode1.maksInntekt shouldBe FORSKUDD_MAKS_INNTEKT_FORSKUDD_MOTTAKER_2019_2020

                val forskuddVedtakPeriode4 = message.brev?.forskuddVedtakPeriode!![3]
                forskuddVedtakPeriode4.fomDato shouldBe periode4.fraDato
                forskuddVedtakPeriode4.tomDato shouldBe periode4.tomDato
                forskuddVedtakPeriode4.beløp shouldBe BigDecimal(1320)
                forskuddVedtakPeriode4.fnr shouldBe BARN1.ident.verdi
                forskuddVedtakPeriode4.resultatKode shouldBe "75"
                forskuddVedtakPeriode4.prosent shouldBe "075"
                forskuddVedtakPeriode4.maksInntekt shouldBe FORSKUDD_MAKS_INNTEKT_FORSKUDD_MOTTAKER_2022_2023

                message.brev?.bidragBarn!! shouldHaveSize 1
                val barn1 = message?.brev?.bidragBarn!![0]
                barn1.barn!!.fnr shouldBe BARN1.ident.verdi
                barn1.barn!!.saksnr shouldBe saksnummer

                barn1.inntektPerioder shouldHaveSize 15
                val inntekterPeriode1 = barn1.hentInntektPerioder(periode1)
                inntekterPeriode1 shouldHaveSize 3
                inntekterPeriode1[0].inntektGrense shouldBe FORSKUDD_INNTEKTGRENSE_2019_2020
                inntekterPeriode1[0].belopType shouldBe "ESBT"
                inntekterPeriode1[0].belopÅrsinntekt shouldBe BigDecimal(5000)
                inntekterPeriode1[1].belopType shouldBe "MDOK"
                inntekterPeriode1[1].belopÅrsinntekt shouldBe BigDecimal(350000)
                inntekterPeriode1[2].belopType shouldBe "XINN"
                inntekterPeriode1[2].belopÅrsinntekt shouldBe BigDecimal(355000)

                val inntekterPeriode2 = barn1.hentInntektPerioder(periode2)
                inntekterPeriode2 shouldHaveSize 3
                inntekterPeriode2[0].inntektGrense shouldBe FORSKUDD_INNTEKTGRENSE_2020_2021
                inntekterPeriode2[0].belopType shouldBe "ESBT"
                inntekterPeriode2[0].belopÅrsinntekt shouldBe BigDecimal(5000)
                inntekterPeriode2[1].belopType shouldBe "PIEO"
                inntekterPeriode2[1].belopÅrsinntekt shouldBe BigDecimal(360000)
                inntekterPeriode2[2].belopType shouldBe "XINN"
                inntekterPeriode2[2].belopÅrsinntekt shouldBe BigDecimal(365000)

                val inntekterPeriode3 = barn1.hentInntektPerioder(periode3)
                inntekterPeriode3 shouldHaveSize 4
                inntekterPeriode3[0].inntektGrense shouldBe FORSKUDD_INNTEKTGRENSE_2021_2022
                inntekterPeriode3[0].belopType shouldBe "AG"
                inntekterPeriode3[0].belopÅrsinntekt shouldBe BigDecimal(400000)
                inntekterPeriode3[1].belopType shouldBe "ESBT"
                inntekterPeriode3[1].belopÅrsinntekt shouldBe BigDecimal(5000)
                inntekterPeriode3[2].belopType shouldBe "UBAT"
                inntekterPeriode3[2].belopÅrsinntekt shouldBe BigDecimal(8000)
                inntekterPeriode3[3].belopType shouldBe "XINN"
                inntekterPeriode3[3].belopÅrsinntekt shouldBe BigDecimal(413000)

                val inntekterPeriode4 = barn1.hentInntektPerioder(periode4)
                inntekterPeriode4 shouldHaveSize 5
                inntekterPeriode4[0].inntektGrense shouldBe FORSKUDD_INNTEKTGRENSE_2022_2023
                inntekterPeriode4[0].belopType shouldBe "ESBT"
                inntekterPeriode4[0].beskrivelse shouldBe "Ekstra smÃ¥barnstillegg"
                inntekterPeriode4[0].belopÅrsinntekt shouldBe BigDecimal(2000)
                inntekterPeriode4[1].belopType shouldBe "KIEO"
                inntekterPeriode4[1].beskrivelse shouldBe "Kapitalinntekt egne opplysninger"
                inntekterPeriode4[1].belopÅrsinntekt shouldBe BigDecimal(19000)
                inntekterPeriode4[2].belopType shouldBe "PIEO"
                inntekterPeriode4[2].beskrivelse shouldBe "Personinntekt egne opplysninger"
                inntekterPeriode4[2].belopÅrsinntekt shouldBe BigDecimal(410000)
                inntekterPeriode4[3].belopType shouldBe "UBAT"
                inntekterPeriode4[3].beskrivelse shouldBe "Utvidet barnetrygd"
                inntekterPeriode4[3].belopÅrsinntekt shouldBe BigDecimal(1000)
                inntekterPeriode4[4].belopType shouldBe "XINN"
                inntekterPeriode4[4].belopÅrsinntekt shouldBe BigDecimal(432000)

                barn1.forskuddSivilstandPerioder shouldHaveSize 3
                val sivilstandPeriode1 = barn1.forskuddSivilstandPerioder[0]
                sivilstandPeriode1.fomDato shouldBe periode1.fraDato
                sivilstandPeriode1.tomDato shouldBe periode1.tomDato
                sivilstandPeriode1.kode shouldBe "GIFT"
                sivilstandPeriode1.beskrivelse shouldBe "Gift"

                val sivilstandPeriode2 = barn1.forskuddSivilstandPerioder[1]
                sivilstandPeriode2.fomDato shouldBe periode2.fraDato
                sivilstandPeriode2.tomDato shouldBe periode3.tomDato
                sivilstandPeriode2.kode shouldBe "SKIL"
                sivilstandPeriode2.beskrivelse shouldBe "Skilt"

                val sivilstandPeriode3 = barn1.forskuddSivilstandPerioder[2]
                sivilstandPeriode3.fomDato shouldBe periode4.fraDato
                sivilstandPeriode3.tomDato shouldBe MAX_DATE
                sivilstandPeriode3.kode shouldBe "SKIL"
                sivilstandPeriode3.beskrivelse shouldBe "Enke"

                // Skal være samme som message.brev.forskuddVedtakPeriode[0]
                barn1.forskuddVedtakPerioder shouldHaveSize 4
                val barnForskuddVedtakPeriode1 = barn1.forskuddVedtakPerioder[0]
                barnForskuddVedtakPeriode1.fomDato shouldBe forskuddVedtakPeriode1.fomDato
                barnForskuddVedtakPeriode1.tomDato shouldBe forskuddVedtakPeriode1.tomDato
                barnForskuddVedtakPeriode1.beløp shouldBe forskuddVedtakPeriode1.beløp
                barnForskuddVedtakPeriode1.fnr shouldBe forskuddVedtakPeriode1.fnr
                barnForskuddVedtakPeriode1.resultatKode shouldBe forskuddVedtakPeriode1.resultatKode
                barnForskuddVedtakPeriode1.prosent shouldBe forskuddVedtakPeriode1.prosent
                barnForskuddVedtakPeriode1.maksInntekt shouldBe forskuddVedtakPeriode1.maksInntekt

                barn1.forskuddBarnPerioder shouldHaveSize 2
                barn1.forskuddBarnPerioder[0].antallBarn shouldBe 1
                barn1.forskuddBarnPerioder[1].antallBarn shouldBe 3

                barn1.inntektGrunnlagForskuddPerioder shouldHaveSize 8 * 4 // 4 inntektgrenser for enslig, 4 for gift/samboer. Inntekgrenser vises for alle 4 perioder = total 8*4

                stubUtils.Verify().verifyHentEnhetKontaktInfoCalledWith()
                stubUtils.Verify().verifyHentPersonCalled(BM1.ident.verdi)
                stubUtils.Verify().verifyHentPersonCalled(BP1.ident.verdi)
                stubUtils.Verify().verifyHentPersonCalled(BARN1.ident.verdi)
                stubUtils.Verify().verifyHentPersonCalled(BARN2.ident.verdi)
                stubUtils.Verify().verifyHentPersonCalled(BARN2.ident.verdi)
            }
        }
    }

    @Test
    fun `skal produsere XML for fritekstsbrev`() {
        stubDefaultValues()
        val enhetKontaktInfo = createEnhetKontaktInformasjon()
        val bmAdresse = createPostAdresseResponse()
        val dokumentMal = DokumentMal.BI01S02
        val tittel = "Tittel på dokument"
        val saksnummer = "123213"
        val mottakerId = BM1.ident
        val gjelderId = BP1.ident

        stubUtils.stubHentAdresse(postAdresse = bmAdresse)
        stubUtils.stubEnhetKontaktInfo(enhetKontaktInfo)

        stubUtils.stubOpprettJournalpost(createOpprettJournalpostResponse(dokumentReferanse = "DOKREF_1"))

        val request = DokumentBestillingForespørsel(
            mottakerId = mottakerId.verdi,
            gjelderId = gjelderId.verdi,
            saksnummer = saksnummer,
            tittel = tittel,
            enhet = "4806",
            spraak = "NB"

        )

        jmsTestConsumer.withOnlinebrev {
            val response = httpHeaderTestRestTemplate.exchange(
                "${rootUri()}/bestill/${dokumentMal.name}",
                HttpMethod.POST,
                HttpEntity(request),
                DokumentBestillingResponse::class.java
            )

            response.statusCode shouldBe HttpStatus.OK

            val message: BrevBestilling = this.getMessageAsObject(BrevBestilling::class.java)!!
            assertSoftly {
                verifyBrevbestillingHeaders(message, dokumentMal)
                message.brev?.tknr shouldBe enhetKontaktInfo.enhetIdent
                message.brev?.spraak shouldBe "NB"
                message.brev?.brevref shouldBe "DOKREF_1"

                message.brev?.kontaktInfo?.avsender?.navn shouldBe enhetKontaktInfo.enhetNavn
                message.brev?.kontaktInfo?.tlfAvsender?.telefonnummer shouldBe "55553333"
                message.brev?.kontaktInfo?.returAdresse?.enhet shouldBe "4806"
                message.brev?.kontaktInfo?.returAdresse?.navn shouldBe enhetKontaktInfo.enhetNavn
                message.brev?.kontaktInfo?.returAdresse?.adresselinje1 shouldBe enhetKontaktInfo.postadresse?.adresselinje1
                message.brev?.kontaktInfo?.returAdresse?.adresselinje2 shouldBe enhetKontaktInfo.postadresse?.adresselinje2
                message.brev?.kontaktInfo?.returAdresse?.postnummer shouldBe enhetKontaktInfo.postadresse?.postnummer
                message.brev?.kontaktInfo?.returAdresse?.poststed shouldBe enhetKontaktInfo.postadresse?.poststed
                message.brev?.kontaktInfo?.returAdresse?.land shouldBe enhetKontaktInfo.postadresse?.land
                message.brev?.kontaktInfo?.returAdresse?.shouldBeEqualToComparingFields(message.brev?.kontaktInfo?.postadresse as BrevKontaktinfo.Adresse)

                message.brev?.mottaker?.navn shouldBe BM1.navn?.verdi
                message.brev?.mottaker?.adresselinje1 shouldBe bmAdresse.adresselinje1?.verdi
                message.brev?.mottaker?.adresselinje2 shouldBe bmAdresse.adresselinje2?.verdi
                message.brev?.mottaker?.adresselinje3 shouldBe "3030 Drammen"
                message.brev?.mottaker?.boligNr shouldBe bmAdresse.bruksenhetsnummer?.verdi
                message.brev?.mottaker?.postnummer shouldBe bmAdresse.postnummer?.verdi
                message.brev?.mottaker?.spraak shouldBe "NB"
                message.brev?.mottaker?.rolle shouldBe "01"
                message.brev?.mottaker?.fodselsnummer shouldBe BM1.ident.verdi
                message.brev?.mottaker?.fodselsdato shouldBe BM1.fødselsdato?.verdi

                message.brev?.parter?.bmfnr shouldBe BM1.ident.verdi
                message.brev?.parter?.bmnavn shouldBe BM1.fornavnEtternavn()
                message.brev?.parter?.bpfnr shouldBe BP1.ident.verdi
                message.brev?.parter?.bpnavn shouldBe BP1.fornavnEtternavn()
                message.brev?.parter?.bmfodselsdato shouldBe BM1.fødselsdato?.verdi
                message.brev?.parter?.bpfodselsdato shouldBe BP1.fødselsdato?.verdi
                message.brev?.parter?.bmkravkfremav shouldBe ""
                message.brev?.parter?.bmgebyr shouldBe ""
                message.brev?.parter?.bmlandkode shouldBe ""
                message.brev?.parter?.bpkravfremav shouldBe ""
                message.brev?.parter?.bpgebyr shouldBe ""
                message.brev?.parter?.bplandkode shouldBe ""
                message.brev?.parter?.bmdatodod shouldBe null
                message.brev?.parter?.bpdatodod shouldBe null

                message.brev?.barnISak?.shouldHaveAtLeastSize(2)
                message.brev?.barnISak?.get(0)?.fnr shouldBe BARN2.ident.verdi
                message.brev?.barnISak?.get(0)?.navn shouldBe BARN2.fornavnEtternavn()
                message.brev?.barnISak?.get(0)?.fDato shouldBe BARN2.fødselsdato?.verdi
                message.brev?.barnISak?.get(0)?.personIdRm shouldBe ""
                message.brev?.barnISak?.get(0)?.belopGebyrRm shouldBe ""
                message.brev?.barnISak?.get(0)?.belForskudd shouldBe null
                message.brev?.barnISak?.get(0)?.belBidrag shouldBe null

                message.brev?.barnISak?.get(1)?.fDato shouldBe BARN1.fødselsdato?.verdi
                message.brev?.barnISak?.get(1)?.fnr shouldBe BARN1.ident.verdi
                message.brev?.barnISak?.get(1)?.navn shouldBe BARN1.fornavnEtternavn()
                message.brev?.barnISak?.get(1)?.personIdRm shouldBe ""
                message.brev?.barnISak?.get(1)?.belopGebyrRm shouldBe ""
                message.brev?.barnISak?.get(1)?.belForskudd shouldBe null
                message.brev?.barnISak?.get(1)?.belBidrag shouldBe null

                message.brev?.soknadBost?.saksnr shouldBe saksnummer
                message.brev?.soknadBost?.sakstype shouldBe "E"
                message.brev?.soknadBost?.rmISak shouldBe false
                message.brev?.soknadBost?.sendtDato shouldBe LocalDate.now()

                message.brev?.saksbehandler?.navn shouldBe "Saksbehandler Mellomnavn Saksbehandlersen"

                stubUtils.Verify().verifyHentEnhetKontaktInfoCalledWith()
                stubUtils.Verify().verifyHentPersonCalled(BM1.ident.verdi)
                stubUtils.Verify().verifyHentPersonCalled(BP1.ident.verdi)
                stubUtils.Verify().verifyHentPersonCalled(BARN1.ident.verdi)
                stubUtils.Verify().verifyHentPersonCalled(BARN2.ident.verdi)
                stubUtils.Verify().verifyHentPersonCalled(BARN2.ident.verdi)
                stubUtils.Verify().verifyOpprettJournalpostCalledWith(
                    "{\"skalFerdigstilles\":false," +
                        "\"tittel\":\"$tittel\"," +
                        "\"gjelderIdent\":\"${gjelderId.verdi}\"," +
                        "\"avsenderMottaker\":{\"navn\":\"${BM1.navn}\",\"ident\":\"${mottakerId.verdi}\",\"type\":\"FNR\",\"adresse\":null}," +
                        "\"dokumenter\":[{\"tittel\":\"$tittel\",\"brevkode\":\"${dokumentMal.name}\"}]," +
                        "\"tilknyttSaker\":[\"$saksnummer\"]," +
                        "\"journalposttype\":\"UTGÅENDE\"," +
                        "\"journalførendeEnhet\":\"4806\"," +
                        "\"saksbehandlerIdent\":\"Z99999\"}"
                )
            }
        }
    }

    @Test
    fun `skal produsere XML for fritekstsbrev for utenlandsk adresse and engelsk språk`() {
        stubDefaultValues()
        val enhetKontaktInfo = createEnhetKontaktInformasjon()
        val bmAdresse = createPostAdresseResponseUtenlandsk()
        val dokumentMal = DokumentMal.BI01S02
        stubUtils.stubHentPersonSpraak("en")
        stubUtils.stubHentAdresse(postAdresse = bmAdresse)
        stubUtils.stubEnhetKontaktInfo(enhetKontaktInfo)

        stubUtils.stubOpprettJournalpost(createOpprettJournalpostResponse(dokumentReferanse = "DOKREF_1"))

        val request = DokumentBestillingForespørsel(
            mottakerId = BM1.ident.verdi,
            gjelderId = BP1.ident.verdi,
            saksnummer = "123213",
            tittel = "Tittel på dokument",
            enhet = "4806",
            spraak = "EN"

        )

        jmsTestConsumer.withOnlinebrev {
            val response = httpHeaderTestRestTemplate.exchange(
                "${rootUri()}/bestill/${dokumentMal.name}",
                HttpMethod.POST,
                HttpEntity(request),
                DokumentBestillingResponse::class.java
            )

            response.statusCode shouldBe HttpStatus.OK

            val message: BrevBestilling = this.getMessageAsObject(BrevBestilling::class.java)!!
            assertSoftly {
                message.brev?.spraak shouldBe "EN"
                message.brev?.mottaker?.navn shouldBe BM1.navn?.verdi
                message.brev?.mottaker?.adresselinje1 shouldBe bmAdresse.adresselinje1?.verdi
                message.brev?.mottaker?.adresselinje2 shouldBe bmAdresse.adresselinje2?.verdi
                message.brev?.mottaker?.adresselinje3 shouldBe bmAdresse.adresselinje3?.verdi
                message.brev?.mottaker?.adresselinje4 shouldBe "USA"
                message.brev?.mottaker?.boligNr shouldBe ""
                message.brev?.mottaker?.postnummer shouldBe ""
                message.brev?.mottaker?.spraak shouldBe "EN"
                message.brev?.mottaker?.rolle shouldBe "01"
                message.brev?.mottaker?.fodselsnummer shouldBe BM1.ident.verdi
                message.brev?.mottaker?.fodselsdato shouldBe BM1.fødselsdato?.verdi

                stubUtils.Verify().verifyHentEnhetKontaktInfoCalledWith("EN")
            }
        }
    }

    @Test
    fun `should use title from brevkode if title missing in request`() {
        val dokumentMal = DokumentMal.BI01S02
        stubDefaultValues()

        stubUtils.stubOpprettJournalpost(createOpprettJournalpostResponse(dokumentReferanse = "DOKREF_1"))
        val headers = HttpHeaders()
        headers.set(X_ENHET_HEADER, "4806")

        val request = DokumentBestillingForespørsel(
            mottakerId = BM1.ident.verdi,
            gjelderId = BP1.ident.verdi,
            saksnummer = "123213",
            enhet = "4806"
        )

        jmsTestConsumer.withOnlinebrev {
            val response = httpHeaderTestRestTemplate.exchange(
                "${rootUri()}/bestill/${dokumentMal.name}",
                HttpMethod.POST,
                HttpEntity(request, headers),
                DokumentBestillingResponse::class.java
            )

            response.statusCode shouldBe HttpStatus.OK

            this.getMessageAsObject(BrevBestilling::class.java)!!
            stubUtils.Verify().verifyOpprettJournalpostCalledWith("\"tittel\":\"${dokumentMal.beskrivelse}\"")
        }
    }

    @Test
    fun `should produse XML for brevkode notat`() {
        val dokumentMal = DokumentMal.BI01X01
        stubDefaultValues()

        stubUtils.stubOpprettJournalpost(createOpprettJournalpostResponse(dokumentReferanse = "DOKREF_1"))
        val request = DokumentBestillingForespørsel(
            mottakerId = BM1.ident.verdi,
            gjelderId = BP1.ident.verdi,
            saksnummer = "123213",
            enhet = "4806"
        )

        jmsTestConsumer.withOnlinebrev {
            val response = httpHeaderTestRestTemplate.exchange(
                "${rootUri()}/bestill/${dokumentMal.name}",
                HttpMethod.POST,
                HttpEntity(request),
                DokumentBestillingResponse::class.java
            )

            response.statusCode shouldBe HttpStatus.OK

            val message = this.getMessageAsObject(BrevBestilling::class.java)!!
            message.malpakke shouldContain dokumentMal.name

            stubUtils.Verify().verifyOpprettJournalpostCalledWith("\"journalposttype\":\"NOTAT\"")
            stubUtils.Verify().verifyOpprettJournalpostCalledWith("\"tittel\":\"${dokumentMal.beskrivelse}\"")
        }
    }

    @Test
    fun `should produse XML without adresse when samhandler mottaker has not adresse`() {
        val dokumentMal = DokumentMal.BI01S02
        stubDefaultValues()
        stubUtils.stubHentAdresse(postAdresse = null, status = HttpStatus.NO_CONTENT)

        stubUtils.stubOpprettJournalpost(createOpprettJournalpostResponse(dokumentReferanse = "DOKREF_1"))
        val request = DokumentBestillingForespørsel(
            gjelderId = BP1.ident.verdi,
            saksnummer = "123213",
            enhet = "4806",
            mottaker = MottakerTo(
                ident = SAMHANDLER_IDENT,
                navn = SAKSBEHANDLER_NAVN
            )
        )

        jmsTestConsumer.withOnlinebrev {
            val response = httpHeaderTestRestTemplate.exchange(
                "${rootUri()}/bestill/${dokumentMal.name}",
                HttpMethod.POST,
                HttpEntity(request),
                DokumentBestillingResponse::class.java
            )

            response.statusCode shouldBe HttpStatus.OK

            val message = this.getMessageAsObject(BrevBestilling::class.java)!!
            message.brev?.mottaker?.navn shouldBe SAKSBEHANDLER_NAVN
            message.brev?.mottaker?.adresselinje1 shouldBe ""
            message.brev?.mottaker?.adresselinje2 shouldBe ""
            message.brev?.mottaker?.adresselinje3 shouldBe ""
            message.brev?.mottaker?.boligNr shouldBe ""
            message.brev?.mottaker?.postnummer shouldBe ""
        }
    }

    @Test
    fun `should produse XML without adresse when mottaker has not adresse`() {
        val dokumentMal = DokumentMal.BI01S02
        stubDefaultValues()
        stubUtils.stubHentAdresse(postAdresse = null, status = HttpStatus.NO_CONTENT)

        stubUtils.stubOpprettJournalpost(createOpprettJournalpostResponse(dokumentReferanse = "DOKREF_1"))
        val request = DokumentBestillingForespørsel(
            gjelderId = BP1.ident.verdi,
            saksnummer = "123213",
            enhet = "4806",
            mottaker = MottakerTo(
                ident = BM1.ident.verdi
            )
        )

        jmsTestConsumer.withOnlinebrev {
            val response = httpHeaderTestRestTemplate.exchange(
                "${rootUri()}/bestill/${dokumentMal.name}",
                HttpMethod.POST,
                HttpEntity(request),
                DokumentBestillingResponse::class.java
            )

            response.statusCode shouldBe HttpStatus.OK

            val message = this.getMessageAsObject(BrevBestilling::class.java)!!
            message.brev?.mottaker?.navn shouldBe BM1.navn?.verdi
            message.brev?.mottaker?.adresselinje1 shouldBe ""
            message.brev?.mottaker?.adresselinje2 shouldBe ""
            message.brev?.mottaker?.adresselinje3 shouldBe ""
            message.brev?.mottaker?.boligNr shouldBe ""
            message.brev?.mottaker?.postnummer shouldBe ""
        }
    }

    @Test
    fun `should not send XML when opprett journalpost fails`() {
        val dokumentMal = DokumentMal.BI01X01
        stubDefaultValues()

        stubUtils.stubOpprettJournalpost(status = HttpStatus.BAD_REQUEST)
        val request = DokumentBestillingForespørsel(
            mottakerId = BM1.ident.verdi,
            gjelderId = BP1.ident.verdi,
            saksnummer = "123213",
            enhet = "4806"
        )

        jmsTestConsumer.withOnlinebrev {
            val response = httpHeaderTestRestTemplate.exchange(
                "${rootUri()}/bestill/${dokumentMal.name}",
                HttpMethod.POST,
                HttpEntity(request),
                DokumentBestillingResponse::class.java
            )

            response.statusCode shouldBe HttpStatus.BAD_REQUEST

            this.hasNoMessage() shouldBe true
        }
    }

    @Test
    fun `should fail when request with invalid brevkode`() {
        stubDefaultValues()

        val request = DokumentBestillingForespørsel(
            mottakerId = BM1.ident.verdi,
            gjelderId = BP1.ident.verdi,
            saksnummer = "123213",
            enhet = "4806"
        )

        jmsTestConsumer.withOnlinebrev {
            val response = httpHeaderTestRestTemplate.exchange(
                "${rootUri()}/bestill/BI01INVALID",
                HttpMethod.POST,
                HttpEntity(request),
                DokumentBestillingResponse::class.java
            )

            response.statusCode shouldBe HttpStatus.BAD_REQUEST

            this.hasNoMessage() shouldBe true
        }
    }

    @Test
    fun `should produse XML with mottaker role RM`() {
        val dokumentMal = DokumentMal.BI01X01
        val sak = createSakResponse().copy(
            roller = listOf(
                RolleDto(
                    fødselsnummer = BM1.ident,
                    type = Rolletype.BM
                ),
                RolleDto(
                    fødselsnummer = BP1.ident,
                    type = Rolletype.BP
                ),
                RolleDto(
                    fødselsnummer = BARN1.ident,
                    type = Rolletype.BA
                ),
                RolleDto(
                    fødselsnummer = BARN2.ident,
                    type = Rolletype.BA
                ),
                RolleDto(
                    fødselsnummer = ANNEN_MOTTAKER.ident,
                    type = Rolletype.RM
                )
            )
        )
        stubDefaultValues()
        stubUtils.stubHentSak(sak)
        stubUtils.stubOpprettJournalpost(createOpprettJournalpostResponse(dokumentReferanse = "DOKREF_1"))

        val request = DokumentBestillingForespørsel(
            mottakerId = ANNEN_MOTTAKER.ident.verdi,
            gjelderId = BP1.ident.verdi,
            saksnummer = "123213",
            enhet = "4806"
        )

        jmsTestConsumer.withOnlinebrev {
            val response = httpHeaderTestRestTemplate.exchange(
                "${rootUri()}/bestill/${dokumentMal.name}",
                HttpMethod.POST,
                HttpEntity(request),
                DokumentBestillingResponse::class.java
            )

            response.statusCode shouldBe HttpStatus.OK

            val message = this.getMessageAsObject(BrevBestilling::class.java)!!
            assertSoftly {
                message.brev?.mottaker?.rolle shouldBe "RM"
            }
        }
    }
}
