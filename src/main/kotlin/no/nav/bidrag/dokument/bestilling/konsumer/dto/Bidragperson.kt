package no.nav.bidrag.dokument.bestilling.konsumer.dto

import no.nav.bidrag.domain.enums.Diskresjonskode
import no.nav.bidrag.transport.person.PersonDto

fun PersonDto.fornavnEtternavn() = listOfNotNull(fornavn?.verdi, mellomnavn?.verdi, etternavn?.verdi).joinToString(" ")
fun PersonDto.isKode6() = diskresjonskode == Diskresjonskode.SPSF
fun PersonDto.isDod() = dødsdato != null
data class HentPersonInfoRequest(
    var ident: String,
    val verdi: String = ident
)
