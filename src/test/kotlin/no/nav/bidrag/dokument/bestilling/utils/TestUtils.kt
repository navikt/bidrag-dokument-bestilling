package no.nav.bidrag.dokument.bestilling.utils

import org.springframework.core.io.ClassPathResource

fun readFile(filePath: String): String {
    return String(ClassPathResource("testdata/$filePath").inputStream.readAllBytes())
}

fun readFile2(filePath: String): String {
    return String(ClassPathResource("__files/$filePath").inputStream.readAllBytes())
}