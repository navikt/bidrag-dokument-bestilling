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
var BM_PERSON_NAVN_1 = "Etternavn, BMFornavn Bidragsmottaker"

var BP_PERSON_ID_1 = "444213123123"
var BP_PERSON_NAVN_1 = "Etternavn, BPFornavn Bidragspliktig"

var BARN_ID_1 = "3323213"
var BARN_NAVN_1 = "Etternavn, Barn1 Mellomnavn"

var BARN_ID_2 = "333333323213"
var BARN_NAVN_2 = "Etternavn, Barn2 Mellomnavn"

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
            ),
            SakRolle(
                foedselsnummer = BARN_ID_1,
                rolleType = RolleType.BA
            ),
            SakRolle(
                foedselsnummer = BARN_ID_2,
                rolleType = RolleType.BA
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
        land = "NO",
        bruksenhetsnummer = "H0101"
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
            postnummer = "3040",
            poststed = "Drammen",
            land = "Norge"
        )
    )
}