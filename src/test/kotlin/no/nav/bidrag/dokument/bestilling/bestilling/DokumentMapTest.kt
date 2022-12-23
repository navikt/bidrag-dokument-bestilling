package no.nav.bidrag.dokument.bestilling.bestilling

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import no.nav.bidrag.dokument.bestilling.konfigurasjon.SaksbehandlerInfoManager
import no.nav.bidrag.dokument.bestilling.konsumer.KodeverkKonsumer
import no.nav.bidrag.dokument.bestilling.tjenester.KodeverkTjeneste
import no.nav.bidrag.dokument.bestilling.tjenester.OrganisasjonTjeneste
import no.nav.bidrag.dokument.bestilling.tjenester.PersonTjeneste
import no.nav.bidrag.dokument.bestilling.tjenester.SakTjeneste
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.ApplicationContext

@ExtendWith(MockKExtension::class)

internal class DokumentMapTest {
    @RelaxedMockK
    lateinit var personService: PersonTjeneste
    @RelaxedMockK
    lateinit var sakService: SakTjeneste
    @RelaxedMockK
    lateinit var kodeverkKonsumer: KodeverkKonsumer
    @RelaxedMockK
    lateinit var saksbehandlerInfoManager: SaksbehandlerInfoManager
    @RelaxedMockK
    lateinit var organisasjonService: OrganisasjonTjeneste
    @InjectMockKs
    lateinit var kodeverkTjeneste: KodeverkTjeneste
    @InjectMockKs
    lateinit var metadataCollector: DokumentMetadataInnsamler
    @MockK
    lateinit var applicationContext: ApplicationContext

    @InjectMockKs
    lateinit var dokumentMap: DokumentMap

    @Test
    @Disabled
    fun validateBrevkoder() {
      /*  every { sakService.hentSak(any()) } returns HentSakResponse(roller = listOf(SakRolle("123", RolleType.BM)))
        every { applicationContext.getBean(DokumentMetadataCollector::class.java) } returns metadataCollector
        dokumentMap.forEach{ (brevkode, value) ->
            withClue("Feil brevkode lagt til i DokumentMap.kt for metadatacollector konfigurert for brevkode=$brevkode ") {
                value.invoke(DokumentBestillingRequest("", gjelderId = "123", saksnummer = "", saksbehandler = Saksbehandler("123", "21313"))).erSammeBrevkode(brevkode) shouldBe true }
        }*/
    }

    @Test
    fun `Notater should add mottaker and gjelder`(){

    }
}