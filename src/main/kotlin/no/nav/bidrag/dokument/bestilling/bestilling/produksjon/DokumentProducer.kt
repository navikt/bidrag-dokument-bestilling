package no.nav.bidrag.dokument.bestilling.bestilling.produksjon

import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentBestilling
import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentBestillingResult
import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentMal

interface DokumentProducer {
    fun produser(
        dokumentBestilling: DokumentBestilling,
        dokumentMal: DokumentMal,
    ): DokumentBestillingResult
}
