package no.nav.bidrag.dokument.bestilling.api

import StubUtils
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.dokument.bestilling.BidragDokumentBestillingLocalTest
import no.nav.bidrag.dokument.bestilling.JmsTestKonfig
import no.nav.bidrag.dokument.bestilling.konsumer.KodeverkConsumer
import no.nav.bidrag.dokument.bestilling.model.BrevBestilling
import no.nav.bidrag.dokument.bestilling.model.BrevKode
import no.nav.bidrag.dokument.bestilling.konsumer.dto.KodeverkResponse
import no.nav.bidrag.dokument.bestilling.utils.ANNEN_MOTTAKER
import no.nav.bidrag.dokument.bestilling.utils.BARN1
import no.nav.bidrag.dokument.bestilling.utils.BARN2
import no.nav.bidrag.dokument.bestilling.utils.BM1
import no.nav.bidrag.dokument.bestilling.utils.BP1
import no.nav.bidrag.dokument.bestilling.utils.JmsTestConsumer
import no.nav.bidrag.dokument.bestilling.utils.SAKSBEHANDLER_IDENT
import no.nav.bidrag.dokument.bestilling.utils.createEnhetKontaktInformasjon
import no.nav.bidrag.dokument.bestilling.utils.createPostAdresseResponse
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.core.io.ClassPathResource
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.ActiveProfiles
import javax.jms.Queue
@ActiveProfiles("test")
@SpringBootTest(
    classes = [BidragDokumentBestillingLocalTest::class, StubUtils::class, JmsTestKonfig::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWireMock(port = 0)
@EnableMockOAuth2Server
abstract class AbstractControllerTest {
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
        clearAllMocks()
    }

    fun rootUri(): String {
        return "http://localhost:$port"
    }

    fun readFile(filePath: String): String {
        return String(ClassPathResource("testdata/$filePath").inputStream.readAllBytes())
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
}