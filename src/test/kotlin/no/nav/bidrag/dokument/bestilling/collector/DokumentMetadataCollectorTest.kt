package no.nav.bidrag.dokument.bestilling.collector

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import no.nav.bidrag.dokument.bestilling.config.SaksbehandlerInfoManager
import no.nav.bidrag.dokument.bestilling.consumer.KodeverkConsumer
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingRequest
import no.nav.bidrag.dokument.bestilling.model.KodeverkResponse
import no.nav.bidrag.dokument.bestilling.model.RolleType
import no.nav.bidrag.dokument.bestilling.model.Saksbehandler
import no.nav.bidrag.dokument.bestilling.model.SaksbehandlerInfoResponse
import no.nav.bidrag.dokument.bestilling.service.KodeverkService
import no.nav.bidrag.dokument.bestilling.service.OrganisasjonService
import no.nav.bidrag.dokument.bestilling.service.PersonService
import no.nav.bidrag.dokument.bestilling.service.SakService
import no.nav.bidrag.dokument.bestilling.utils.BARN1
import no.nav.bidrag.dokument.bestilling.utils.BARN2
import no.nav.bidrag.dokument.bestilling.utils.BM1
import no.nav.bidrag.dokument.bestilling.utils.BP1
import no.nav.bidrag.dokument.bestilling.utils.SAKSBEHANDLER_IDENT
import no.nav.bidrag.dokument.bestilling.utils.SAKSBEHANDLER_NAVN
import no.nav.bidrag.dokument.bestilling.utils.createEnhetKontaktInformasjon
import no.nav.bidrag.dokument.bestilling.utils.createPostAdresseResponse
import no.nav.bidrag.dokument.bestilling.utils.createSakResponse
import no.nav.bidrag.dokument.bestilling.utils.readFile
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class DokumentMetadataCollectorTest {

    @MockK
    lateinit var personService: PersonService
    @MockK
    lateinit var sakService: SakService
    @MockK
    lateinit var kodeverkConsumer: KodeverkConsumer
    @MockK
    lateinit var saksbehandlerInfoManager: SaksbehandlerInfoManager
    @MockK
    lateinit var organisasjonService: OrganisasjonService
    @RelaxedMockK
    lateinit var kodeverkService: KodeverkService
    @InjectMockKs
    lateinit var metadataCollector: DokumentMetadataCollector


    @BeforeEach
    fun initMocks(){
        val kodeverkResponse = ObjectMapper().findAndRegisterModules().readValue(readFile("api/landkoder.json"), KodeverkResponse::class.java)
        every { kodeverkConsumer.hentLandkoder() } returns kodeverkResponse
        mockDefaultValues()
    }

    @AfterEach
    fun resetMocks(){
       clearAllMocks()
    }

    fun mockDefaultValues(){
        every { personService.hentPerson(BM1.ident, any()) } returns BM1
        every { personService.hentPerson(BP1.ident, any()) } returns BP1
        every { personService.hentPerson(BARN1.ident, any()) } returns BARN1
        every { personService.hentPerson(BARN2.ident, any()) } returns BARN2
        every { personService.hentSpraak(any()) } returns "NB"

        every { organisasjonService.hentEnhetKontaktInfo(any(), any()) } returns createEnhetKontaktInformasjon()
        every { sakService.hentSak(any()) } returns createSakResponse()
        every { saksbehandlerInfoManager.hentSaksbehandler() } returns Saksbehandler(SAKSBEHANDLER_IDENT, SAKSBEHANDLER_NAVN)
        every { saksbehandlerInfoManager.hentSaksbehandlerBrukerId() } returns SAKSBEHANDLER_IDENT
    }

    @Test
    fun shouldMapToBestillingRequest(){

        val adresseResponse = createPostAdresseResponse()
        val defaultKontaktinfo = createEnhetKontaktInformasjon()
        every { personService.hentPersonAdresse(any(), any()) } returns adresseResponse
        val request = DokumentBestillingRequest(
            mottakerId = BM1.ident,
            gjelderId = BM1.ident,
            saksnummer = "123213123",
            tittel = "Tittel på dokument",
            enhet = "4806",
            spraak = "NB"
        )
        val bestilling = metadataCollector.init(request).addCommonMetadata().getBestillingData()

        bestilling.mottaker?.spraak shouldBe "NB"
        bestilling.mottaker?.navn shouldBe BM1.navn
        bestilling.mottaker?.fodselsnummer shouldBe BM1.ident
        bestilling.mottaker?.rolle shouldBe RolleType.BM
        bestilling.mottaker?.fodselsdato shouldBe BM1.foedselsdato
        bestilling.mottaker?.adresse?.adresselinje1 shouldBe adresseResponse.adresselinje1
        bestilling.mottaker?.adresse?.adresselinje2 shouldBe adresseResponse.adresselinje2
        bestilling.mottaker?.adresse?.adresselinje3 shouldBe adresseResponse.adresselinje3
        bestilling.mottaker?.adresse?.adresselinje4 shouldBe null
        bestilling.mottaker?.adresse?.bruksenhetsnummer shouldBe adresseResponse.bruksenhetsnummer
        bestilling.mottaker?.adresse?.postnummer shouldBe adresseResponse.postnummer
        bestilling.mottaker?.adresse?.poststed shouldBe adresseResponse.poststed
        bestilling.mottaker?.adresse?.landkode shouldBe adresseResponse.land
        bestilling.mottaker?.adresse?.landkode3 shouldBe adresseResponse.land3
        bestilling.mottaker?.adresse?.land shouldBe ""

        bestilling.gjelder?.fodselsnummer shouldBe BM1.ident
        bestilling.gjelder?.rolle shouldBe RolleType.BM

        bestilling.kontaktInfo?.navn shouldBe defaultKontaktinfo.enhetNavn
        bestilling.kontaktInfo?.telefonnummer shouldBe defaultKontaktinfo.telefonnummer

        bestilling.spraak shouldBe "NB"
        bestilling.saksnummer shouldBe "123213123"
        bestilling.tittel shouldBe "Tittel på dokument"
        bestilling.enhet shouldBe "4806"
    }
}