package no.nav.bidrag.dokument.bestilling.config

import no.nav.bidrag.commons.security.service.OidcTokenManager
import no.nav.bidrag.commons.security.utils.TokenUtils
import no.nav.bidrag.commons.security.utils.TokenUtils.fetchSubject
import no.nav.bidrag.dokument.bestilling.consumer.BidragOrganisasjonConsumer
import no.nav.bidrag.dokument.bestilling.model.Saksbehandler
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class SaksbehandlerInfoManager(
    private val bidragOrganisasjonConsumer: BidragOrganisasjonConsumer,
    private val oidcTokenManager: OidcTokenManager
) {
    fun hentSaksbehandlerBrukerId(): String? {
        return try {
            fetchSubject(oidcTokenManager.fetchTokenAsString())
        } catch (e: Exception) {
            null
        }
    }

    fun hentSaksbehandler(): Saksbehandler? {
        return try {
            val saksbehandlerIdent = hentSaksbehandlerBrukerId() ?: return null
            val saksbehandlerNavn = bidragOrganisasjonConsumer.hentSaksbehandlerInfo(saksbehandlerIdent)!!.navn
            Saksbehandler(saksbehandlerIdent, saksbehandlerNavn)
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