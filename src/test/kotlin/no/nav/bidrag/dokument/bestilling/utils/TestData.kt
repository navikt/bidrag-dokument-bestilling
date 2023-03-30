package no.nav.bidrag.dokument.bestilling.utils

import no.nav.bidrag.dokument.bestilling.api.dto.MottakerAdresseTo
import no.nav.bidrag.dokument.bestilling.api.dto.SamhandlerAdresse
import no.nav.bidrag.dokument.bestilling.api.dto.SamhandlerInformasjon
import no.nav.bidrag.dokument.bestilling.konsumer.dto.EnhetKontaktInfoDto
import no.nav.bidrag.dokument.bestilling.konsumer.dto.EnhetPostadresseDto
import no.nav.bidrag.dokument.bestilling.konsumer.dto.HentPersonResponse
import no.nav.bidrag.dokument.bestilling.konsumer.dto.HentPostadresseResponse
import no.nav.bidrag.dokument.bestilling.konsumer.dto.HentSakResponse
import no.nav.bidrag.dokument.bestilling.konsumer.dto.RolleType
import no.nav.bidrag.dokument.bestilling.konsumer.dto.SakRolle
import no.nav.bidrag.dokument.dto.OpprettDokumentDto
import no.nav.bidrag.dokument.dto.OpprettJournalpostResponse
import java.time.LocalDate

val DEFAULT_TITLE_DOKUMENT = "Tittel på dokumentet"
val DEFAULT_SAKSNUMMER = "123312321321"

val SAMHANDLER_IDENT = "80000123213"

val SAKSBEHANDLER_IDENT = "Z99999"
val SAKSBEHANDLER_NAVN = "Saksbehandlersen, Saksbehandler Mellomnavn"

val SAMHANDLER_INFO = SamhandlerInformasjon(
    navn = "Samhandler samhandlersen",
    spraak = "NB",
    adresse = SamhandlerAdresse(
        adresselinje1 = "Samhandler adresselinje 1",
        adresselinje2 = "Samhandler adresselinje 2",
        adresselinje3 = "Samhandler adresselinje 3",
        postnummer = "3000",
        landkode = "NOR"
    )
)

val SAMHANDLER_MOTTAKER_ADRESSE = MottakerAdresseTo(
    adresselinje1 = "Samhandler adresselinje 1",
    adresselinje2 = "Samhandler adresselinje 2",
    adresselinje3 = "Samhandler adresselinje 3",
    postnummer = "3000",
    landkode3 = "NOR"
)

val ANNEN_MOTTAKER = createPersonResponse(
    "444213123123333",
    "Etternavn, BPFornavn Annen mottaker",
    fodselsdato = LocalDate.parse("2001-05-06")
)
val BP1 = createPersonResponse(
    "444213123123",
    "Etternavn, BPFornavn Bidragspliktig",
    fodselsdato = LocalDate.parse("2001-05-06")
)
val BM1 = createPersonResponse(
    "123123123123",
    "Etternavn, BMFornavn Bidragsmottaker",
    fodselsdato = LocalDate.parse("2000-03-06")
)
val BARN1 = createPersonResponse(
    "3323213",
    "Etternavn, Barn1 Mellomnavn",
    fodselsdato = LocalDate.parse("2020-05-06")
)
val BARN2 = createPersonResponse(
    "333333323213",
    "Etternavn, Barn2 Mellomnavn",
    fodselsdato = LocalDate.parse("2018-03-20")
)

val BARN3 = createPersonResponse(
    "412421412421",
    "Etternavn, Barn3",
    fodselsdato = LocalDate.parse("2014-03-20")
)

fun createSakResponse(): HentSakResponse {
    return HentSakResponse(
        saksnummer = DEFAULT_SAKSNUMMER,
        eierfogd = "4806",
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

fun createPersonResponse(
    ident: String,
    navn: String,
    kortNavn: String? = null,
    fodselsdato: LocalDate? = null,
    dodsdato: LocalDate? = null,
    aktorId: String? = "313213",
    diskresjonskode: String? = null

): HentPersonResponse {
    return HentPersonResponse(ident, navn, kortNavn, fodselsdato, dodsdato, aktorId, diskresjonskode = diskresjonskode)
}
fun createPostAdresseResponse(): HentPostadresseResponse {
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

fun createPostAdresseResponseUtenlandsk(): HentPostadresseResponse {
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
