package no.nav.bidrag.dokument.bestilling.bestilling.dto

object BestillingSystem {
    const val BREVSERVER = "BREVSERVER"
}

enum class BrevType {
    UTGÅENDE,
    NOTAT
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
    FR
}

private val støttetSpråkListeNynorsk = listOf(StøttetSpråk.NB, StøttetSpråk.NN)

enum class DokumentMal(
    val beskrivelse: String,
    var brevtype: BrevType,
    val bestillingSystem: BestillingSystemType,
    val batchbrev: Boolean = false,
    val enabled: Boolean = true,
    val kreverDataGrunnlag: DokumentDataGrunnlag = DokumentDataGrunnlag(),
    val støttetSpråk: List<StøttetSpråk> = listOf(StøttetSpråk.NB)
) {
    // Standardbrev
    BI01P11(
        "NOTAT P11 T",
        BrevType.NOTAT,
        BestillingSystem.BREVSERVER,
        kreverDataGrunnlag = DokumentDataGrunnlag(roller = false)
    ),
    BI01P18(
        "Saksbehandlingsnotat",
        BrevType.NOTAT,
        BestillingSystem.BREVSERVER,
        kreverDataGrunnlag = DokumentDataGrunnlag(roller = false, enhetKontaktInfo = false)
    ),
    BI01X01(
        "REFERAT FRA SAMTALE",
        BrevType.NOTAT,
        BestillingSystem.BREVSERVER,
        kreverDataGrunnlag = DokumentDataGrunnlag(roller = false, enhetKontaktInfo = false)
    ),
    BI01X02(
        "ELEKTRONISK DIALOG",
        BrevType.NOTAT,
        BestillingSystem.BREVSERVER,
        kreverDataGrunnlag = DokumentDataGrunnlag(roller = false, enhetKontaktInfo = false)
    ),
    BI01S10("Oversendelse av informasjon", BrevType.UTGÅENDE, BestillingSystem.BREVSERVER),
    BI01S67("ADRESSEFORESPØRSEL", BrevType.UTGÅENDE, BestillingSystem.BREVSERVER),
    BI01S02(
        "Fritekstbrev",
        BrevType.UTGÅENDE,
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
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        enabled = false,
        DokumentDataGrunnlag(vedtak = true),
        støttetSpråk = støttetSpråkListeNynorsk
    ),
    BI01A01(
        "Vedtak bidragsforskudd",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        enabled = false,
        DokumentDataGrunnlag(vedtak = true),
        støttetSpråk = støttetSpråkListeNynorsk
    ),
    BI01A04(
        "Revurdering av bidragsforskudd",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        enabled = false,
        DokumentDataGrunnlag(vedtak = true),
        støttetSpråk = støttetSpråkListeNynorsk
    ),
    BI01S08(
        "Varsel revurd forskudd",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        enabled = false
    ),
    BI01S27(
        "Varsel om ny beregning av bidragsforskudd og varsel om mulig tilbakebetaling",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        enabled = false
    ), // MAIN
    BI01S28(
        "Varsel opphør av bidragsforskudd tilbake i tid",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        enabled = false
    ), // TODO: Slettes og erstattes av S27
    BI01S29(
        "Varsel opphør av bidragsforskudd tilbake i tid",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        enabled = false
    ), // TODO: Slettes og erstattes av S27
    BI01S30(
        "Varsel opphør av bidragsforskudd tilbake i tid",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        enabled = false
    ), // TODO: Slettes og erstattes av S27

    // Brev ikke støttet av bestilling
    BI01G01(
        "Vedtak innkrev. barnebidrag og gjeld",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01G02(
        "Vedtak innkreving opphør",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S19(
        "Innkreving varsel til motparten",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01B50(
        "Klage - Vedtak barnebidrag",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01G50(
        "Klage - vedtak innkreving",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01P17(
        "Uttalelse til klageinstans",
        BrevType.NOTAT,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S20(
        "NAV har mottatt klage på vedtak",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S21(
        "Du har klaget på vedtaket",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S60(
        "Infobrev til partene oversendelse klageinstans",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S61(
        "Innhenting opplysninger paragraf 10 i barnelova",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S64("Varsel klage fvt 35", BrevType.UTGÅENDE, BestillingSystem.BREVSERVER, false, false),
    BI01S65("Varsel om motregning", BrevType.UTGÅENDE, BestillingSystem.BREVSERVER, false, false),

    //    BI01S68(
//        "Varsel om overføring KO-fogd",
//        BrevType.UTGÅENDE,
//        BestillingSystem.BREVSERVER,
//        false,
//        false
//    ),
    BI01B10("Opphørsvedtak", BrevType.UTGÅENDE, BestillingSystem.BREVSERVER, false, false),
    BI01S07(
        "Varsel om begrenset revurdering av barnebidrag",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01B01(
        "Vedtak barnebidrag",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false,
        støttetSpråk = listOf(StøttetSpråk.NB, StøttetSpråk.EN, StøttetSpråk.FR)
    ),
    BI01B04(
        "Revurdering Bidrags pga forskudd",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false,
        støttetSpråk = støttetSpråkListeNynorsk
    ),
    BI01B20(
        "Vedtak utland skjønn fastsettelse",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01B21(
        "Vedtak utland skjønn endring",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S06(
        "Varsel om fastsettelse av barnebidrag fra barnets fødsel",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S31(
        "Fasts. eget tiltak uten innkr varsel til partene",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S32(
        "Fastsettelse eget tiltak varsel til BM",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S33(
        "Endring varsel eget tiltak pga 25%",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S34(
        "Endring varsel eget tiltak pga barnetillegg",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S35(
        "Varsel om endring av barnebidraget på grunn av forholdsmessig fordeling",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S36(
        "Endring varsel eget tiltak pga forsørgingstillegg",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S46(
        "Varsel om endring av oppfostringsbidraget på grunn av forholdsmessig fordeling",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S62(
        "Varsel om fastsettelse av barnebidrag til den bidragspliktiges barnetillegg",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S63(
        "Fastsettelse bidrag forsørgingstillegg varsel",
        BrevType.UTGÅENDE,
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
        "Revurdering bidrag pga FO til BM",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S23(
        "Revurdering bidrag pga FO til BP",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S01(
        "NAV har mottatt søknad om barnebidrag",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S12(
        "Du har søkt om barnebidrag",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S52(
        "Forhåndsvarsel om mottatt søknad om oppfostringsbidrag",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S53(
        "Forhåndsvarsel om mottatt søknad om oppfostringsbidrag",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ), // Erstattes av BI01S52
    BI01S13(
        "Du har søkt om barnebidrag",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01B02(
        "Vedtak tilleggsbidrag",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false,
        støttetSpråk = listOf(StøttetSpråk.NB, StøttetSpråk.EN, StøttetSpråk.DE)
    ),
    BI01S05(
        "NAV har mottatt søknad om sletting av bidragsgjeld",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S14(
        "Du har søkt om endring av barnebidraget",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S26(
        "Endring varsel til motparten",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S47(
        "Forhåndsvarsel om mottatt søknad om endring av oppfostringsbidrag",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S48(
        "Forhåndsvarsel om mottatt søknad om endring av oppfostringsbidrag",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S49(
        "Forhåndsvarsel om mottatt søknad om endring av oppfostringsbidrag",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01I50(
        "Klage - vedtak ektefellebidrag",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01I01("Vedtak ektefellebidrag", BrevType.UTGÅENDE, BestillingSystem.BREVSERVER, false, false),
    BI01S43(
        "Fastsettelse ektefellebidrag orientering",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S44(
        "Fastsettelse ektefellebidrag varsel til BP",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S37(
        "Bortfall ektefellebidrag BP død orientering til BM",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S38(
        "Bortfall ektefellebidrag orientering til partene",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S39(
        "Bortfall ektefellebidrag nytt ekteskap orientering",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S41(
        "Vedtak - bortfall nytt ekteskap",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S42(
        "Endring ektefellebidrag orientering til søkeren",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S45(
        "Varsel om ny beregning av bidragsforskudd og varsel om mulig tilbakebetaling",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ), // TODO: Slettes og erstattes av S27
    BI01H01("Farskap innkalling mor", BrevType.UTGÅENDE, BestillingSystem.BREVSERVER, false, false),
    BI01H03(
        "Melding om blodprøver i farskapsak",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01H04(
        "Pålegg om å framstille barn for å gi blodprøve",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01H05(
        "Pålegg om blodprøve i farskapssak",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01H02(
        "Innkalling farskapssak oppgitt far",
        BrevType.UTGÅENDE,
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
    BI01J50("Klage - vedtak gebyr", BrevType.UTGÅENDE, BestillingSystem.BREVSERVER, false, false),
    BI01S03(
        "NAV har mottatt søknad om barnebidrag etter fylte 18 år - forhåndsvarsel",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01K50(
        "Klage - vedtak tilbakekreving",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01A05(
        "Vedtak ikke tilbakekreving",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false,
        støttetSpråk = støttetSpråkListeNynorsk
    ),
    BI01G04(
        "Vedtak innkreving særtilskudd",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01E50(
        "Klage - vedtak bidrag til særlige utgifter",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01E01(
        "Vedtak bidrag til særlige utgifter",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01E02(
        "Vedtak særtilskudd avslag",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01E03(
        "Vedtak bidrag til særlige utgifter tannregulering",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S04(
        "NAV har mottatt søknad om bidrag til særlige utgifter",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S18(
        "Du har søkt om bidrag til særlige utgifter",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01F50(
        "Klage - vedtak ettergivelse gjeld",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01F01(
        "Vedtak ettergivelse innvilget",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01F02(
        "Vedtak ettergivelse avslag",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S17(
        "Informasjon om din søknad om sletting av bidragsgjeld",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S50(
        "Ettergivelse oppfostringsbidrag orientering til BP",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S51(
        "Ettergivelse oppfostringsbidrag varsel kommune",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01A02(
        "Vedtak tilbakekreving",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false,
        støttetSpråk = støttetSpråkListeNynorsk
    ),
    BI01S54(
        "Varsel om tilbakekreving av bidragsforskudd - inntekt",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S55(
        "Varsel tilbakekreving av bidragsforskudd - partene bor sammen",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S56(
        "Varsel tilbakekreving av bidragsforskudd - ikke opphold i Riket",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S57(
        "Varsel tilbakekreving av bidragsforskudd - direkte betalinger",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S58(
        "Varsel tilbakekreving av bidragsforskudd - ikke omsorg",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S59(
        "Informasjon om din søknad om sletting av bidragsgjeld",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S24(
        "NAV har mottatt søknad om barnebidrag etter fylte 18 år - innhenting av opplysninger",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01B03(
        "Vedtak barnebidrag etter fylte 18 år",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S15(
        "Du har søkt om barnebidrag etter fylte 18 år",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01S16(
        "Du har søkt om barnebidrag etter fylte 18 år",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),
    BI01B11(
        "Vedtak tilleggsbidrag over 18 år",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        false,
        false
    ),

    // Batchbrev
    BI01B05(
        "VEDTAK AUTOMATISK JUSTERING BARNEBIDRAG",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        true,
        false
    ),
    BI01S09(
        "Varsel om opphør barnebidrag etter fylte 18 år",
        BrevType.UTGÅENDE,
        BestillingSystem.BREVSERVER,
        true,
        false
    )
}
