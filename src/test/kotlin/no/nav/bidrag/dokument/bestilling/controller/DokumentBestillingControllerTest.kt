package no.nav.bidrag.dokument.bestilling.controller

import StubUtils
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.ninjasquad.springmockk.MockkBean
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeIn
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.equality.shouldNotBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import no.nav.bidrag.commons.web.EnhetFilter.X_ENHET_HEADER
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.dokument.bestilling.BidragDokumentBestillingLocalTest
import no.nav.bidrag.dokument.bestilling.JmsTestConfig
import no.nav.bidrag.dokument.bestilling.consumer.KodeverkConsumer
import no.nav.bidrag.dokument.bestilling.model.BrevBestilling
import no.nav.bidrag.dokument.bestilling.model.BrevKode
import no.nav.bidrag.dokument.bestilling.model.BrevKontaktinfo
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingRequest
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingResponse
import no.nav.bidrag.dokument.bestilling.model.KodeverkResponse
import no.nav.bidrag.dokument.bestilling.model.RolleType
import no.nav.bidrag.dokument.bestilling.model.SakRolle
import no.nav.bidrag.dokument.bestilling.utils.ANNEN_MOTTAKER
import no.nav.bidrag.dokument.bestilling.utils.BARN1
import no.nav.bidrag.dokument.bestilling.utils.BARN2
import no.nav.bidrag.dokument.bestilling.utils.BM1
import no.nav.bidrag.dokument.bestilling.utils.BP1
import no.nav.bidrag.dokument.bestilling.utils.DEFAULT_SAKSNUMMER
import no.nav.bidrag.dokument.bestilling.utils.JmsTestConsumer
import no.nav.bidrag.dokument.bestilling.utils.SAKSBEHANDLER_IDENT
import no.nav.bidrag.dokument.bestilling.utils.SAKSBEHANDLER_NAVN
import no.nav.bidrag.dokument.bestilling.utils.createEnhetKontaktInformasjon
import no.nav.bidrag.dokument.bestilling.utils.createOpprettJournalpostResponse
import no.nav.bidrag.dokument.bestilling.utils.createPostAdresseResponse
import no.nav.bidrag.dokument.bestilling.utils.createPostAdresseResponseUtenlandsk
import no.nav.bidrag.dokument.bestilling.utils.createSakResponse
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import javax.jms.Queue


@ActiveProfiles("test")
@SpringBootTest(
    classes = [BidragDokumentBestillingLocalTest::class, StubUtils::class, JmsTestConfig::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWireMock(port = 0)
@EnableMockOAuth2Server
class DokumentBestillingControllerTest {

    @LocalServerPort
    private val port = 0

    @MockkBean
    lateinit var kodeverkConsumer: KodeverkConsumer

    @Autowired
    lateinit var stubUtils: StubUtils

    @Autowired
    lateinit var httpHeaderTestRestTemplate: HttpHeaderTestRestTemplate

    @Autowired
    lateinit var onlineBrevQueue: Queue

    @Autowired
    lateinit var onlinebrevTemplate: JmsTemplate

    @Autowired
    lateinit var jmsTestConsumer: JmsTestConsumer

    @BeforeEach
    fun initMocks() {
        stubUtils.stubEnhetInfo()
        stubUtils.stubEnhetKontaktInfo()
        stubUtils.stubHentSaksbehandlerInfo()
        stubUtils.stubHentPersonSpraak()
        val kodeverkResponse = ObjectMapper().findAndRegisterModules().readValue(readFile("api/landkoder.json"), KodeverkResponse::class.java)
        every { kodeverkConsumer.hentLandkoder() } returns kodeverkResponse
    }

    @AfterEach
    fun resetMocks() {
        WireMock.reset()
    }

    fun stubDefaultValues(){
        stubUtils.stubHentPerson(BP1.ident, BP1)
        stubUtils.stubHentPerson(BM1.ident, BM1)
        stubUtils.stubHentPerson(BARN1.ident, BARN1)
        stubUtils.stubHentPerson(BARN2.ident, BARN2)
        stubUtils.stubHentPerson(ANNEN_MOTTAKER.ident, ANNEN_MOTTAKER)
        stubUtils.stubHentSak()
        stubUtils.stubHentPersonSpraak()
        stubUtils.stubHentAdresse(postAdresse = createPostAdresseResponse())
        stubUtils.stubEnhetKontaktInfo(createEnhetKontaktInformasjon())
    }

    @Test
    fun `skal returnere liste over brevkoder som er støttet`(){
        val response = httpHeaderTestRestTemplate.exchange(
            "${rootUri()}/brevkoder",
            HttpMethod.OPTIONS,
            null,
            List::class.java
        )

        response.body?.forEach {
            it shouldBeIn BrevKode.values().map { bk -> bk.name }
            it shouldNotBeIn BrevKode.values().filter{bk -> !bk.enabled}.map { bk -> bk.name }
        }

        response.body?.shouldHaveSize(BrevKode.values().filter { it.enabled }.size)
    }
    @Test
    fun `skal produsere XML for fritekstsbrev`() {
        stubDefaultValues()
        val enhetKontaktInfo = createEnhetKontaktInformasjon()
        val bmAdresse = createPostAdresseResponse()
        val brevKode = BrevKode.BI01S02
        val tittel = "Tittel på dokument"
        val saksnummer = "123213"
        val mottakerId = BM1.ident
        val gjelderId = BP1.ident

        stubUtils.stubHentAdresse(postAdresse = bmAdresse)
        stubUtils.stubEnhetKontaktInfo(enhetKontaktInfo)

        stubUtils.stubOpprettJournalpost(createOpprettJournalpostResponse(dokumentReferanse = "DOKREF_1"))

        val request = DokumentBestillingRequest(
            mottakerId = mottakerId,
            gjelderId = gjelderId,
            saksnummer = saksnummer,
            tittel = tittel,
            enhet = "4806",
            spraak = "NB"

        )

        jmsTestConsumer.withOnlinebrev {
            val response = httpHeaderTestRestTemplate.exchange(
                "${rootUri()}/bestill/${brevKode.name}",
                HttpMethod.POST,
                HttpEntity(request),
                DokumentBestillingResponse::class.java
            )

            response.statusCode shouldBe HttpStatus.OK

            val message: BrevBestilling = this.getMessageAsObject(BrevBestilling::class.java)!!
            assertSoftly {
                verifyBrevbestillingHeaders(message, brevKode)
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

                message.brev?.mottaker?.navn shouldBe BM1.navn
                message.brev?.mottaker?.adresselinje1 shouldBe bmAdresse.adresselinje1
                message.brev?.mottaker?.adresselinje2 shouldBe bmAdresse.adresselinje2
                message.brev?.mottaker?.adresselinje3 shouldBe "3030 Drammen"
                message.brev?.mottaker?.boligNr shouldBe bmAdresse.bruksenhetsnummer
                message.brev?.mottaker?.postnummer shouldBe bmAdresse.postnummer
                message.brev?.mottaker?.spraak shouldBe "NB"
                message.brev?.mottaker?.rolle shouldBe "01"
                message.brev?.mottaker?.fodselsnummer shouldBe BM1.ident
                message.brev?.mottaker?.fodselsdato shouldBe BM1.foedselsdato

                message.brev?.parter?.bmfnr shouldBe BM1.ident
                message.brev?.parter?.bmnavn shouldBe BM1.fornavnEtternavn
                message.brev?.parter?.bpfnr shouldBe BP1.ident
                message.brev?.parter?.bpnavn shouldBe BP1.fornavnEtternavn
                message.brev?.parter?.bmfodselsdato shouldBe BM1.foedselsdato
                message.brev?.parter?.bpfodselsdato shouldBe BP1.foedselsdato
                message.brev?.parter?.bmkravkfremav shouldBe ""
                message.brev?.parter?.bmgebyr shouldBe ""
                message.brev?.parter?.bmlandkode shouldBe ""
                message.brev?.parter?.bpkravfremav shouldBe ""
                message.brev?.parter?.bpgebyr shouldBe ""
                message.brev?.parter?.bplandkode shouldBe ""
                message.brev?.parter?.bmdatodod shouldBe null
                message.brev?.parter?.bpdatodod shouldBe null

                message.brev?.barnISak?.shouldHaveAtLeastSize(2)
                message.brev?.barnISak?.get(0)?.fnr shouldBe BARN1.ident
                message.brev?.barnISak?.get(0)?.navn shouldBe BARN1.fornavnEtternavn
                message.brev?.barnISak?.get(0)?.fDato shouldBe BARN1.foedselsdato
                message.brev?.barnISak?.get(0)?.personIdRm shouldBe ""
                message.brev?.barnISak?.get(0)?.belopGebyrRm shouldBe ""
                message.brev?.barnISak?.get(0)?.belForskudd shouldBe ""
                message.brev?.barnISak?.get(0)?.belBidrag shouldBe ""

                message.brev?.barnISak?.get(1)?.fDato shouldBe BARN2.foedselsdato
                message.brev?.barnISak?.get(1)?.fnr shouldBe BARN2.ident
                message.brev?.barnISak?.get(1)?.navn shouldBe BARN2.fornavnEtternavn
                message.brev?.barnISak?.get(1)?.personIdRm shouldBe ""
                message.brev?.barnISak?.get(1)?.belopGebyrRm shouldBe ""
                message.brev?.barnISak?.get(1)?.belForskudd shouldBe ""
                message.brev?.barnISak?.get(1)?.belBidrag shouldBe ""

                message.brev?.soknad?.saksnr shouldBe saksnummer
                message.brev?.soknad?.sakstype shouldBe "E"
                message.brev?.soknad?.rmISak shouldBe false
                message.brev?.soknad?.sendtDato shouldBe LocalDate.now()

                message.brev?.saksbehandler?.navn shouldBe SAKSBEHANDLER_NAVN

                stubUtils.Verify().verifyHentEnhetKontaktInfoCalledWith()
                stubUtils.Verify().verifyHentPersonCalled(BM1.ident)
                stubUtils.Verify().verifyHentPersonCalled(BP1.ident)
                stubUtils.Verify().verifyHentPersonCalled(BARN1.ident)
                stubUtils.Verify().verifyHentPersonCalled(BARN2.ident)
                stubUtils.Verify().verifyHentPersonCalled(BARN2.ident)
                stubUtils.Verify().verifyOpprettJournalpostCalledWith("{" +
                        "\"tittel\":\"$tittel\"," +
                        "\"gjelder\":{\"ident\":\"$gjelderId\",\"type\":null}," +
                        "\"avsenderMottaker\":" +
                            "{\"navn\":\"${BM1.navn}\",\"ident\":\"$mottakerId\",\"type\":\"UKJENT\"}," +
                        "\"dokumenter\":[" +
                            "{\"tittel\":\"$tittel\",\"brevkode\":\"${brevKode.name}\",\"dokumentreferanse\":null,\"dokument\":null}" +
                        "]," +
                        "\"tilknyttSaker\":[\"$saksnummer\"]," +
                        "\"behandlingstema\":null,\"tema\":null," +
                        "\"journalposttype\":\"UTGAAENDE\"," +
                        "\"referanseId\":null," +
                        "\"journalfoerendeEnhet\":\"4806\"," +
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
        val brevKode = BrevKode.BI01S02
        stubUtils.stubHentPersonSpraak("en")
        stubUtils.stubHentAdresse(postAdresse = bmAdresse)
        stubUtils.stubEnhetKontaktInfo(enhetKontaktInfo)

        stubUtils.stubOpprettJournalpost(createOpprettJournalpostResponse(dokumentReferanse = "DOKREF_1"))

        val request = DokumentBestillingRequest(
            mottakerId = BM1.ident,
            gjelderId = BP1.ident,
            saksnummer = "123213",
            tittel = "Tittel på dokument",
            enhet = "4806",
            spraak = "EN"

        )

        jmsTestConsumer.withOnlinebrev {
            val response = httpHeaderTestRestTemplate.exchange(
                "${rootUri()}/bestill/${brevKode.name}",
                HttpMethod.POST,
                HttpEntity(request),
                DokumentBestillingResponse::class.java
            )

            response.statusCode shouldBe HttpStatus.OK

            val message: BrevBestilling = this.getMessageAsObject(BrevBestilling::class.java)!!
            assertSoftly {

                message.brev?.spraak shouldBe "EN"
                message.brev?.mottaker?.navn shouldBe BM1.navn
                message.brev?.mottaker?.adresselinje1 shouldBe bmAdresse.adresselinje1
                message.brev?.mottaker?.adresselinje2 shouldBe bmAdresse.adresselinje2
                message.brev?.mottaker?.adresselinje3 shouldBe bmAdresse.adresselinje3
                message.brev?.mottaker?.adresselinje4 shouldBe "USA"
                message.brev?.mottaker?.boligNr shouldBe ""
                message.brev?.mottaker?.postnummer shouldBe ""
                message.brev?.mottaker?.spraak shouldBe "EN"
                message.brev?.mottaker?.rolle shouldBe "01"
                message.brev?.mottaker?.fodselsnummer shouldBe BM1.ident
                message.brev?.mottaker?.fodselsdato shouldBe BM1.foedselsdato

                stubUtils.Verify().verifyHentEnhetKontaktInfoCalledWith("EN")
            }
        }

    }

    @Test
    fun `should use title from brevkode if title missing in request`(){
        val brevKode = BrevKode.BI01S02
        stubDefaultValues()

        stubUtils.stubOpprettJournalpost(createOpprettJournalpostResponse(dokumentReferanse = "DOKREF_1"))
        val headers = HttpHeaders()
        headers.set(X_ENHET_HEADER, "4806")

        val request = DokumentBestillingRequest(
            mottakerId = BM1.ident,
            gjelderId = BP1.ident,
            saksnummer = "123213",
            enhet = "4806"
        )

        jmsTestConsumer.withOnlinebrev {
            val response = httpHeaderTestRestTemplate.exchange(
                "${rootUri()}/bestill/${brevKode.name}",
                HttpMethod.POST,
                HttpEntity(request, headers),
                DokumentBestillingResponse::class.java
            )

            response.statusCode shouldBe HttpStatus.OK

            this.getMessageAsObject(BrevBestilling::class.java)!!
            stubUtils.Verify().verifyOpprettJournalpostCalledWith("\"tittel\":\"${brevKode.beskrivelse}\"")
        }
    }

    @Test
    fun `should produse XML for brevkode notat`(){
        val brevKode = BrevKode.BI01X01
        stubDefaultValues()

        stubUtils.stubOpprettJournalpost(createOpprettJournalpostResponse(dokumentReferanse = "DOKREF_1"))
        val request = DokumentBestillingRequest(
            mottakerId = BM1.ident,
            gjelderId = BP1.ident,
            saksnummer = "123213",
            enhet = "4806"
        )

        jmsTestConsumer.withOnlinebrev {
            val response = httpHeaderTestRestTemplate.exchange(
                "${rootUri()}/bestill/${brevKode.name}",
                HttpMethod.POST,
                HttpEntity(request),
                DokumentBestillingResponse::class.java
            )

            response.statusCode shouldBe HttpStatus.OK

            val message = this.getMessageAsObject(BrevBestilling::class.java)!!
            message.malpakke shouldContain brevKode.name

            stubUtils.Verify().verifyOpprettJournalpostCalledWith("\"journalposttype\":\"NOTAT\"")
            stubUtils.Verify().verifyOpprettJournalpostCalledWith("\"tittel\":\"${brevKode.beskrivelse}\"")
        }
    }

    @Test
    fun `should not send XML when opprett journalpost fails`(){
        val brevKode = BrevKode.BI01X01
        stubDefaultValues()

        stubUtils.stubOpprettJournalpost(status = HttpStatus.BAD_REQUEST)
        val request = DokumentBestillingRequest(
            mottakerId = BM1.ident,
            gjelderId = BP1.ident,
            saksnummer = "123213",
            enhet = "4806"
        )

        jmsTestConsumer.withOnlinebrev {
            val response = httpHeaderTestRestTemplate.exchange(
                "${rootUri()}/bestill/${brevKode.name}",
                HttpMethod.POST,
                HttpEntity(request),
                DokumentBestillingResponse::class.java
            )

            response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR

            this.hasNoMessage() shouldBe true
        }
    }

    @Test
    fun `should produse XML with mottaker role RM`(){
        val brevKode = BrevKode.BI01X01
        val sak = createSakResponse().copy(
            roller = listOf(
                SakRolle(
                    foedselsnummer = BM1.ident,
                    rolleType = RolleType.BM
                ),
                SakRolle(
                    foedselsnummer = BP1.ident,
                    rolleType = RolleType.BP
                ),
                SakRolle(
                    foedselsnummer = BARN1.ident,
                    rolleType = RolleType.BA
                ),
                SakRolle(
                    foedselsnummer = BARN2.ident,
                    rolleType = RolleType.BA
                ),
                SakRolle(
                    foedselsnummer = ANNEN_MOTTAKER.ident,
                    rolleType = RolleType.RM
                )
            )
        )
        stubDefaultValues()
        stubUtils.stubHentSak(sak)
        stubUtils.stubOpprettJournalpost(createOpprettJournalpostResponse(dokumentReferanse = "DOKREF_1"))

        val request = DokumentBestillingRequest(
            mottakerId = ANNEN_MOTTAKER.ident,
            gjelderId = BP1.ident,
            saksnummer = "123213",
            enhet = "4806"
        )

        jmsTestConsumer.withOnlinebrev {
            val response = httpHeaderTestRestTemplate.exchange(
                "${rootUri()}/bestill/${brevKode.name}",
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
    fun verifyBrevbestillingHeaders(bestilling: BrevBestilling, brevKode: BrevKode) {
        bestilling.passord shouldBe "pass"
        bestilling.sysid shouldBe "BI12"
        bestilling.arkiver shouldBe "JA"
        bestilling.format shouldBe "ENSIDIG"
        bestilling.skrivertype shouldBe "LOKAL"
        bestilling.skuff shouldBe ""
        bestilling.skriver shouldBe ""
        bestilling.saksbehandler shouldBe SAKSBEHANDLER_IDENT
        bestilling.malpakke shouldBe "BI01.${brevKode.name}"
    }

    fun rootUri(): String {
        return "http://localhost:$port"
    }

    fun readFile(filePath: String): String {
        return String(ClassPathResource("testdata/$filePath").inputStream.readAllBytes())
    }

}