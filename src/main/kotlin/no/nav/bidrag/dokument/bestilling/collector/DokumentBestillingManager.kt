package no.nav.bidrag.dokument.bestilling.collector

import no.nav.bidrag.dokument.bestilling.model.BrevKode
import no.nav.bidrag.dokument.bestilling.model.DokumentBestilling
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingRequest
import no.nav.bidrag.dokument.bestilling.model.DokumentBestillingResult
import no.nav.bidrag.dokument.bestilling.model.ProduksjonAvDokumentStottesIkke
import no.nav.bidrag.dokument.bestilling.producer.DokumentProducer
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component


@Component
class DokumentBestillingManager(var applicationContext: ApplicationContext, val dokumentMap: DokumentMap) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(DokumentBestillingManager::class.java)
    }

    fun bestill(reqest: DokumentBestillingRequest, brevKode: BrevKode): DokumentBestillingResult {
        val bestillingData = buildDokumentBestilling(reqest, brevKode)
        val dokumentProducer = fetchProducer(brevKode)
        return dokumentProducer.produce(bestillingData, brevKode)
    }

    private fun fetchProducer(brevKode: BrevKode): DokumentProducer{
        return applicationContext.getBean(brevKode.bestillingSystem, DokumentProducer::class.java)
    }

    private fun buildDokumentBestilling(dokumentBestilling: DokumentBestillingRequest, brevKode: BrevKode): DokumentBestilling {
        val metadataCollector = dokumentMap[brevKode] ?: throw ProduksjonAvDokumentStottesIkke(brevKode)
        return metadataCollector.invoke(dokumentBestilling).getBestillingData()
    }

}