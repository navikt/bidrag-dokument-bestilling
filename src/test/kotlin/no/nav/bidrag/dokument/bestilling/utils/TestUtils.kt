package no.nav.bidrag.dokument.bestilling.utils

import org.springframework.core.io.ClassPathResource

fun readFile(filePath: String): String {
    return String(ClassPathResource("__files/$filePath").inputStream.readAllBytes())
}
