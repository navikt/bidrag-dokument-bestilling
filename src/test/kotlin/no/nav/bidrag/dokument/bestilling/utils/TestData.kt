package no.nav.bidrag.dokument.bestilling.utils

import no.nav.bidrag.dokument.bestilling.api.dto.MottakerAdresseTo
import no.nav.bidrag.dokument.bestilling.api.dto.SamhandlerAdresse
import no.nav.bidrag.dokument.bestilling.api.dto.SamhandlerInformasjon
import no.nav.bidrag.dokument.bestilling.consumer.dto.EnhetKontaktInfoDto
import no.nav.bidrag.dokument.bestilling.consumer.dto.EnhetPostadresseDto
import no.nav.bidrag.dokument.dto.OpprettDokumentDto
import no.nav.bidrag.dokument.dto.OpprettJournalpostResponse
import no.nav.bidrag.domain.bool.LevdeAdskilt
import no.nav.bidrag.domain.bool.UkjentPart
import no.nav.bidrag.domain.enums.Adressetype
import no.nav.bidrag.domain.enums.Bidragssakstatus
import no.nav.bidrag.domain.enums.Diskresjonskode
import no.nav.bidrag.domain.enums.Rolletype
import no.nav.bidrag.domain.enums.Sakskategori
import no.nav.bidrag.domain.ident.AktørId
import no.nav.bidrag.domain.ident.PersonIdent
import no.nav.bidrag.domain.string.Adresselinje1
import no.nav.bidrag.domain.string.Adresselinje2
import no.nav.bidrag.domain.string.Adresselinje3
import no.nav.bidrag.domain.string.Bruksenhetsnummer
import no.nav.bidrag.domain.string.Enhetsnummer
import no.nav.bidrag.domain.string.FulltNavn
import no.nav.bidrag.domain.string.Kortnavn
import no.nav.bidrag.domain.string.Landkode2
import no.nav.bidrag.domain.string.Landkode3
import no.nav.bidrag.domain.string.Postnummer
import no.nav.bidrag.domain.string.Poststed
import no.nav.bidrag.domain.string.Saksnummer
import no.nav.bidrag.domain.tid.Dødsdato
import no.nav.bidrag.domain.tid.Fødselsdato
import no.nav.bidrag.domain.tid.OpprettetDato
import no.nav.bidrag.transport.person.PersonAdresseDto
import no.nav.bidrag.transport.person.PersonDto
import no.nav.bidrag.transport.sak.BidragssakDto
import no.nav.bidrag.transport.sak.RolleDto
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
    landkode = "NO",
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

fun createSakResponse(): BidragssakDto {
    return BidragssakDto(
        saksnummer = Saksnummer(DEFAULT_SAKSNUMMER),
        eierfogd = Enhetsnummer("4806"),
        roller = listOf(
            RolleDto(
                fødselsnummer = BM1.ident,
                type = Rolletype.BM
            ),
            RolleDto(
                fødselsnummer = BP1.ident,
                type = Rolletype.BP
            ),
            RolleDto(
                fødselsnummer = BARN1.ident,
                type = Rolletype.BA
            ),
            RolleDto(
                fødselsnummer = BARN2.ident,
                type = Rolletype.BA
            )
        ),
        saksstatus = Bidragssakstatus.IN,
        kategori = Sakskategori.N,
        opprettetDato = OpprettetDato(LocalDate.now()),
        levdeAdskilt = LevdeAdskilt(false),
        ukjentPart = UkjentPart(false)
    )
}

fun createPersonResponse(
    ident: String,
    navn: String,
    kortNavn: String? = null,
    fodselsdato: LocalDate? = null,
    dodsdato: LocalDate? = null,
    aktorId: String? = "313213",
    diskresjonskode: Diskresjonskode? = null

): PersonDto {
    return PersonDto(
        ident = PersonIdent(ident),
        navn = FulltNavn(navn),
        kortnavn = kortNavn?.let { Kortnavn(it) },
        fødselsdato = fodselsdato?.let { Fødselsdato(it) },
        dødsdato = dodsdato?.let { Dødsdato(it) },
        aktørId = aktorId?.let { AktørId(it) },
        diskresjonskode = diskresjonskode
    )
}

fun createPostAdresseResponse(): PersonAdresseDto {
    return PersonAdresseDto(
        adresselinje1 = Adresselinje1("Adresselinje1"),
        adresselinje2 = Adresselinje2("Adresselinje2"),
        postnummer = Postnummer("3030"),
        poststed = Poststed("Drammen"),
        land = Landkode2("NO"),
        land3 = Landkode3("NOR"),
        bruksenhetsnummer = Bruksenhetsnummer("H0201"),
        adressetype = Adressetype.BOSTEDSADRESSE
    )
}

fun createPostAdresseResponseUtenlandsk(): PersonAdresseDto {
    return PersonAdresseDto(
        adresselinje1 = Adresselinje1("Utenlandsk Adresselinje1"),
        adresselinje2 = Adresselinje2("Utenlandsk Adresselinje2"),
        adresselinje3 = Adresselinje3("United states of America"),
        land = Landkode2("US"),
        land3 = Landkode3("USA"),
        adressetype = Adressetype.BOSTEDSADRESSE
    )
}

fun createOpprettJournalpostResponse(
    tittel: String = "Tittel på dokument",
    journalpostId: String = "123123",
    dokumentReferanse: String = "dokref1"
): OpprettJournalpostResponse {
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
