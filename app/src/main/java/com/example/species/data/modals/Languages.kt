package com.example.species.data.modals

import com.google.mlkit.nl.translate.TranslateLanguage

sealed class Languages(val lang: String) {
    object ENG : Languages(TranslateLanguage.ENGLISH)
    object MAR : Languages(TranslateLanguage.MARATHI)
    object HIN : Languages(TranslateLanguage.HINDI)
    object TAM : Languages(TranslateLanguage.TAMIL)
    object TEL : Languages(TranslateLanguage.TELUGU)
    object KAN : Languages(TranslateLanguage.KANNADA)
    object BEN : Languages(TranslateLanguage.BENGALI)
    object ARB : Languages(TranslateLanguage.ARABIC)
    object JAP : Languages(TranslateLanguage.JAPANESE)
    object KOR : Languages(TranslateLanguage.KOREAN)
    object IND : Languages(TranslateLanguage.INDONESIAN)
    object CHI : Languages(TranslateLanguage.CHINESE)
    object ESP : Languages(TranslateLanguage.SPANISH)
    object RUS : Languages(TranslateLanguage.RUSSIAN)
}
