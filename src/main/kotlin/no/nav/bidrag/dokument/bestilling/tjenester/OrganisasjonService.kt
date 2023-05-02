package no.nav.bidrag.dokument.bestilling.tjenester

import no.nav.bidrag.dokument.bestilling.konsumer.BidragOrganisasjonConsumer
import no.nav.bidrag.dokument.bestilling.konsumer.dto.EnhetInfo
import no.nav.bidrag.dokument.bestilling.konsumer.dto.EnhetKontaktInfoDto
import org.springframework.stereotype.Service

@Service
class OrganisasjonService(private val bidragOrganisasjonConsumer: BidragOrganisasjonConsumer) {

    fun hentEnhetInfo(enhetId: String): EnhetInfo? {
        return bidragOrganisasjonConsumer.hentEnhetInfo(enhetId)
    }

    fun hentEnhetKontaktInfo(enhetId: String, spraak: String?): EnhetKontaktInfoDto? {
        return bidragOrganisasjonConsumer.hentEnhetKontaktinfo(enhetId, spraak ?: "NB")
    }
}
