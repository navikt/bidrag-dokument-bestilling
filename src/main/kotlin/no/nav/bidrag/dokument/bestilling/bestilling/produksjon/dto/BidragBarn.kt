package no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto

import java.math.BigDecimal
import java.time.LocalDate
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter

@Suppress("unused")
@XmlRootElement(name = "bidrBarn")
@XmlAccessorType(XmlAccessType.FIELD)
class BidragBarn {
    @XmlElement(name = "barn", nillable = true)
    var barn: Barn? = null

    @XmlElement(name = "perInntekt", nillable = true)
    var inntektPerioder: MutableList<InntektPeriode> = mutableListOf()

    @XmlElement(name = "perForskBarn", nillable = true)
    var forskuddBarnPerioder: MutableList<ForskuddBarnPeriode> = mutableListOf()

    @XmlElement(name = "perForskSiv", nillable = true)
    var forskuddSivilstandPerioder: MutableList<ForskuddSivilstandPeriode> = mutableListOf()

    @XmlElement(name = "perForskVtak", nillable = true)
    var forskuddVedtakPerioder: MutableList<ForskuddVedtakPeriode> = mutableListOf()

    @XmlElement(name = "perInGrForsk", nillable = true)
    var inntektGrunnlagForskuddPerioder: MutableList<InntektGrunnlagForskuddPeriode> = mutableListOf()
    fun barn(init: Barn.() -> Unit): Barn {
        val initValue = Barn()
        initValue.init()
        barn = initValue
        return initValue
    }

    fun forskuddBarnPeriode(init: ForskuddBarnPeriode.() -> Unit): ForskuddBarnPeriode {
        val initValue = ForskuddBarnPeriode()
        initValue.init()
        forskuddBarnPerioder.add(initValue)
        return initValue
    }

    fun inntektPeriode(init: InntektPeriode.() -> Unit): InntektPeriode {
        val initValue = InntektPeriode()
        initValue.init()
        inntektPerioder.add(initValue)
        return initValue
    }

    fun forskuddSivilstandPeriode(init: ForskuddSivilstandPeriode.() -> Unit): ForskuddSivilstandPeriode {
        val initValue = ForskuddSivilstandPeriode()
        initValue.init()
        forskuddSivilstandPerioder.add(initValue)
        return initValue
    }
    fun forskuddVedtakPeriode(init: ForskuddVedtakPeriode.() -> Unit): ForskuddVedtakPeriode {
        val initValue = ForskuddVedtakPeriode()
        initValue.init()
        forskuddVedtakPerioder.add(initValue)
        return initValue
    }
    fun inntektGrunnlagForskuddPeriode(init: InntektGrunnlagForskuddPeriode.() -> Unit): InntektGrunnlagForskuddPeriode {
        val initValue = InntektGrunnlagForskuddPeriode()
        initValue.init()
        inntektGrunnlagForskuddPerioder.add(initValue)
        return initValue
    }
}

@Suppress("unused")
@XmlRootElement(name = "perForskBarn")
@XmlAccessorType(XmlAccessType.FIELD)
class ForskuddBarnPeriode {
    @XmlElement(name = "antBarn", nillable = true)
    @XmlJavaTypeAdapter(NumberAdapter::class)
    var antallBarn: Int? = 0

    @XmlElement(name = "fomDato", nillable = true)
    @XmlJavaTypeAdapter(DateAdapter::class)
    var fomDato: LocalDate? = null

    @XmlElement(name = "tomDato", nillable = true)
    @XmlJavaTypeAdapter(PeriodDateAdapter::class)
    var tomDato: LocalDate? = null
}

@Suppress("unused")
@XmlRootElement(name = "perForskSiv")
@XmlAccessorType(XmlAccessType.FIELD)
class ForskuddSivilstandPeriode {
    @XmlElement(name = "forsorgKd", nillable = true)
    var kode: String? = null

    @XmlElement(name = "forsorgBesk", nillable = true)
    var beskrivelse: String? = null

    @XmlElement(name = "fomDato", nillable = true)
    @XmlJavaTypeAdapter(DateAdapter::class)
    var fomDato: LocalDate? = null

    @XmlElement(name = "tomDato", nillable = true)
    @XmlJavaTypeAdapter(PeriodDateAdapter::class)
    var tomDato: LocalDate? = null
}

@Suppress("unused")
@XmlRootElement(name = "perForskVtak")
@XmlAccessorType(XmlAccessType.FIELD)
class ForskuddVedtakPeriode {
    @XmlElement(name = "fomDato", nillable = true)
    @XmlJavaTypeAdapter(DateAdapter::class)
    var fomDato: LocalDate? = null

    @XmlElement(name = "tomDato", nillable = true)
    @XmlJavaTypeAdapter(PeriodDateAdapter::class)
    var tomDato: LocalDate? = null

    @XmlElement(name = "maksInnt", nillable = true)
    @XmlJavaTypeAdapter(BelopAdapter::class)
    var maksInntekt: BigDecimal? = null

    @XmlElement(name = "resKd", nillable = true)
    var resultatKode: String? = null

    @XmlElement(name = "forskKd", nillable = true)
    var forskKode: String? = null

    @XmlElement(name = "forskBelop", nillable = true)
    @XmlJavaTypeAdapter(BelopAdapter::class)
    var beløp: BigDecimal? = null

    @XmlElement(name = "forskPst", nillable = true)
    var prosent: String? = null

    @XmlElement(name = "fnr", nillable = true)
    var fnr: String? = null
}

@Suppress("unused")
@XmlRootElement(name = "perForskVtak")
@XmlAccessorType(XmlAccessType.FIELD)
class InntektGrunnlagForskuddPeriode {
    @XmlElement(name = "fomDato", nillable = true)
    @XmlJavaTypeAdapter(DateAdapter::class)
    var fomDato: LocalDate? = null

    @XmlElement(name = "tomDato", nillable = true)
    @XmlJavaTypeAdapter(DateAdapter::class)
    var tomDato: LocalDate? = null

    @XmlElement(name = "forsorgKd", nillable = true)
    var forsorgerKode: String? = null

    @XmlElement(name = "antBarn", nillable = true)
    @XmlJavaTypeAdapter(NumberAdapter::class)
    var antallBarn: Int? = 0

    @XmlElement(name = "belop50fra", nillable = true)
    @XmlJavaTypeAdapter(BelopAdapter::class)
    var belop50fra: BigDecimal? = null

    @XmlElement(name = "belop50til", nillable = true)
    @XmlJavaTypeAdapter(BelopAdapter::class)
    var belop50til: BigDecimal? = null

    @XmlElement(name = "belop75fra", nillable = true)
    @XmlJavaTypeAdapter(BelopAdapter::class)
    var belop75fra: BigDecimal? = null

    @XmlElement(name = "belop75til", nillable = true)
    @XmlJavaTypeAdapter(BelopAdapter::class)
    var belop75til: BigDecimal? = null
}

@Suppress("unused")
@XmlRootElement(name = "perInntekt")
@XmlAccessorType(XmlAccessType.FIELD)
class InntektPeriode {
    @XmlElement(name = "fnr", nillable = true)
    var fnr: String? = null

    @XmlElement(name = "fomDato", nillable = true)
    @XmlJavaTypeAdapter(DateAdapter::class)
    var fomDato: LocalDate? = null

    @XmlElement(name = "tomDato", nillable = true)
    @XmlJavaTypeAdapter(PeriodDateAdapter::class)
    var tomDato: LocalDate? = null

    @XmlElement(name = "belopTyp", nillable = true)
    var belopType: String? = null

    @XmlElement(name = "belopAar", nillable = true)
    @XmlJavaTypeAdapter(BelopAdapter::class)
    var belopÅrsinntekt: BigDecimal? = null

    @XmlElement(name = "bidrRolle", nillable = true)
    var rolle: String? = null

    @XmlElement(name = "belopBeskr", nillable = true)
    var beskrivelse: String? = null

    @XmlElement(name = "inntGrense", nillable = true)
    @XmlJavaTypeAdapter(BelopAdapter::class)
    var inntektGrense: BigDecimal? = null
}

@Suppress("unused")
@XmlRootElement(name = "barn")
@XmlAccessorType(XmlAccessType.FIELD)
class Barn {
    @XmlElement(name = "navn", nillable = true)
    var navn: String? = null

    @XmlElement(name = "fnr", nillable = true)
    var fnr: String? = null

    @XmlElement(name = "fDato", nillable = true)
    @XmlJavaTypeAdapter(BirthDateAdapter::class)
    var fDato: LocalDate? = null

    @XmlElement(name = "saksnr", nillable = true)
    var saksnr: String? = null
}
