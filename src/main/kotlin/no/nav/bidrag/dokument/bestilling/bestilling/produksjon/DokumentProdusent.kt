package no.nav.bidrag.dokument.bestilling.bestilling.produksjon

import no.nav.bidrag.dokument.bestilling.bestilling.dto.BrevKode
import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentBestilling
import no.nav.bidrag.dokument.bestilling.bestilling.dto.DokumentBestillingResult

interface DokumentProdusent {


    fun produser(dokumentBestilling: DokumentBestilling, brevKode: BrevKode): DokumentBestillingResult
}