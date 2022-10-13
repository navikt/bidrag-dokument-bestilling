package no.nav.bidrag.dokument.bestilling.model

import java.text.ParseException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.adapters.XmlAdapter
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter


var BREV_DATETIME_FORMAT = DateTimeFormatter.ofPattern("ddMMyy")
var BREV_SOKNAD_DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd")

@XmlRootElement(name = "rtv-brev")
@XmlAccessorType(XmlAccessType.FIELD)
class BrevBestilling {

    @XmlAttribute
    var sysid: String = "BI12"

    @XmlAttribute
    var arkiver: String = "JA"

    @XmlAttribute
    var direkteutskrift: String = "NEI"

    @XmlAttribute
    var format: String = "ENSIDIG"

    @XmlAttribute
    var skriver: String = ""

    @XmlAttribute
    var skrivertype: String = "LOKAL"

    @XmlAttribute
    var skuff: String = ""

    @XmlAttribute
    lateinit var malpakke: String

    @XmlAttribute
    lateinit var passord: String

    @XmlAttribute
    lateinit var saksbehandler: String

    var brev: Brev? = null
}

@XmlRootElement(name = "brev")
@XmlAccessorType(XmlAccessType.FIELD)
class Brev {
    @XmlAttribute
    lateinit var tknr: String
    @XmlAttribute
    lateinit var spraak: String
    @XmlAttribute
    lateinit var brevref: String
    @XmlElement(name = "brevMottaker")
    var mottaker: BrevMottaker? = null
    @XmlElement(name = "parter")
    var parter: Parter? = null
    @XmlElement(name = "soknBost")
    var soknad: Soknad? = null
    @XmlElement(name = "Kontaktinfo")
    var kontaktInfo: BrevKontaktinfo? = null
    @XmlElement(name = "Saksbehandl")
    var saksbehandler: BrevSaksbehandler? = null
}


@Suppress("unused")
@XmlRootElement(name = "Saksbehandl")
@XmlAccessorType(XmlAccessType.FIELD)
class BrevSaksbehandler() {

    @XmlElement(name = "saksbNavn")
    var navn: String? = null

}

@XmlRootElement(name = "brevMottaker")
@XmlAccessorType(XmlAccessType.FIELD)
class BrevMottaker {
    @XmlElement(name = "navn", nillable = true)
    var navn: String? = null
    @XmlElement(name = "adr1", nillable = true)
    var adresselinje1: String? = null
    @XmlElement(name = "adr2", nillable = true)
    var adresselinje2: String? = null
    @XmlElement(name = "adr3", nillable = true)
    var adresselinje3: String? = null
    @XmlElement(name = "adr4", nillable = true)
    var adresselinje4: String? = null
    @XmlElement(name = "boligNr", nillable = true)
    var boligNr: String? = null
    @XmlElement(name = "bidrRolle", nillable = true)
    var rolle: String? = null
    @XmlElement(name = "fnr", nillable = true)
    var fodselsnummer: String? = null
    @XmlElement(name = "fdato", nillable = true)
    var fodselsdato: String? = null
    @XmlElement(name = "postnr", nillable = true)
    var postnummer: String? = null
    @XmlElement(name = "landKd", nillable = true)
    var landkode: String? = null
    @XmlElement(name = "spraak", nillable = true)
    var spraak: String? = null

}

@Suppress("unused")
@XmlRootElement(name = "Kontaktinfo")
@XmlAccessorType(XmlAccessType.FIELD)
class BrevKontaktinfo {
    @XmlElement(name = "NavnAvsender", nillable = true)
    var avsender: Avsender? = null
    @XmlElement(name = "TelfAvsender", nillable = true)
    var tlfAvsender: TlfAvsender? = null
    @XmlElement(name = "Returadr", nillable = true)
    var returAdresse: Adresse? = null
    @XmlElement(name = "Postadr", nillable = true)
    var postadresse: Adresse? = null

    @XmlRootElement(name = "NavnAvsender")
    @XmlAccessorType(XmlAccessType.FIELD)
    class Avsender {
        @XmlElement(name = "NavnAvsEnh", nillable = true)
        var navn: String? = null
    }

    @XmlRootElement(name = "TelfAvsender")
    @XmlAccessorType(XmlAccessType.FIELD)
    class TlfAvsender {
        @XmlElement(name = "TelfAvsEnh", nillable = true)
        var telefonnummer: String? = null
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    class Adresse {
        @XmlElement(name = "NavEnhId", nillable = true)
        var enhet: String? = null
        @XmlElement(name = "NavEnhNavn", nillable = true)
        var navn: String? = null
        @XmlElement(name = "Telefon", nillable = true)
        var telefon: String? = null
        @XmlElement(name = "AdrLinje1", nillable = true)
        var adresselinje1: String? = null
        @XmlElement(name = "AdrLinje2", nillable = true)
        var adresselinje2: String? = null
        @XmlElement(name = "PostNr", nillable = true)
        var postnummer: String? = null
        @XmlElement(name = "PostSted", nillable = true)
        var poststed: String? = null
        @XmlElement(name = "Land", nillable = true)
        var land: String? = null
    }


    fun adresse(init: Adresse.() -> Unit): Adresse {
        val adresse = Adresse()
        adresse.init()
        return adresse
    }


    fun tlfAvsender(init: TlfAvsender.() -> Unit): TlfAvsender {
        val tlfAvsender = TlfAvsender()
        tlfAvsender.init()
        return tlfAvsender
    }

    fun avsender(init: Avsender.() -> Unit): Avsender {
        val avsender = Avsender()
        avsender.init()
        return avsender
    }
}

@Suppress("unused")
@XmlRootElement(name = "parter")
@XmlAccessorType(XmlAccessType.FIELD)
class Parter {
    @XmlElement(name = "bpfnr", nillable = true)
    var bpfnr: String? = null
    @XmlElement(name = "bpNavn", nillable = true)
    var bpnavn: String? = null
    @XmlElement(name = "bpfDato", nillable = true)
    var bpfodselsdato: String? = null
    @XmlElement(name = "bpKravFremAv", nillable = true)
    var bpkravfremav: String? = null
    @XmlElement(name = "bpbelopGebyr", nillable = true)
    var bpgebyr: String? = "00000000000"


    @XmlElement(name = "bmfnr", nillable = true)
    var bmfnr: String? = null
    @XmlElement(name = "bmNavn", nillable = true)
    var bmnavn: String? = null
    @XmlElement(name = "bmfDato", nillable = true)
    var bmfodselsdato: String? = null
    @XmlElement(name = "bmKravFremAv", nillable = true)
    var bmkravkfremav: String? = null
    @XmlElement(name = "bmbelopGebyr", nillable = true)
    var bmgebyr: String? = "00000000000"
    @XmlElement(name = "bmLandKd", nillable = true)
    var bmlandkode: String? = null
    @XmlElement(name = "bpLandKd", nillable = true)
    var bplandkode: String? = null
    @XmlElement(name = "bmDatoDod", nillable = true)
    var bmdatodod: String? = null
    @XmlElement(name = "bpDatoDod", nillable = true)
    var bpdatodod: String? = null
}

@Suppress("unused")
@XmlRootElement(name = "soknBost")
@XmlAccessorType(XmlAccessType.FIELD)
class Soknad {
    @XmlElement(name = "saksnr", nillable = true)
    var saksnr: String? = null
    @XmlElement(name = "BBFogd", nillable = true)
    var bbfogd: String? = null
    @XmlElement(name = "BPFogd", nillable = true)
    var bpfogd: String? = null
    @XmlElement(name = "sakstype", nillable = true)
    var sakstype: String? = null
    @XmlElement(name = "jounalkode", nillable = true)
    var bpgebyr: String? = null
    @XmlElement(name = "indexRegDato", nillable = true)
    var indexRegDato: String? = null
    @XmlElement(name = "indexRegPro", nillable = true)
    var indexRegPro: String? = "00000"

    @XmlElement(name = "hgKode", nillable = true)
    var hgKode: String? = "XX"
    @XmlElement(name = "ugKode", nillable = true)
    var ugKode: String? = null
    @XmlElement(name = "datoSakReg", nillable = true)
    var datoSakReg: String? = LocalDate.now().format(BREV_SOKNAD_DATETIME_FORMAT)
    @XmlElement(name = "resKode", nillable = true)
    var resKode: String? = null
    @XmlElement(name = "datoVtak", nillable = true)
    var datoVedtak: String? = null
    @XmlElement(name = "rmISak", nillable = true)
    @XmlJavaTypeAdapter(BooleanAdapter::class)
    var rmISak: Boolean? = false
    @XmlElement(name = "forskUtBet", nillable = true)
    @XmlJavaTypeAdapter(BooleanAdapter::class)
    var forskUtBet: Boolean? = false
    @XmlElement(name = "sendtDato")
    var sendtDato: String? = LocalDate.now().format(BREV_SOKNAD_DATETIME_FORMAT)
    @XmlElement(name = "gebyrsats", nillable = true)
    var gebyrsats: String? = "01223.0"
    @XmlElement(name = "innkrSamtid")
    @XmlJavaTypeAdapter(BooleanAdapter::class)
    var innkrSamtid: Boolean? = false
    @XmlElement(name = "mottDato", nillable = true)
    var mottatDato: String? = null
    @XmlElement(name = "virknDato", nillable = true)
    var virkningsDato: String? = null
    @XmlElement(name = "myndighet", nillable = true)
    var myndighet: String? = null
    @XmlElement(name = "ffuRefNr", nillable = true)
    var ffuRefNr: String? = null
    @XmlElement(name = "konv", nillable = true)
    var konv: String? = null
    @XmlElement(name = "soknGrKode", nillable = true)
    var soknGrKode: String? = null
    @XmlElement(name = "soknFraKode", nillable = true)
    var soknFraKode: String? = null
    @XmlElement(name = "soknType", nillable = true)
    var soknType: String? = null
    @XmlElement(name = "b4Kode", nillable = true)
    var b4Kode: String? = "0"
    @XmlElement(name = "b4Belop", nillable = true)
    var b4Belop: String? = "00000000000"
}
fun brevbestilling(init: BrevBestilling.() -> Unit): BrevBestilling {
    val brevBestilling = BrevBestilling()
    brevBestilling.init()
    return brevBestilling
}

fun brev(init: Brev.() -> Unit): Brev {
    val brev = Brev()
    brev.init()
    return brev
}

fun brevmottaker(init: BrevMottaker.() -> Unit): BrevMottaker {
    val brevMottaker = BrevMottaker()
    brevMottaker.init()
    return brevMottaker
}

fun soknad(init: Soknad.() -> Unit): Soknad {
    val soknad = Soknad()
    soknad.init()
    return soknad
}

fun parter(init: Parter.() -> Unit): Parter {
    val parter = Parter()
    parter.init()
    return parter
}

fun brevKontaktinfo(init: BrevKontaktinfo.() -> Unit): BrevKontaktinfo {
    val brevKontaktinfo = BrevKontaktinfo()
    brevKontaktinfo.init()
    return brevKontaktinfo
}

fun brevSaksbehandler(init: BrevSaksbehandler.() -> Unit): BrevSaksbehandler {
    val brevSaksbehandler = BrevSaksbehandler()
    brevSaksbehandler.init()
    return brevSaksbehandler
}


class BooleanAdapter: XmlAdapter<String, Boolean?>() {
    override fun marshal(v: Boolean?): String {
        return if (v == true) "J" else "N"
    }

    @Throws(ParseException::class)
    override fun unmarshal(v: String): Boolean {
        return v == "J"
    }
}