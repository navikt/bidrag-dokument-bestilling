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
    BI01P11("NOTAT P11 T", BrevType.NOTAT, BestillingSystem.BREVSERVER),
    BI01P18("Saksbehandlingsnotat", BrevType.NOTAT, BestillingSystem.BREVSERVER),
    BI01X01("REFERAT FRA SAMTALE", BrevType.NOTAT, BestillingSystem.BREVSERVER),
    BI01X02("ELEKTRONISK DIALOG", BrevType.NOTAT, BestillingSystem.BREVSERVER),
    BI01S10("KOPIFORSIDE T", BrevType.UTGAAENDE, BestillingSystem.BREVSERVER),
    BI01S67("ADRESSEFORESPØRSEL", BrevType.UTGAAENDE, BestillingSystem.BREVSERVER),
    BI01S02("Fritekstbrev", BrevType.UTGAAENDE, BestillingSystem.BREVSERVER),
    BI01S09("Varsel opphør bidrag v 18 år", BrevType.UTGAAENDE, BestillingSystem.BREVSERVER, false),
}