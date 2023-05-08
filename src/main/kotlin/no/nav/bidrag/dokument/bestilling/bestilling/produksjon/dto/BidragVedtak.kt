package no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto

import java.math.BigDecimal
import java.time.LocalDate
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter

@Suppress("unused")
@XmlRootElement(name = "bidrVtak")
@XmlAccessorType(XmlAccessType.FIELD)
class BidragVedtak {
    @XmlElement(name = "fnr", nillable = true)
    var fnr: String? = null

    @XmlElement(name = "belopBidr", nillable = true)
    @XmlJavaTypeAdapter(BelopAdapter::class)
    var belopBidrag: BigDecimal? = null

    @XmlElement(name = "belopTlg", nillable = true)
    var belopTillegg: String? = null

    @XmlElement(name = "innkr", nillable = true)
    var innkr: String? = null

    @XmlElement(name = "soktTlgBidJN", nillable = true)
    @XmlJavaTypeAdapter(BooleanAdapter::class)
    var soktTillegg: Boolean? = null

    @XmlElement(name = "resKd", nillable = true)
    var resultatKode: String? = null

    @XmlElement(name = "fomDato", nillable = true)
    @XmlJavaTypeAdapter(DateAdapter::class)
    var fomDato: LocalDate? = null

    @XmlElement(name = "tomDato", nillable = true)
    @XmlJavaTypeAdapter(PeriodDateAdapter::class)
    var tomDato: LocalDate? = null
}
