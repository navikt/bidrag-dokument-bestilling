package no.nav.bidrag.dokument.bestilling.model


data class Saksbehandler(
    val ident: String? = null,
    val navn: String? = null
) {
    val fornavnEtternavn: String get() = run {
            if (navn.isNullOrEmpty()) {
                return navn ?: ""
            }
            val navnDeler = navn.split(",\\s*".toRegex(), limit = 2).toTypedArray()
            return if (navnDeler.size > 1) {
                navnDeler[1] + " " + navnDeler[0]
            } else navn
        }
}
