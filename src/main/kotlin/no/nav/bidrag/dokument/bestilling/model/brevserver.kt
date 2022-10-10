package no.nav.bidrag.dokument.bestilling.model

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

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

    var mottaker: BrevMottaker? = null
    var soknad: Soknad? = null
    var parter: Parter? = null
    var saksbehandler: BrevSaksbehandler? = null

    var kontaktInfo: BrevKontaktinfo? = null
}


@Suppress("unused")
@XmlRootElement(name = "Saksbehandl")
@XmlAccessorType(XmlAccessType.FIELD)
class BrevSaksbehandler {

    @XmlElement(name = "saksbNavn")
    lateinit var navn: String

}

@XmlRootElement(name = "brevMottaker")
@XmlAccessorType(XmlAccessType.FIELD)
class BrevMottaker {
    var spraak: String? = null
    var navn: String? = null

    @XmlElement(name = "fnr")
    var fodselsnummer: String? = null
    @XmlElement(name = "bidrRolle")
    var rolle: String? = null
    @XmlElement(name = "fdato")
    var fodselsdato: String? = null

    @XmlElement(name = "adr1")
    var adresselinje1: String? = null
    @XmlElement(name = "adr2")
    var adresselinje2: String? = null
    @XmlElement(name = "adr3")
    var adresselinje3: String? = null
    @XmlElement(name = "adr4")
    var adresselinje4: String? = null
    var boligNr: String? = null
    @XmlElement(name = "postnr")
    var postnummer: String? = null

    @XmlElement(name = "landKd")
    var landkode: String? = null

}

@Suppress("unused")
@XmlRootElement(name = "Kontaktinfo")
@XmlAccessorType(XmlAccessType.FIELD)
class BrevKontaktinfo {
    var avsender: Avsender? = null
    var tlfAvsender: TlfAvsender? = null
    @XmlElement(name = "Returadr")
    var returAdresse: Adresse? = null
    @XmlElement(name = "Postadr")
    var postadresse: Adresse? = null

    @XmlRootElement(name = "NavnAvsender")
    @XmlAccessorType(XmlAccessType.FIELD)
    class Avsender {
        @XmlElement(name = "NavnAvsEnh")
        var navn: String? = null
    }

    @XmlRootElement(name = "TelfAvsender")
    @XmlAccessorType(XmlAccessType.FIELD)
    class TlfAvsender {
        @XmlElement(name = "TelfAvsEnh")
        var telefonnummer: String? = null
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    class Adresse {
        @XmlElement(name = "NavEnhId")
        var enhet: String? = null
        @XmlElement(name = "NavEnhNavn")
        var navn: String? = null
        @XmlElement(name = "Telefon")
        var telefon: String? = null
        @XmlElement(name = "AdrLinje1")
        var adresselinje1: String? = null
        @XmlElement(name = "AdrLinje2")
        var adresselinje2: String? = null
        @XmlElement(name = "PostNr")
        var postnummer: String? = null
        @XmlElement(name = "PostSted")
        var poststed: String? = null
        @XmlElement(name = "Land")
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
    @XmlElement(name = "bpfnr")
    var bpfnr: String? = null
    @XmlElement(name = "bpNavn")
    var bpnavn: String? = null
    @XmlElement(name = "bpfDato")
    var bpfodselsdato: String? = null
    @XmlElement(name = "bpKravFremAv")
    var bpkravfremav: String? = null
    @XmlElement(name = "bpbelopGebyr")
    var bpgebyr: String? = null
    @XmlElement(name = "bpLandKd")
    var bplandkode: String? = null
    @XmlElement(name = "bpDatoDod")
    var bpdatodod: String? = null

    @XmlElement(name = "bmfnr")
    var bmfnr: String? = null
    @XmlElement(name = "bmNavn")
    var bmnavn: String? = null
    @XmlElement(name = "bmfDato")
    var bmfodselsdato: String? = null
    @XmlElement(name = "bmKravFremAv")
    var bmkravkfremav: String? = null
    @XmlElement(name = "bmbelopGebyr")
    var bmgebyr: String? = null
    @XmlElement(name = "bmLandKd")
    var bmlandkode: String? = null
    @XmlElement(name = "bmDatoDod")
    var bmdatodod: String? = null
}

@Suppress("unused")
@XmlRootElement(name = "soknBost")
@XmlAccessorType(XmlAccessType.FIELD)
class Soknad {
    @XmlElement(name = "saksnr")
    var saksnr: String? = null
    @XmlElement(name = "BBFogd")
    var bbfogd: String? = null
    @XmlElement(name = "BPFogd")
    var bpfogd: String? = null
    @XmlElement(name = "sakstype")
    var sakstype: String? = null
    @XmlElement(name = "jounalkode")
    var bpgebyr: String? = null
    @XmlElement(name = "indexRegDato")
    var indexRegDato: String? = null
    @XmlElement(name = "indexRegPro")
    var indexRegPro: String? = null

    @XmlElement(name = "hgKode")
    var hgKode: String? = null
    @XmlElement(name = "ugKode")
    var ugKode: String? = null
    @XmlElement(name = "datoSakReg")
    var datoSakReg: String? = null
    @XmlElement(name = "resKode")
    var resKode: String? = null
    @XmlElement(name = "datoVtak")
    var datoVedtak: String? = null
    @XmlElement(name = "rmISak")
    var rmISak: String? = null
    @XmlElement(name = "forskUtBet")
    var forskUtBet: String? = null
    @XmlElement(name = "sendtDato")
    var sendtDato: String? = null
    @XmlElement(name = "gebyrsats")
    var gebyrsats: String? = null
    @XmlElement(name = "innkrSamtid")
    var innkrSamtid: String? = null
    @XmlElement(name = "mottDato")
    var mottatDato: String? = null
    @XmlElement(name = "virknDato")
    var virkningsDato: String? = null
    @XmlElement(name = "myndighet")
    var myndighet: String? = null
    @XmlElement(name = "ffuRefNr")
    var ffuRefNr: String? = null
    @XmlElement(name = "konv")
    var konv: String? = null
    @XmlElement(name = "soknGrKode")
    var soknGrKode: String? = null
    @XmlElement(name = "soknFraKode")
    var soknFraKode: String? = null
    @XmlElement(name = "soknType")
    var soknType: String? = null
    @XmlElement(name = "b4Kode")
    var b4Kode: String? = null
    @XmlElement(name = "b4Belop")
    var b4Belop: String? = null
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