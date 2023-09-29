package no.nav.bidrag.dokument.bestilling.bestilling.dto

object BestillingSystem {
    const val BREVSERVER = "BREVSERVER"
    const val BUCKET = "BUCKET"
}

enum class DokumentType {
    UTGÅENDE,
    NOTAT
}

enum class InnholdType {
    REDIGERBAR, // Redigerbar brev hvor innhold kan endres av bruker
    IKKE_REDIGERBAR, // Ikke redigerbar brev hvor innhold er bestemt av data sendt inn eller samlet fra en eller flere kilder
    STATISK, // Dokument hvor innhold er statisk (innhold kan ikke endres). Dette er dokumenter som feks ligger på en bucket
}
typealias BestillingSystemType = String

data class DokumentDataGrunnlag(
    val vedtak: Boolean = false,
    val behandling: Boolean = false,
    val roller: Boolean = true,
    val enhetKontaktInfo: Boolean = true
)

enum class StøttetSpråk {
    NB,
    EN,
    NN,
    DE,
    PL,
    FR
}

private val støttetSpråkListeNynorsk = listOf(StøttetSpråk.NB, StøttetSpråk.NN)


interface DokumentMal {
    val kode: String
    val beskrivelse: String
    val tittel: String
    val dokumentType: DokumentType
    val bestillingSystem: BestillingSystemType
    val batchbrev: Boolean
    val enabled: Boolean
    val kreverDataGrunnlag: DokumentDataGrunnlag?
    val støttetSpråk: List<StøttetSpråk>
    val innholdtype: InnholdType
}

data class DokumentMalBucketUtland(
    override val folderName: String = "vedlegg_utland",
    override val kode: String,
    override val beskrivelse: String,
    override val tittel: String = beskrivelse,
    override val støttetSpråk: List<StøttetSpråk> = listOf(StøttetSpråk.NB),
) : DokumentMalBucket(
    kode = kode,
    beskrivelse = beskrivelse,
    støttetSpråk = støttetSpråk,
    folderName = folderName,
    tittel = tittel
)

data class DokumentMalBucketFarskap(
    override val folderName: String = "vedlegg_farskap",
    override val kode: String,
    override val beskrivelse: String,
    override val tittel: String = beskrivelse,
    override val støttetSpråk: List<StøttetSpråk> = listOf(StøttetSpråk.NB),
) : DokumentMalBucket(
    kode = kode,
    beskrivelse = beskrivelse,
    støttetSpråk = støttetSpråk,
    folderName = folderName,
    tittel = tittel
)


open class DokumentMalBucket(
    override val kode: String,
    override val beskrivelse: String,
    override val tittel: String,
    override val batchbrev: Boolean = false,
    override val enabled: Boolean = true,
    override val dokumentType: DokumentType = DokumentType.UTGÅENDE,
    override val innholdtype: InnholdType = InnholdType.STATISK,
    override val støttetSpråk: List<StøttetSpråk> = listOf(StøttetSpråk.NB),
    override val kreverDataGrunnlag: DokumentDataGrunnlag? = null,
    override val bestillingSystem: BestillingSystemType = BestillingSystem.BUCKET,
    open val folderName: String,
) : DokumentMal {
    val filePath get() = "$folderName/$kode.pdf"
}

data class DokumentMalBrevserver(
    override val kode: String,
    override val beskrivelse: String,
    override val tittel: String = beskrivelse,
    override val batchbrev: Boolean = false,
    override val enabled: Boolean = false,
    override var dokumentType: DokumentType = DokumentType.UTGÅENDE,
    override val innholdtype: InnholdType = InnholdType.REDIGERBAR,
    override val støttetSpråk: List<StøttetSpråk> = listOf(StøttetSpråk.NB),
    override val kreverDataGrunnlag: DokumentDataGrunnlag = DokumentDataGrunnlag(),
    override val bestillingSystem: BestillingSystemType = BestillingSystem.BREVSERVER,
) : DokumentMal


val dokumentmalerBucket = listOf(
    DokumentMalBucketUtland(
        kode = "VEDLEGG_VARSEL_BM_EN",
        tittel = "Response to Advance Notice Regarding Child Support Form for Custodial Parent",
        beskrivelse = "Svar på forhåndsvarsel i sak om barnebidrag til bidragsmottaker (EN)",
        støttetSpråk = listOf(StøttetSpråk.EN),
    ),
    DokumentMalBucketUtland(
        kode = "VEDLEGG_VARSEL_BP_EN",
        tittel = "Response to Advance Notice Regarding Child Support Form for Non-Custodial Parent",
        beskrivelse = "Svar på forhåndsvarsel i sak om barnebidrag til bidragspliktig (EN)",
        støttetSpråk = listOf(StøttetSpråk.EN),
    ),
    DokumentMalBucketUtland(
        kode = "VEDLEGG_VARSEL_BM_NB",
        beskrivelse = "Svar på forhåndsvarsel i sak om barnebidrag til bidragsmottaker",
        støttetSpråk = listOf(StøttetSpråk.NB),
    ),
    DokumentMalBucketUtland(
        kode = "VEDLEGG_VARSEL_BP_NB",
        beskrivelse = "Svar på forhåndsvarsel i sak om barnebidrag til bidragspliktig",
        støttetSpråk = listOf(StøttetSpråk.NB),
    ),
    DokumentMalBucketUtland(
        kode = "VEDLEGG_SVARSKJEMA_BP_FR",
        beskrivelse = "Svarskjema bidragspliktig (FR)",
        tittel = "Réponse à l’avis concernant pension alimentaire pour enfant(s) - formulaire de réponse pour le parent débiteur",
        støttetSpråk = listOf(StøttetSpråk.FR),
    ),
    DokumentMalBucketUtland(
        kode = "VEDLEGG_SVARSKJEMA_BP_PL",
        beskrivelse = "Svarskjema bidragspliktig (PL)",
        tittel = "Formularz dla rodzica zobowiązanego do płacenia alimentów",
        støttetSpråk = listOf(StøttetSpråk.PL),
    ),
    DokumentMalBucketUtland(
        kode = "VEDLEGG_SVARSKJEMA_BP_DE",
        beskrivelse = "Svarskjema bidragspliktig (DE)",
        tittel = "Antwortformular für Unterhaltspflichtige",
        støttetSpråk = listOf(StøttetSpråk.DE),
    ),
)
val dokumentmalerBrevserver = listOf(
    DokumentMalBrevserver(
        kode = "BI01P11",
        beskrivelse = "NOTAT P11 T",
        dokumentType = DokumentType.NOTAT,
        enabled = true,
        kreverDataGrunnlag = DokumentDataGrunnlag(roller = false),
    ),
    DokumentMalBrevserver(
        kode = "BI01P18",
        beskrivelse = "Saksbehandlingsnotat",
        dokumentType = DokumentType.NOTAT,
        enabled = true,
        kreverDataGrunnlag = DokumentDataGrunnlag(roller = false, enhetKontaktInfo = false)
    ),
    DokumentMalBrevserver(
        kode = "BI01X01",
        beskrivelse = "REFERAT FRA SAMTALE",
        dokumentType = DokumentType.NOTAT,
        enabled = true,
        kreverDataGrunnlag = DokumentDataGrunnlag(roller = false, enhetKontaktInfo = false)
    ),
    DokumentMalBrevserver(
        kode = "BI01X02",
        beskrivelse = "ELEKTRONISK DIALOG",
        dokumentType = DokumentType.NOTAT,
        enabled = true,
        kreverDataGrunnlag = DokumentDataGrunnlag(roller = false, enhetKontaktInfo = false)
    ),
    DokumentMalBrevserver(
        kode = "BI01S10",
        beskrivelse = "Oversendelse av informasjon",
        enabled = true
    ),
    DokumentMalBrevserver(
        kode = "BI01S67",
        beskrivelse = "ADRESSEFORESPØRSEL",
        enabled = true
    ),
    DokumentMalBrevserver(
        kode = "BI01S02",
        beskrivelse = "Fritekstbrev",
        enabled = true,
        støttetSpråk = listOf(
            StøttetSpråk.NB,
            StøttetSpråk.DE,
            StøttetSpråk.FR,
            StøttetSpråk.NN,
            StøttetSpråk.EN
        )
    ),
    DokumentMalBrevserver(
        kode = "BI01A50",
        beskrivelse = "Klage - Vedtak bidragsforskudd"
    ),
    DokumentMalBrevserver(
        kode = "BI01A01",
        beskrivelse = "Vedtak bidragsforskudd"
    ),
    DokumentMalBrevserver(
        kode = "BI01A04",
        beskrivelse = "Revurdering av bidragsforskudd"
    ),
    DokumentMalBrevserver(
        kode = "BI01S08",
        beskrivelse = "Varsel revurd forskudd"
    ),
    DokumentMalBrevserver(
        kode = "BI01S27",
        beskrivelse = "Varsel om ny beregning av bidragsforskudd og varsel om mulig tilbakebetaling"
    ),
    DokumentMalBrevserver(
        kode = "BI01S28",
        beskrivelse = "Varsel opphør av bidragsforskudd tilbake i tid"
    ),
    DokumentMalBrevserver(
        kode = "BI01S29",
        beskrivelse = "Varsel opphør av bidragsforskudd tilbake i tid"
    ),
    DokumentMalBrevserver(
        kode = "BI01S30",
        beskrivelse = "Varsel opphør av bidragsforskudd tilbake i tid"
    ),
    DokumentMalBrevserver(
        kode = "BI01G01",
        beskrivelse = "Vedtak innkrev. barnebidrag og gjeld"
    ),
    DokumentMalBrevserver(
        kode = "BI01G02",
        beskrivelse = "Vedtak innkreving opphør"
    ),
    DokumentMalBrevserver(
        kode = "BI01S19",
        beskrivelse = "Innkreving varsel til motparten"
    ),
    DokumentMalBrevserver(
        kode = "BI01B50",
        beskrivelse = "Klage - Vedtak barnebidrag"
    ),
    DokumentMalBrevserver(
        kode = "BI01G50",
        beskrivelse = "Klage - vedtak innkreving"
    ),
    DokumentMalBrevserver(
        kode = "BI01P17",
        beskrivelse = "Uttalelse til klageinstans",
        dokumentType = DokumentType.NOTAT
    ),
    DokumentMalBrevserver(
        kode = "BI01S20",
        beskrivelse = "NAV har mottatt klage på vedtak"
    ),
    DokumentMalBrevserver(
        kode = "BI01S21",
        beskrivelse = "Du har klaget på vedtaket"
    ),
    DokumentMalBrevserver(
        kode = "BI01S60",
        beskrivelse = "Infobrev til partene oversendelse klageinstans"
    ),
    DokumentMalBrevserver(
        kode = "BI01S61",
        beskrivelse = "Innhenting opplysninger paragraf 10 i barnelova"
    ),
    DokumentMalBrevserver(
        kode = "BI01S64",
        beskrivelse = "Varsel klage fvt 35"
    ),
    DokumentMalBrevserver(
        kode = "BI01S65",
        beskrivelse = "Varsel om motregning"
    ),
    DokumentMalBrevserver(
        kode = "BI01B10",
        beskrivelse = "Opphørsvedtak"
    ),
    DokumentMalBrevserver(
        kode = "BI01S07",
        beskrivelse = "Varsel om begrenset revurdering av barnebidrag"
    ),
    DokumentMalBrevserver(
        kode = "BI01B01",
        beskrivelse = "Vedtak barnebidrag"
    ),
    DokumentMalBrevserver(
        kode = "BI01B04",
        beskrivelse = "Revurdering Bidrags pga forskudd"
    ),
    DokumentMalBrevserver(
        kode = "BI01B20",
        beskrivelse = "Vedtak utland skjønn fastsettelse"
    ),
    DokumentMalBrevserver(
        kode = "BI01B21",
        beskrivelse = "Vedtak utland skjønn endring"
    ),
    DokumentMalBrevserver(
        kode = "BI01S06",
        beskrivelse = "Varsel om fastsettelse av barnebidrag fra barnets fødsel"
    ),
    DokumentMalBrevserver(
        kode = "BI01S31",
        beskrivelse = "Fasts. eget tiltak uten innkr varsel til partene"
    ),
    DokumentMalBrevserver(
        kode = "BI01S32",
        beskrivelse = "Fastsettelse eget tiltak varsel til BM"
    ),
    DokumentMalBrevserver(
        kode = "BI01S33",
        beskrivelse = "Endring varsel eget tiltak pga 25%"
    ),
    DokumentMalBrevserver(
        kode = "BI01S34",
        beskrivelse = "Endring varsel eget tiltak pga barnetillegg"
    ),
    DokumentMalBrevserver(
        kode = "BI01S35",
        beskrivelse = "Varsel om endring av barnebidraget på grunn av forholdsmessig fordeling"
    ),
    DokumentMalBrevserver(
        kode = "BI01S36",
        beskrivelse = "Endring varsel eget tiltak pga forsørgingstillegg"
    ),
    DokumentMalBrevserver(
        kode = "BI01S46",
        beskrivelse = "Varsel om endring av oppfostringsbidraget på grunn av forholdsmessig fordeling"
    ),
    DokumentMalBrevserver(
        kode = "BI01S62",
        beskrivelse = "Varsel om fastsettelse av barnebidrag til den bidragspliktiges barnetillegg"
    ),
    DokumentMalBrevserver(
        kode = "BI01S63",
        beskrivelse = "Fastsettelse bidrag forsørgingstillegg varsel"
    ),
    DokumentMalBrevserver(
        kode = "BI01S22",
        beskrivelse = "Revurdering bidrag pga bidragsforskudd til bidragsmottaker"
    ),
    DokumentMalBrevserver(
        kode = "BI01S23",
        beskrivelse = "Revurdering bidrag pga bidragsforskudd til bidragspliktig"
    ),
    DokumentMalBrevserver(
        kode = "BI01S01",
        beskrivelse = "NAV har mottatt søknad om barnebidrag"
    ),
    DokumentMalBrevserver(
        kode = "BI01S12",
        beskrivelse = "Du har søkt om barnebidrag"
    ),
    DokumentMalBrevserver(
        kode = "BI01S52",
        beskrivelse = "Forhåndsvarsel om mottatt søknad om oppfostringsbidrag"
    ),
    DokumentMalBrevserver(
        kode = "BI01S53",
        beskrivelse = "Forhåndsvarsel om mottatt søknad om oppfostringsbidrag"
    ),
    DokumentMalBrevserver(
        kode = "BI01S13",
        beskrivelse = "Du har søkt om barnebidrag"
    ),
    DokumentMalBrevserver(
        kode = "BI01B02",
        beskrivelse = "Vedtak tilleggsbidrag"
    ),
    DokumentMalBrevserver(
        kode = "BI01S05",
        beskrivelse = "NAV har mottatt søknad om sletting av bidragsgjeld"
    ),
    DokumentMalBrevserver(
        kode = "BI01S14",
        beskrivelse = "Du har søkt om endring av barnebidraget"
    ),
    DokumentMalBrevserver(
        kode = "BI01S26",
        beskrivelse = "Endring varsel til motparten"
    ),
    DokumentMalBrevserver(
        kode = "BI01S47",
        beskrivelse = "Forhåndsvarsel om mottatt søknad om endring av oppfostringsbidrag"
    ),
    DokumentMalBrevserver(
        kode = "BI01S48",
        beskrivelse = "Forhåndsvarsel om mottatt søknad om endring av oppfostringsbidrag"
    ),
    DokumentMalBrevserver(
        kode = "BI01S49",
        beskrivelse = "Forhåndsvarsel om mottatt søknad om endring av oppfostringsbidrag"
    ),
    DokumentMalBrevserver(
        kode = "BI01I50",
        beskrivelse = "Klage - vedtak ektefellebidrag"
    ),
    DokumentMalBrevserver(
        kode = "BI01I01",
        beskrivelse = "Vedtak ektefellebidrag"
    ),
    DokumentMalBrevserver(
        kode = "BI01S43",
        beskrivelse = "Fastsettelse ektefellebidrag orientering"
    ),
    DokumentMalBrevserver(
        kode = "BI01S44",
        beskrivelse = "Fastsettelse ektefellebidrag varsel til BP"
    ),
    DokumentMalBrevserver(
        kode = "BI01S37",
        beskrivelse = "Bortfall ektefellebidrag BP død orientering til BM"
    ),
    DokumentMalBrevserver(
        kode = "BI01S38",
        beskrivelse = "Bortfall ektefellebidrag orientering til partene"
    ),
    DokumentMalBrevserver(
        kode = "BI01S39",
        beskrivelse = "Bortfall ektefellebidrag nytt ekteskap orientering"
    ),
    DokumentMalBrevserver(
        kode = "BI01S41",
        beskrivelse = "Vedtak - bortfall nytt ekteskap"
    ),
    DokumentMalBrevserver(
        kode = "BI01S42",
        beskrivelse = "Endring ektefellebidrag orientering til søkeren"
    ),
    DokumentMalBrevserver(
        kode = "BI01S45",
        beskrivelse = "Varsel om ny beregning av bidragsforskudd og varsel om mulig tilbakebetaling"
    ),
    DokumentMalBrevserver(
        kode = "BI01H01",
        beskrivelse = "Farskap innkalling mor"
    ),
    DokumentMalBrevserver(
        kode = "BI01H03",
        beskrivelse = "Melding om blodprøver i farskapsak"
    ),
    DokumentMalBrevserver(
        kode = "BI01H04",
        beskrivelse = "Pålegg om å framstille barn for å gi blodprøve"
    ),
    DokumentMalBrevserver(
        kode = "BI01H05",
        beskrivelse = "Pålegg om blodprøve i farskapssak"
    ),
    DokumentMalBrevserver(
        kode = "BI01H02",
        beskrivelse = "Innkalling farskapssak oppgitt far"
    ),
    DokumentMalBrevserver(
        kode = "BI01J50",
        beskrivelse = "Klage - vedtak gebyr"
    ),
    DokumentMalBrevserver(
        kode = "BI01S03",
        beskrivelse = "NAV har mottatt søknad om barnebidrag etter fylte 18 år - forhåndsvarsel"
    ),
    DokumentMalBrevserver(
        kode = "BI01K50",
        beskrivelse = "Klage - vedtak tilbakekreving"
    ),
    DokumentMalBrevserver(
        kode = "BI01A05",
        beskrivelse = "Vedtak ikke tilbakekreving"
    ),
    DokumentMalBrevserver(
        kode = "BI01G04",
        beskrivelse = "Vedtak innkreving særtilskudd"
    ),
    DokumentMalBrevserver(
        kode = "BI01E50",
        beskrivelse = "Klage - vedtak bidrag til særlige utgifter"
    ),
    DokumentMalBrevserver(
        kode = "BI01E01",
        beskrivelse = "Vedtak bidrag til særlige utgifter"
    ),
    DokumentMalBrevserver(
        kode = "BI01E02",
        beskrivelse = "Vedtak særtilskudd avslag"
    ),
    DokumentMalBrevserver(
        kode = "BI01E03",
        beskrivelse = "Vedtak bidrag til særlige utgifter tannregulering"
    ),
    DokumentMalBrevserver(
        kode = "BI01S04",
        beskrivelse = "NAV har mottatt søknad om bidrag til særlige utgifter"
    ),
    DokumentMalBrevserver(
        kode = "BI01S18",
        beskrivelse = "Du har søkt om bidrag til særlige utgifter"
    ),
    DokumentMalBrevserver(
        kode = "BI01F50",
        beskrivelse = "Klage - vedtak ettergivelse gjeld"
    ),
    DokumentMalBrevserver(
        kode = "BI01F01",
        beskrivelse = "Vedtak ettergivelse innvilget"
    ),
    DokumentMalBrevserver(
        kode = "BI01F02",
        beskrivelse = "Vedtak ettergivelse avslag"
    ),
    DokumentMalBrevserver(
        kode = "BI01S17",
        beskrivelse = "Informasjon om din søknad om sletting av bidragsgjeld"
    ),
    DokumentMalBrevserver(
        kode = "BI01S50",
        beskrivelse = "Ettergivelse oppfostringsbidrag orientering til BP"
    ),
    DokumentMalBrevserver(
        kode = "BI01S51",
        beskrivelse = "Ettergivelse oppfostringsbidrag varsel kommune"
    ),
    DokumentMalBrevserver(
        kode = "BI01A02",
        beskrivelse = "Vedtak tilbakekreving"
    ),
    DokumentMalBrevserver(
        kode = "BI01S54",
        beskrivelse = "Varsel om tilbakekreving av bidragsforskudd - inntekt"
    ),
    DokumentMalBrevserver(
        kode = "BI01S55",
        beskrivelse = "Varsel tilbakekreving av bidragsforskudd - partene bor sammen"
    ),
    DokumentMalBrevserver(
        kode = "BI01S56",
        beskrivelse = "Varsel tilbakekreving av bidragsforskudd - ikke opphold i Riket"
    ),
    DokumentMalBrevserver(
        kode = "BI01S57",
        beskrivelse = "Varsel tilbakekreving av bidragsforskudd - direkte betalinger"
    ),
    DokumentMalBrevserver(
        kode = "BI01S58",
        beskrivelse = "Varsel tilbakekreving av bidragsforskudd - ikke omsorg"
    ),
    DokumentMalBrevserver(
        kode = "BI01S59",
        beskrivelse = "Informasjon om din søknad om sletting av bidragsgjeld"
    ),
    DokumentMalBrevserver(
        kode = "BI01S24",
        beskrivelse = "NAV har mottatt søknad om barnebidrag etter fylte 18 år - innhenting av opplysninger"
    ),
    DokumentMalBrevserver(
        kode = "BI01B03",
        beskrivelse = "Vedtak barnebidrag etter fylte 18 år"
    ),
    DokumentMalBrevserver(
        kode = "BI01S15",
        beskrivelse = "Du har søkt om barnebidrag etter fylte 18 år"
    ),
    DokumentMalBrevserver(
        kode = "BI01S16",
        beskrivelse = "Du har søkt om barnebidrag etter fylte 18 år"
    ),
    DokumentMalBrevserver(
        kode = "BI01B11",
        beskrivelse = "Vedtak tilleggsbidrag over 18 år"
    ),
    DokumentMalBrevserver(
        kode = "BI01B05",
        beskrivelse = "VEDTAK AUTOMATISK JUSTERING BARNEBIDRAG",
        batchbrev = true
    ),
    DokumentMalBrevserver(
        kode = "BI01S09",
        beskrivelse = "Varsel om opphør barnebidrag etter fylte 18 år",
        batchbrev = true
    )
)

enum class DokumentMalEnum(
    val beskrivelse: String,
    var brevtype: DokumentType,
    val bestillingSystem: BestillingSystemType,
    val batchbrev: Boolean = false,
    val enabled: Boolean = true,
    val kreverDataGrunnlag: DokumentDataGrunnlag? = DokumentDataGrunnlag(),
    val støttetSpråk: List<StøttetSpråk> = listOf(StøttetSpråk.NB),
    val innholdtype: InnholdType = InnholdType.REDIGERBAR
) {
    // Standardbrev
    BI01P11(
        "NOTAT P11 T",
        DokumentType.NOTAT,
        BestillingSystem.BREVSERVER,
        kreverDataGrunnlag = DokumentDataGrunnlag(roller = false)
    ),
    BI01P18(
        "Saksbehandlingsnotat",
        DokumentType.NOTAT,
        BestillingSystem.BREVSERVER,
        kreverDataGrunnlag = DokumentDataGrunnlag(roller = false, enhetKontaktInfo = false)
    ),
    BI01X01(
        "REFERAT FRA SAMTALE",
        DokumentType.NOTAT,
        BestillingSystem.BREVSERVER,
        kreverDataGrunnlag = DokumentDataGrunnlag(roller = false, enhetKontaktInfo = false)
    ),
    BI01X02(
        "ELEKTRONISK DIALOG",
        DokumentType.NOTAT,
        BestillingSystem.BREVSERVER,
        kreverDataGrunnlag = DokumentDataGrunnlag(roller = false, enhetKontaktInfo = false)
    ),
    BI01S10("Oversendelse av informasjon", DokumentType.UTGÅENDE, BestillingSystem.BREVSERVER),
    BI01S67("ADRESSEFORESPØRSEL", DokumentType.UTGÅENDE, BestillingSystem.BREVSERVER),
    BI01S02(
        "Fritekstbrev",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        støttetSpråk = listOf(
            StøttetSpråk.NB,
            StøttetSpråk.DE,
            StøttetSpråk.FR,
            StøttetSpråk.NN,
            StøttetSpråk.EN
        )
    ),

    // Brev relatert til forskudd
    BI01A50(
        "Klage - Vedtak bidragsforskudd",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        enabled = false,
        DokumentDataGrunnlag(vedtak = true),
        støttetSpråk = støttetSpråkListeNynorsk
    ),
    BI01A01(
        "Vedtak bidragsforskudd",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        enabled = false,
        DokumentDataGrunnlag(vedtak = true),
        støttetSpråk = støttetSpråkListeNynorsk
    ),
    BI01A04(
        "Revurdering av bidragsforskudd",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        enabled = false,
        DokumentDataGrunnlag(vedtak = true),
        støttetSpråk = støttetSpråkListeNynorsk
    ),
    BI01S08(
        "Varsel revurd forskudd",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        enabled = false
    ),
    BI01S27(
        "Varsel om ny beregning av bidragsforskudd og varsel om mulig tilbakebetaling",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        enabled = false
    ), // MAIN
    BI01S28(
        "Varsel opphør av bidragsforskudd tilbake i tid",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        enabled = false
    ), // TODO: Slettes og erstattes av S27
    BI01S29(
        "Varsel opphør av bidragsforskudd tilbake i tid",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        enabled = false
    ), // TODO: Slettes og erstattes av S27
    BI01S30(
        "Varsel opphør av bidragsforskudd tilbake i tid",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        enabled = false
    ), // TODO: Slettes og erstattes av S27

    // Brev ikke støttet av bestilling
    BI01G01(
        "Vedtak innkrev. barnebidrag og gjeld",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01G02(
        "Vedtak innkreving opphør",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S19(
        "Innkreving varsel til motparten",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01B50(
        "Klage - Vedtak barnebidrag",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01G50(
        "Klage - vedtak innkreving",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01P17(
        "Uttalelse til klageinstans",
        DokumentType.NOTAT,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S20(
        "NAV har mottatt klage på vedtak",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S21(
        "Du har klaget på vedtaket",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S60(
        "Infobrev til partene oversendelse klageinstans",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S61(
        "Innhenting opplysninger paragraf 10 i barnelova",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S64(
        "Varsel klage fvt 35",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S65(
        "Varsel om motregning",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),

    //    BI01S68(
//        "Varsel om overføring KO-fogd",
//        BrevType.UTGÅENDE,
//        BestillingSystem.BREVSERVER,
//        false,
//        false
//    ),
    BI01B10("Opphørsvedtak", DokumentType.UTGÅENDE, BestillingSystem.BREVSERVER, false, false),
    BI01S07(
        "Varsel om begrenset revurdering av barnebidrag",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01B01(
        "Vedtak barnebidrag",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false,
        støttetSpråk = listOf(StøttetSpråk.NB, StøttetSpråk.EN, StøttetSpråk.FR)
    ),
    BI01B04(
        "Revurdering Bidrags pga forskudd",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false,
        støttetSpråk = støttetSpråkListeNynorsk
    ),
    BI01B20(
        "Vedtak utland skjønn fastsettelse",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01B21(
        "Vedtak utland skjønn endring",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S06(
        "Varsel om fastsettelse av barnebidrag fra barnets fødsel",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S31(
        "Fasts. eget tiltak uten innkr varsel til partene",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S32(
        "Fastsettelse eget tiltak varsel til BM",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S33(
        "Endring varsel eget tiltak pga 25%",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S34(
        "Endring varsel eget tiltak pga barnetillegg",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S35(
        "Varsel om endring av barnebidraget på grunn av forholdsmessig fordeling",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S36(
        "Endring varsel eget tiltak pga forsørgingstillegg",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S46(
        "Varsel om endring av oppfostringsbidraget på grunn av forholdsmessig fordeling",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S62(
        "Varsel om fastsettelse av barnebidrag til den bidragspliktiges barnetillegg",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S63(
        "Fastsettelse bidrag forsørgingstillegg varsel",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),

    //    BI01S70(
//        "Varsel om eget tiltak trekkes",
//        BrevType.UTGÅENDE,
//        BestillingSystem.BREVSERVER,
//        false,
//        false
//    ),
    BI01S22(
        "Revurdering bidrag pga bidragsforskudd til bidragsmottaker",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S23(
        "Revurdering bidrag pga bidragsforskudd til bidragspliktig",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S01(
        "NAV har mottatt søknad om barnebidrag",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S12(
        "Du har søkt om barnebidrag",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S52(
        "Forhåndsvarsel om mottatt søknad om oppfostringsbidrag",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S53(
        "Forhåndsvarsel om mottatt søknad om oppfostringsbidrag",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ), // Erstattes av BI01S52
    BI01S13(
        "Du har søkt om barnebidrag",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01B02(
        "Vedtak tilleggsbidrag",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false,
        støttetSpråk = listOf(StøttetSpråk.NB, StøttetSpråk.EN, StøttetSpråk.DE)
    ),
    BI01S05(
        "NAV har mottatt søknad om sletting av bidragsgjeld",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S14(
        "Du har søkt om endring av barnebidraget",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S26(
        "Endring varsel til motparten",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S47(
        "Forhåndsvarsel om mottatt søknad om endring av oppfostringsbidrag",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S48(
        "Forhåndsvarsel om mottatt søknad om endring av oppfostringsbidrag",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S49(
        "Forhåndsvarsel om mottatt søknad om endring av oppfostringsbidrag",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01I50(
        "Klage - vedtak ektefellebidrag",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01I01(
        "Vedtak ektefellebidrag",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S43(
        "Fastsettelse ektefellebidrag orientering",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S44(
        "Fastsettelse ektefellebidrag varsel til BP",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S37(
        "Bortfall ektefellebidrag BP død orientering til BM",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S38(
        "Bortfall ektefellebidrag orientering til partene",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S39(
        "Bortfall ektefellebidrag nytt ekteskap orientering",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S41(
        "Vedtak - bortfall nytt ekteskap",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S42(
        "Endring ektefellebidrag orientering til søkeren",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S45(
        "Varsel om ny beregning av bidragsforskudd og varsel om mulig tilbakebetaling",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ), // TODO: Slettes og erstattes av S27
    BI01H01(
        "Farskap innkalling mor",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01H03(
        "Melding om blodprøver i farskapsak",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01H04(
        "Pålegg om å framstille barn for å gi blodprøve",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01H05(
        "Pålegg om blodprøve i farskapssak",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01H02(
        "Innkalling farskapssak oppgitt far",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),

    //    BI01S25(
//        "Info til BM når farskap OK",
//        BrevType.UTGÅENDE,
//        BestillingSystem.BREVSERVER,
//        false,
//        false
//    ),
    BI01J50(
        "Klage - vedtak gebyr",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S03(
        "NAV har mottatt søknad om barnebidrag etter fylte 18 år - forhåndsvarsel",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01K50(
        "Klage - vedtak tilbakekreving",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01A05(
        "Vedtak ikke tilbakekreving",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false,
        støttetSpråk = støttetSpråkListeNynorsk
    ),
    BI01G04(
        "Vedtak innkreving særtilskudd",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01E50(
        "Klage - vedtak bidrag til særlige utgifter",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01E01(
        "Vedtak bidrag til særlige utgifter",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01E02(
        "Vedtak særtilskudd avslag",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01E03(
        "Vedtak bidrag til særlige utgifter tannregulering",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S04(
        "NAV har mottatt søknad om bidrag til særlige utgifter",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S18(
        "Du har søkt om bidrag til særlige utgifter",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01F50(
        "Klage - vedtak ettergivelse gjeld",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01F01(
        "Vedtak ettergivelse innvilget",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01F02(
        "Vedtak ettergivelse avslag",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S17(
        "Informasjon om din søknad om sletting av bidragsgjeld",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S50(
        "Ettergivelse oppfostringsbidrag orientering til BP",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S51(
        "Ettergivelse oppfostringsbidrag varsel kommune",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01A02(
        "Vedtak tilbakekreving",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false,
        støttetSpråk = støttetSpråkListeNynorsk
    ),
    BI01S54(
        "Varsel om tilbakekreving av bidragsforskudd - inntekt",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S55(
        "Varsel tilbakekreving av bidragsforskudd - partene bor sammen",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S56(
        "Varsel tilbakekreving av bidragsforskudd - ikke opphold i Riket",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S57(
        "Varsel tilbakekreving av bidragsforskudd - direkte betalinger",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S58(
        "Varsel tilbakekreving av bidragsforskudd - ikke omsorg",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S59(
        "Informasjon om din søknad om sletting av bidragsgjeld",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S24(
        "NAV har mottatt søknad om barnebidrag etter fylte 18 år - innhenting av opplysninger",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01B03(
        "Vedtak barnebidrag etter fylte 18 år",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S15(
        "Du har søkt om barnebidrag etter fylte 18 år",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S16(
        "Du har søkt om barnebidrag etter fylte 18 år",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01B11(
        "Vedtak tilleggsbidrag over 18 år",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),

    // Batchbrev
    BI01B05(
        "VEDTAK AUTOMATISK JUSTERING BARNEBIDRAG",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        true,
        false
    ),
    BI01S09(
        "Varsel om opphør barnebidrag etter fylte 18 år",
        DokumentType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        true,
        false
    ),


    // -- Vedlegg utland -- //


}


val alleDokumentmaler = dokumentmalerBrevserver + dokumentmalerBucket

fun hentDokumentMal(kode: String): DokumentMal? = alleDokumentmaler.find { it.kode == kode }