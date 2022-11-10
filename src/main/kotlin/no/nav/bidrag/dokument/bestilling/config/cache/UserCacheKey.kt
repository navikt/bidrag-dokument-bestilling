package no.nav.bidrag.dokument.bestilling.config.cache

import org.apache.commons.lang3.builder.HashCodeBuilder

class UserCacheKey(private val userId: String, private val key: Any) {
    override fun equals(other: Any?): Boolean {
        if (other is UserCacheKey) {
            return userId == other.userId && key == other.key
        }
        return false
    }

    override fun hashCode(): Int {
        return HashCodeBuilder()
            .append(userId)
            .append(key)
            .toHashCode()
    }

    override fun toString(): String {
        return StringBuilder()
            .append(userId)
            .append(" - ")
            .append(key)
            .toString()
    }

    companion object {
        const val GENERATOR_BEAN = "UserCacheKeyGenerator"
    }
}