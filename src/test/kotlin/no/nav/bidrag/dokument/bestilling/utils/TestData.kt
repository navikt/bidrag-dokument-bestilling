package no.nav.bidrag.dokument.bestilling.utils

import no.nav.bidrag.dokument.bestilling.model.EnhetKontaktInfoDto
import no.nav.bidrag.dokument.bestilling.model.EnhetPostadresseDto
import no.nav.bidrag.dokument.bestilling.model.HentPostadresseResponse
import no.nav.bidrag.dokument.bestilling.model.HentSakResponse
import no.nav.bidrag.dokument.bestilling.model.RolleType
import no.nav.bidrag.dokument.bestilling.model.SakRolle
import no.nav.bidrag.dokument.dto.OpprettDokumentDto
import no.nav.bidrag.dokument.dto.OpprettJournalpostResponse


var BM_PERSON_ID_1 = "123123123123"
var BM_PERSON_NAVN_1 = "Navn Bidragsmottaker"

var BP_PERSON_ID_1 = "444213123123"
var BP_PERSON_NAVN_1 = "Navn Bidragspliktig"

var SAKSNUMMER1 = "123213123"
fun createSakResponse(): HentSakResponse{
    return HentSakResponse(
        saksnummer = SAKSNUMMER1,
        roller = listOf(
            SakRolle(
                foedselsnummer = BM_PERSON_ID_1,
                rolleType = RolleType.BM
            ),
            SakRolle(
                foedselsnummer = BP_PERSON_ID_1,
                rolleType = RolleType.BP
            )
        )
    )
}
fun createPostAdresseResponse(): HentPostadresseResponse{
    return HentPostadresseResponse(
        adresselinje1 = "Adresselinje1",
        adresselinje2 = "Adresselinje2",
        adresselinje3 = "Adresselinje3",
        postnummer = "3030",
        poststed = "Drammen",
        land = "NO"
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
        telefonnummer = "55553333",
        postadresse = EnhetPostadresseDto(
            adresselinje = "Postboks 1583",
            postboksnummer = "1583",
            postnummer = "3040",
            postboksanlegg = "Drammen"
        )
    )
}