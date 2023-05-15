package no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto

import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlRootElement
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter
import java.math.BigDecimal
import java.time.LocalDate

@Suppress("unused")
@XmlRootElement(name = "bidrVtak")
@XmlAccessorType(XmlAccessType.FIELD)
class BidragVedtak {
    @XmlElement(name = "fomDato", nillable = true)
    @XmlJavaTypeAdapter(DateAdapter::class)
    var fomDato: LocalDate? = null

    @XmlElement(name = "tomDato", nillable = true)
    @XmlJavaTypeAdapter(PeriodDateAdapter::class)
    var tomDato: LocalDate? = null

    @XmlElement(name = "fnr", nillable = true)
    var fnr: String? = null

    @XmlElement(name = "belopBidr", nillable = true)
    @XmlJavaTypeAdapter(BelopAdapter::class)
    var belopBidrag: BigDecimal? = null

    @XmlElement(name = "belopTlg", nillable = true)
    var belopTillegg: String? = null

    @XmlElement(name = "innkr", nillable = true)
    var erInnkreving: String? = null

    @XmlElement(name = "soktTlgBidJN", nillable = true)
    @XmlJavaTypeAdapter(BooleanAdapter::class)
    var soktTillegg: Boolean? = null

    @XmlElement(name = "resKd", nillable = true)
    var resultatKode: String? = null
}
