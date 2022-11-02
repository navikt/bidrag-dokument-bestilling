package no.nav.bidrag.dokument.bestilling.model

object BestillingSystem {
    const val BREVSERVER = "BREVSERVER"
}

enum class BrevType {
    UTGAAENDE,
    NOTAT
}
typealias BestillingSystemType = String

enum class BrevKode(val beskrivelse: String, var brevtype: BrevType, val bestillingSystem: BestillingSystemType, val enabled: Boolean = true) {
    BI01S02("Fritekstbrev", BrevType.UTGAAENDE, BestillingSystem.BREVSERVER),
    BI01B01("Vedtaksbrev", BrevType.UTGAAENDE, BestillingSystem.BREVSERVER, false),
    BI01P18("Saksbehandlingsnotat", BrevType.NOTAT, BestillingSystem.BREVSERVER)
}