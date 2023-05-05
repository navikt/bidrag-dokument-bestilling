package no.nav.bidrag.dokument.bestilling.bestilling

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import no.nav.bidrag.dokument.bestilling.config.SaksbehandlerInfoManager
import no.nav.bidrag.dokument.bestilling.consumer.KodeverkConsumer
import no.nav.bidrag.dokument.bestilling.tjenester.KodeverkService
import no.nav.bidrag.dokument.bestilling.tjenester.OrganisasjonService
import no.nav.bidrag.dokument.bestilling.tjenester.PersonService
import no.nav.bidrag.dokument.bestilling.tjenester.SakService
import no.nav.bidrag.dokument.bestilling.tjenester.SjablongService
import no.nav.bidrag.dokument.bestilling.tjenester.VedtakService
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.ApplicationContext

@ExtendWith(MockKExtension::class)

internal class DokumentMapTest {
    @RelaxedMockK
    lateinit var personService: PersonService

    @RelaxedMockK
    lateinit var sakService: SakService
    @RelaxedMockK
    lateinit var sjablonService: SjablongService
    @RelaxedMockK
    lateinit var vedtakService: VedtakService
    @RelaxedMockK
    lateinit var kodeverkConsumer: KodeverkConsumer

    @RelaxedMockK
    lateinit var saksbehandlerInfoManager: SaksbehandlerInfoManager

    @RelaxedMockK
    lateinit var organisasjonService: OrganisasjonService

    @InjectMockKs
    lateinit var kodeverkService: KodeverkService

    @InjectMockKs
    lateinit var metadataCollector: DokumentMetadataCollector

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
    fun `Notater should add mottaker and gjelder`() {
    }
}
