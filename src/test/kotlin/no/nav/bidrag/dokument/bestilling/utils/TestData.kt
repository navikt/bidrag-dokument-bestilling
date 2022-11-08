package no.nav.bidrag.dokument.bestilling.utils

import no.nav.bidrag.dokument.bestilling.model.EnhetKontaktInfoDto
import no.nav.bidrag.dokument.bestilling.model.EnhetPostadresseDto
import no.nav.bidrag.dokument.bestilling.model.HentPersonResponse
import no.nav.bidrag.dokument.bestilling.model.HentPostadresseResponse
import no.nav.bidrag.dokument.bestilling.model.HentSakResponse
import no.nav.bidrag.dokument.bestilling.model.RolleType
import no.nav.bidrag.dokument.bestilling.model.SakRolle
import no.nav.bidrag.dokument.dto.OpprettDokumentDto
import no.nav.bidrag.dokument.dto.OpprettJournalpostResponse
import java.time.LocalDate


var BM_PERSON_ID_1 = "123123123123"
var BM_PERSON_NAVN_1 = "Etternavn, BMFornavn Bidragsmottaker"

val SAKSBEHANDLER_IDENT = "Z99999"
val SAKSBEHANDLER_NAVN = "Saksbehandler Saksbehandlersen"

val BP1 = HentPersonResponse("444213123123", "Etternavn, BPFornavn Bidragspliktig", null, LocalDate.parse("2001-05-06"), null, "123335555")
val BM1 = HentPersonResponse("123123123123", "Etternavn, BMFornavn Bidragsmottaker", null, LocalDate.parse("2000-03-06"), null, "123335555")
val BARN1 = HentPersonResponse("3323213", "Etternavn, Barn1 Mellomnavn", null, LocalDate.parse("2020-05-06"), null, "123335555")
val BARN2 = HentPersonResponse("333333323213", "Etternavn, Barn2 Mellomnavn", null, LocalDate.parse("2018-03-20"), null, "123123123")
val BARN3_DOD = HentPersonResponse("5124124124124", "Etternavn, Barn3 Mellomnavn", null, LocalDate.parse("2018-03-20"), LocalDate.parse("2018-05-20"), "123123123")

var SAKSNUMMER1 = "123213123"
fun createSakResponse(): HentSakResponse{
    return HentSakResponse(
        saksnummer = SAKSNUMMER1,
        roller = listOf(
            SakRolle(
                foedselsnummer = BM1.ident,
                rolleType = RolleType.BM
            ),
            SakRolle(
                foedselsnummer = BP1.ident,
                rolleType = RolleType.BP
            ),
            SakRolle(
                foedselsnummer = BARN1.ident,
                rolleType = RolleType.BA
            ),
            SakRolle(
                foedselsnummer = BARN2.ident,
                rolleType = RolleType.BA
            )
        )
    )
}
fun createPostAdresseResponse(): HentPostadresseResponse{
    return HentPostadresseResponse(
        adresselinje1 = "Adresselinje1",
        adresselinje2 = "Adresselinje2",
        adresselinje3 = null,
        postnummer = "3030",
        poststed = "Drammen",
        land = "NO",
        land3 = "NOR",
        bruksenhetsnummer = "H0201"
    )
}

fun createPostAdresseResponseUtenlandsk(): HentPostadresseResponse{
    return HentPostadresseResponse(
        adresselinje1 = "Utenlandsk Adresselinje1",
        adresselinje2 = "Utenlandsk Adresselinje2",
        adresselinje3 = "United states of America",
        postnummer = null,
        poststed = null,
        land = "US",
        land3 = "USA",
        bruksenhetsnummer = null
    )
}

fun createOpprettJournalpostResponse(tittel: String = "Tittel på dokument", journalpostId: String = "123123", dokumentReferanse: String = "dokref1"): OpprettJournalpostResponse {
    return OpprettJournalpostResponse(
        dokumenter = listOf(
            OpprettDokumentDto(
                tittel = tittel,
                dokumentreferanse = dokumentReferanse
            )
        ),
        journalpostId = journalpostId
    )
}

fun createEnhetKontaktInformasjon(): EnhetKontaktInfoDto {

    return EnhetKontaktInfoDto(
        enhetIdent = "4806",
        enhetNavn = "NAV Familie- og pensjonsytelser Drammen",
        telefonnummer = "55553333",
        postadresse = EnhetPostadresseDto(
            adresselinje1 = "Postboks 1583",
            adresselinje2 = "Linje2",
            postnummer = "3040",
            poststed = "Drammen",
            land = "Norge"
        )
    )
}