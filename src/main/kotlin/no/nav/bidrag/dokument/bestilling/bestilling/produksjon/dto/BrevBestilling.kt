package no.nav.bidrag.dokument.bestilling.bestilling.produksjon.dto

import no.nav.bidrag.dokument.bestilling.model.LANDKODE3_NORGE
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

    fun brev(init: Brev.() -> Unit): Brev {
        val initBrev = Brev()
        initBrev.init()
        brev = initBrev
        return initBrev
    }
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

    @XmlElement(name = "barniSak")
    var barnISak: MutableList<BarnISak> = mutableListOf()

    @XmlElement(name = "soknBost")
    var soknad: Soknad? = null

    @XmlElement(name = "Kontaktinfo")
    var kontaktInfo: BrevKontaktinfo? = null

    @XmlElement(name = "Saksbehandl")
    var saksbehandler: BrevSaksbehandler? = null

    fun brevKontaktinfo(init: BrevKontaktinfo.() -> Unit): BrevKontaktinfo {
        val brevKontaktinfo = BrevKontaktinfo()
        brevKontaktinfo.init()
        kontaktInfo = brevKontaktinfo
        return brevKontaktinfo
    }
    fun brevSaksbehandler(init: BrevSaksbehandler.() -> Unit): BrevSaksbehandler {
        val brevSaksbehandler = BrevSaksbehandler()
        brevSaksbehandler.init()
        saksbehandler = brevSaksbehandler
        return brevSaksbehandler
    }
    fun soknad(init: Soknad.() -> Unit): Soknad {
        val initSoknad = Soknad()
        initSoknad.init()
        soknad = initSoknad
        return initSoknad
    }

    fun barnISak(init: BarnISak.() -> Unit): BarnISak {
        val initBarnISak = BarnISak()
        initBarnISak.init()
        barnISak.add(initBarnISak)
        return initBarnISak
    }

    fun brevmottaker(init: BrevMottaker.() -> Unit): BrevMottaker {
        val brevMottaker = BrevMottaker()
        brevMottaker.init()
        mottaker = brevMottaker
        return brevMottaker
    }

    fun parter(init: Parter.() -> Unit): Parter {
        val initParter = Parter()
        initParter.init()
        parter = initParter
        return initParter
    }
}

@Suppress("unused")
@XmlRootElement(name = "Saksbehandl")
@XmlAccessorType(XmlAccessType.FIELD)
class BrevSaksbehandler {

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
    @XmlJavaTypeAdapter(BirthDateAdapter::class)
    var fodselsdato: LocalDate? = null

    @XmlElement(name = "postnr", nillable = true)
    var postnummer: String? = null

    @XmlElement(name = "landKd", nillable = true)
    @XmlJavaTypeAdapter(LandkodeAdapter::class)
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

    fun returOgPostadresse(init: Adresse.() -> Unit): Adresse {
        val adresse = Adresse()
        adresse.init()
        returAdresse = adresse
        postadresse = adresse
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
    // Bidragspliktig (BP)
    @XmlElement(name = "bpfnr", nillable = true)
    var bpfnr: String? = null

    @XmlElement(name = "bpNavn", nillable = true)
    var bpnavn: String? = null

    @XmlElement(name = "bpfDato", nillable = true)
    @XmlJavaTypeAdapter(BirthDateAdapter::class)
    var bpfodselsdato: LocalDate? = null

    @XmlElement(name = "bpKravFremAv", nillable = true)
    var bpkravfremav: String? = null

    @XmlElement(name = "bpbelopGebyr", nillable = true)
    var bpgebyr: String? = null

    @XmlElement(name = "bpLandKd", nillable = true)
    @XmlJavaTypeAdapter(LandkodeAdapter::class)
    var bplandkode: String? = null

    @XmlElement(name = "bpDatoDod", nillable = true)
    @XmlJavaTypeAdapter(BirthDateAdapter::class)
    var bpdatodod: LocalDate? = null

    // Bidragsmottaker (BM)
    @XmlElement(name = "bmfnr", nillable = true)
    var bmfnr: String? = null

    @XmlElement(name = "bmNavn", nillable = true)
    var bmnavn: String? = null

    @XmlElement(name = "bmfDato", nillable = true)
    @XmlJavaTypeAdapter(BirthDateAdapter::class)
    var bmfodselsdato: LocalDate? = null

    @XmlElement(name = "bmKravFremAv", nillable = true)
    var bmkravkfremav: String? = null

    @XmlElement(name = "bmbelopGebyr", nillable = true)
    var bmgebyr: String? = null

    @XmlElement(name = "bmLandKd", nillable = true)
    @XmlJavaTypeAdapter(LandkodeAdapter::class)
    var bmlandkode: String? = null

    @XmlElement(name = "bmDatoDod", nillable = true)
    @XmlJavaTypeAdapter(BirthDateAdapter::class)
    var bmdatodod: LocalDate? = null
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
    var indexRegDato: String? = null // TODO: Date format?
    @XmlElement(name = "indexRegPro", nillable = true)
    var indexRegPro: String? = null

    @XmlElement(name = "hgKode", nillable = true)
    var hgKode: String? = null

    @XmlElement(name = "ugKode", nillable = true)
    var ugKode: String? = null

    @XmlElement(name = "datoSakReg", nillable = true)
    @XmlJavaTypeAdapter(DateAdapter::class)
    var datoSakReg: LocalDate? = null

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
    @XmlJavaTypeAdapter(DateAdapter::class)
    var sendtDato: LocalDate? = LocalDate.now()

    @XmlElement(name = "gebyrsats", nillable = true)
    var gebyrsats: String? = null

    @XmlElement(name = "innkrSamtid")
    @XmlJavaTypeAdapter(BooleanAdapter::class)
    var innkrSamtid: Boolean? = false

    @XmlElement(name = "mottDato", nillable = true)
    @XmlJavaTypeAdapter(BirthDateAdapter::class)
    var mottatDato: LocalDate? = null

    @XmlElement(name = "virknDato", nillable = true)
    var virkningsDato: String? = null // TODO: Date format?
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
    var b4Kode: String? = null

    @XmlElement(name = "b4Belop", nillable = true)
    var b4Belop: String? = null
}

@Suppress("unused")
@XmlRootElement(name = "barniSak")
@XmlAccessorType(XmlAccessType.FIELD)
class BarnISak {
    @XmlElement(name = "fnr", nillable = true)
    var fnr: String? = null

    @XmlElement(name = "fornavn", nillable = true)
    var fornavn: String? = null

    @XmlElement(name = "fDato", nillable = true)
    @XmlJavaTypeAdapter(BirthDateAdapter::class)
    var fDato: LocalDate? = null

    @XmlElement(name = "navn", nillable = true)
    var navn: String? = null

    @XmlElement(name = "personIdRm", nillable = true)
    var personIdRm: String? = null

    @XmlElement(name = "belopGebyrRm", nillable = true)
    var belopGebyrRm: String? = null

    @XmlElement(name = "belForskudd", nillable = true)
    var belForskudd: String? = null

    @XmlElement(name = "belBidrag", nillable = true)
    var belBidrag: String? = null
}

@DslMarker
annotation class BrevBestillingMarker

@BrevBestillingMarker
fun brevbestilling(init: BrevBestilling.() -> Unit): BrevBestilling {
    val brevBestilling = BrevBestilling()
    brevBestilling.init()
    return brevBestilling
}

class BirthDateAdapter : XmlAdapter<String, LocalDate?>() {
    override fun marshal(date: LocalDate?): String? {
        return date?.format(BREV_DATETIME_FORMAT)
    }

    @Throws(ParseException::class)
    override fun unmarshal(v: String): LocalDate {
        return LocalDate.parse(v, BREV_DATETIME_FORMAT)
    }
}

class DateAdapter : XmlAdapter<String, LocalDate?>() {
    override fun marshal(date: LocalDate?): String? {
        return date?.format(BREV_SOKNAD_DATETIME_FORMAT)
    }

    @Throws(ParseException::class)
    override fun unmarshal(v: String): LocalDate {
        return LocalDate.parse(v, BREV_SOKNAD_DATETIME_FORMAT)
    }
}

class LandkodeAdapter : XmlAdapter<String, String?>() {
    override fun marshal(landkode: String?): String? {
        return if (landkode.isNullOrEmpty() || landkode == LANDKODE3_NORGE) null else landkode
    }

    @Throws(ParseException::class)
    override fun unmarshal(landkode: String): String {
        return landkode
    }
}

class BooleanAdapter : XmlAdapter<String, Boolean?>() {
    override fun marshal(v: Boolean?): String {
        return if (v == true) "J" else "N"
    }

    @Throws(ParseException::class)
    override fun unmarshal(v: String): Boolean {
        return v == "J"
    }
}
