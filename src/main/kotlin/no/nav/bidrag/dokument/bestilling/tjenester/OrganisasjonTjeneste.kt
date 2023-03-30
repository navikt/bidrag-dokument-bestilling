package no.nav.bidrag.dokument.bestilling.tjenester

import no.nav.bidrag.dokument.bestilling.konsumer.BidragOrganisasjonKonsumer
import no.nav.bidrag.dokument.bestilling.konsumer.dto.EnhetInfo
import no.nav.bidrag.dokument.bestilling.konsumer.dto.EnhetKontaktInfoDto
import org.springframework.stereotype.Service

@Service
class OrganisasjonTjeneste(private val bidragOrganisasjonKonsumer: BidragOrganisasjonKonsumer) {

    fun hentEnhetInfo(enhetId: String): EnhetInfo? {
        return bidragOrganisasjonKonsumer.hentEnhetInfo(enhetId)
    }

    fun hentEnhetKontaktInfo(enhetId: String, spraak: String?): EnhetKontaktInfoDto? {
        return bidragOrganisasjonKonsumer.hentEnhetKontaktinfo(enhetId, spraak ?: "NB")
    }
}
