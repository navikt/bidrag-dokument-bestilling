package no.nav.bidrag.dokument.bestilling.konfigurasjon.cache

import no.nav.bidrag.dokument.bestilling.konfigurasjon.SaksbehandlerInfoManager
import no.nav.bidrag.dokument.bestilling.model.SYSTEMBRUKER_ID
import org.springframework.cache.interceptor.SimpleKeyGenerator
import java.lang.reflect.Method

class UserCacheKeyGenerator(private val saksbehandlerInfoManager: SaksbehandlerInfoManager) : SimpleKeyGenerator() {
    override fun generate(target: Any, method: Method, vararg params: Any): Any {
        return toUserCacheKey(super.generate(target, method, *params))
    }

    private fun toUserCacheKey(key: Any): UserCacheKey {
        val userId = if (saksbehandlerInfoManager.erSystembruker()) SYSTEMBRUKER_ID else saksbehandlerInfoManager.hentSaksbehandlerBrukerId()
        return UserCacheKey(userId ?: "UKJENT", key)
    }
}