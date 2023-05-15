package no.nav.bidrag.dokument.bestilling.api

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeIn
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import no.nav.bidrag.commons.web.EnhetFilter.Companion.X_ENHET_HEADER
import no.nav.bidrag.dokument.bestilling.api.dto.DokumentBestillingForespørsel
import no.nav.bidrag.dokument.bestilling.api.dto.DokumentBestillingResponse
import no.nav.bidrag.dokument.bestilling.api.dto.MottakerTo
import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentMal
import no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto.BrevBestilling
import no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto.BrevKontaktinfo
import no.nav.bidrag.dokument.bestilling.consumer.dto.fornavnEtternavn
import no.nav.bidrag.dokument.bestilling.utils.ANNEN_MOTTAKER
import no.nav.bidrag.dokument.bestilling.utils.BARN1
import no.nav.bidrag.dokument.bestilling.utils.BARN2
import no.nav.bidrag.dokument.bestilling.utils.BM1
import no.nav.bidrag.dokument.bestilling.utils.BP1
import no.nav.bidrag.dokument.bestilling.utils.SAKSBEHANDLER_NAVN
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
import java.time.LocalDate

@Disabled // i påvente av activemq-broker-jakarta 5.19.0
class DokumentBestillingControllerTest : AbstractControllerTest() {

    @Test
    fun `skal returnere liste over brevkoder som er støttet`() {
        val response = httpHeaderTestRestTemplate.optionsForEntity<List<String>>(
            "${rootUri()}/brevkoder"
        )

        response.body?.forEach {
            it shouldBeIn DokumentMal.values().map { bk -> bk.name }
            it shouldNotBeIn DokumentMal.values().filter { bk -> !bk.enabled }.map { bk -> bk.name }
        }

        response.body?.shouldHaveSize(DokumentMal.values().filter { it.enabled }.size)
    }

    @Test
    fun `skal produsere XML for forskudd vedtakbrev`() {
        stubDefaultValues()
        stubUtils.stubHentPerson("16451299577", ANNEN_MOTTAKER)
        stubUtils.stubHentPerson("25451755601", ANNEN_MOTTAKER)
        stubUtils.stubHentVedtak("vedtak_forskudd_enkel_108.json")
        val enhetKontaktInfo = createEnhetKontaktInformasjon()
        val bmAdresse = createPostAdresseResponse()
        val dokumentMal = DokumentMal.BI01A01
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
            vedtakId = "12312",
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
            val response = httpHeaderTestRestTemplate.postForEntity<DokumentBestillingResponse>(
                "${rootUri()}/bestill/${dokumentMal.name}",
                HttpEntity(request)
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
                message.brev?.kontaktInfo?.returAdresse
                    ?.shouldBeEqualToComparingFields(message.brev?.kontaktInfo?.postadresse as BrevKontaktinfo.Adresse)

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
            val response = httpHeaderTestRestTemplate.postForEntity<DokumentBestillingResponse>(
                "${rootUri()}/bestill/${dokumentMal.name}",
                HttpEntity(request)
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
            val response = httpHeaderTestRestTemplate.postForEntity<DokumentBestillingResponse>(
                "${rootUri()}/bestill/${dokumentMal.name}",
                HttpEntity(request, headers)
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
            val response = httpHeaderTestRestTemplate.postForEntity<DokumentBestillingResponse>(
                "${rootUri()}/bestill/${dokumentMal.name}",
                HttpEntity(request)
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
            val response = httpHeaderTestRestTemplate.postForEntity<DokumentBestillingResponse>(
                "${rootUri()}/bestill/${dokumentMal.name}",
                HttpEntity(request)
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
            val response = httpHeaderTestRestTemplate.postForEntity<DokumentBestillingResponse>(
                "${rootUri()}/bestill/${dokumentMal.name}",
                HttpEntity(request)
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
            val response = httpHeaderTestRestTemplate.postForEntity<DokumentBestillingResponse>(
                "${rootUri()}/bestill/${dokumentMal.name}",
                HttpEntity(request)
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
            val response = httpHeaderTestRestTemplate.postForEntity<DokumentBestillingResponse>(
                "${rootUri()}/bestill/BI01INVALID",
                HttpEntity(request)
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
            val response = httpHeaderTestRestTemplate.postForEntity<DokumentBestillingResponse>(
                "${rootUri()}/bestill/${dokumentMal.name}",
                HttpEntity(request)
            )

            response.statusCode shouldBe HttpStatus.OK

            val message = this.getMessageAsObject(BrevBestilling::class.java)!!
            assertSoftly {
                message.brev?.mottaker?.rolle shouldBe "RM"
            }
        }
    }
}
