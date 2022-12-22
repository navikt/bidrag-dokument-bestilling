package no.nav.bidrag.dokument.bestilling.bestilling

import no.nav.bidrag.dokument.bestilling.api.dto.DokumentBestillingForespørsel
import no.nav.bidrag.dokument.bestilling.bestilling.produksjon.DokumentProdusent
import no.nav.bidrag.dokument.bestilling.bestilling.dto.BrevKode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentBestilling
import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentBestillingResult
import no.nav.bidrag.dokument.bestilling.model.ProduksjonAvDokumentStottesIkke
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component


@Component
class DokumentBestillingManager(val applicationContext: ApplicationContext, val dokumentMap: DokumentMap) {
    fun bestill(reqest: DokumentBestillingForespørsel, brevKode: BrevKode): DokumentBestillingResult {
        val bestillingData = buildDokumentBestilling(reqest, brevKode)
        val dokumentProducer = fetchProducer(brevKode)
        return dokumentProducer.produser(bestillingData, brevKode)
    }

    private fun fetchProducer(brevKode: BrevKode): DokumentProdusent{
        return applicationContext.getBean(brevKode.bestillingSystem, DokumentProdusent::class.java)
    }

    private fun buildDokumentBestilling(dokumentBestilling: DokumentBestillingForespørsel, brevKode: BrevKode): DokumentBestilling {
        val metadataCollector = dokumentMap[brevKode] ?: throw ProduksjonAvDokumentStottesIkke(brevKode)
        return metadataCollector.invoke(dokumentBestilling).hentBestillingData()
    }

}