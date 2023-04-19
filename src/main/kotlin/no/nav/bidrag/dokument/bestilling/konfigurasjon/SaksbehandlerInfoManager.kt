package no.nav.bidrag.dokument.bestilling.konfigurasjon

import no.nav.bidrag.commons.security.service.OidcTokenManager
import no.nav.bidrag.commons.security.utils.TokenUtils
import no.nav.bidrag.commons.security.utils.TokenUtils.fetchSubject
import no.nav.bidrag.dokument.bestilling.konsumer.BidragOrganisasjonKonsumer
import no.nav.bidrag.dokument.bestilling.model.Saksbehandler
import org.springframework.stereotype.Service

@Service
class SaksbehandlerInfoManager(
    private val bidragOrganisasjonKonsumer: BidragOrganisasjonKonsumer,
    private val oidcTokenManager: OidcTokenManager
) {
    fun hentSaksbehandlerBrukerId(): String? {
        return try {
            fetchSubject(oidcTokenManager.fetchTokenAsString())
        } catch (e: Exception) {
            null
        }
    }

    fun hentSaksbehandler(ident: String? = null): Saksbehandler? {
        return try {
            val saksbehandlerIdent = ident ?: hentSaksbehandlerBrukerId() ?: return null
            val saksbehandlerNavn = bidragOrganisasjonKonsumer.hentSaksbehandlerInfo(saksbehandlerIdent)?.navn
            Saksbehandler(saksbehandlerIdent, saksbehandlerNavn?.verdi)
        } catch (e: Exception) {
            null
        }
    }

    fun erSystembruker(): Boolean {
        return try {
            TokenUtils.isSystemUser(oidcTokenManager.fetchTokenAsString())
        } catch (e: Exception) {
            false
        }
    }
}
