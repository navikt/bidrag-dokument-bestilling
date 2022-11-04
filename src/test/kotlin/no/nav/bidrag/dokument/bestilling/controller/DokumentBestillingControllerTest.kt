package no.nav.bidrag.dokument.bestilling.controller

import StubUtils
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import no.nav.bidrag.commons.web.EnhetFilter.X_ENHET_HEADER
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.dokument.bestilling.BidragDokumentBestillingLocalTest
import no.nav.bidrag.dokument.bestilling.JmsTestConfig
import no.nav.bidrag.dokument.bestilling.consumer.KodeverkConsumer
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingRequest
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingResponse
import no.nav.bidrag.dokument.bestilling.model.HentPersonResponse
import no.nav.bidrag.dokument.bestilling.model.KodeverkResponse
import no.nav.bidrag.dokument.bestilling.utils.BARN_ID_1
import no.nav.bidrag.dokument.bestilling.utils.BARN_ID_2
import no.nav.bidrag.dokument.bestilling.utils.BARN_NAVN_1
import no.nav.bidrag.dokument.bestilling.utils.BARN_NAVN_2
import no.nav.bidrag.dokument.bestilling.utils.BM_PERSON_ID_1
import no.nav.bidrag.dokument.bestilling.utils.BM_PERSON_NAVN_1
import no.nav.bidrag.dokument.bestilling.utils.BP_PERSON_ID_1
import no.nav.bidrag.dokument.bestilling.utils.BP_PERSON_NAVN_1
import no.nav.bidrag.dokument.bestilling.utils.JmsTestConsumer
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
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
import java.time.LocalDateTime
import javax.jms.Queue


@ActiveProfiles("test")
@SpringBootTest(classes = [BidragDokumentBestillingLocalTest::class, StubUtils::class, JmsTestConfig::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@EnableMockOAuth2Server
class DokumentBestillingControllerTest {

    @LocalServerPort
    private val port = 0

    @MockBean
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

    val localDateMock = Mockito.mockStatic(LocalDate::class.java, Mockito.CALLS_REAL_METHODS)

    @AfterEach
    fun `remove time mock`(){
        localDateMock.close()
    }

    @BeforeEach
    fun resetMocks(){
        WireMock.reset()
        val kodeverkResponse = ObjectMapper().findAndRegisterModules().readValue(readFile("api/landkoder.json"), KodeverkResponse::class.java)
        whenever(kodeverkConsumer.hentLandkoder()).thenReturn(kodeverkResponse)
    }

    @Test
    fun `skal produsere XML for fritekstsbrev`(){
        val mockTime = LocalDate.parse("2022-09-10")
        whenever(LocalDate.now()).thenReturn(mockTime)
        stubUtils.stubHentPerson(BP_PERSON_ID_1, HentPersonResponse(BP_PERSON_ID_1, BP_PERSON_NAVN_1, LocalDate.parse("2020-05-06"), null, "213213213"))
        stubUtils.stubHentPerson(BM_PERSON_ID_1, HentPersonResponse(BM_PERSON_ID_1, BM_PERSON_NAVN_1, LocalDate.parse("2020-05-06"), null, "213213213"))
        stubUtils.stubHentPerson(BARN_ID_1, HentPersonResponse(BARN_ID_1, BARN_NAVN_1, LocalDate.parse("2020-05-06"), null, "213213213"))
        stubUtils.stubHentPerson(BARN_ID_2, HentPersonResponse(BARN_ID_2, BARN_NAVN_2, LocalDate.parse("2020-05-06"), null, "213213213"))
        stubUtils.stubHentAdresse()
        stubUtils.stubEnhetInfo()
        stubUtils.stubEnhetKontaktInfo()
        stubUtils.stubHentSaksbehandlerInfo()
        stubUtils.stubHentSak()
        stubUtils.stubHentPersonSpraak()

        stubUtils.stubOpprettJournalpost()
        val headers = HttpHeaders()
        headers.set(X_ENHET_HEADER, "4806")

        val request = DokumentBestillingRequest(
            mottakerId = BM_PERSON_ID_1,
            gjelderId = BP_PERSON_ID_1,
            saksnummer = "123213",
        )


        jmsTestConsumer.withOnlinebrev {
            val response = httpHeaderTestRestTemplate.exchange("${rootUri()}/bestill/BI01S02", HttpMethod.POST, HttpEntity(request, headers), DokumentBestillingResponse::class.java)

            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

            val message = this.getMessageAsString()

            println(message)
//            assertThat(message).isEqualTo(readFile("xml/simpel_fritekstbrev.xml"))

            stubUtils.Verify().verifyHentEnhetKontaktInfoCalledWith()
        }


    }

    fun rootUri(): String{
        return "http://localhost:$port"
    }

    fun readFile(filePath: String): String{
        return String(ClassPathResource("testdata/$filePath").inputStream.readAllBytes())
    }

}