package no.nav.bidrag.dokument.bestilling.consumer.dto

import no.nav.bidrag.domene.enums.Diskresjonskode
import no.nav.bidrag.transport.person.PersonDto

fun PersonDto.fornavnEtternavn() = kortnavn?.verdi ?: "" // listOfNotNull(fornavn?.verdi, mellomnavn?.verdi, etternavn?.verdi).joinToString(" ")
fun PersonDto.isKode6() = diskresjonskode == Diskresjonskode.SPSF
fun PersonDto.isDod() = dødsdato != null
