package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val CairoFontFamily = FontFamily(Font(R.font.cairo, FontWeight.Normal))
val TajawalFontFamily = FontFamily(Font(R.font.tajawal, FontWeight.Normal))
val AmiriFontFamily = FontFamily(Font(R.font.amiri, FontWeight.Normal))
val ReadexProFontFamily = FontFamily(Font(R.font.readex_pro, FontWeight.Normal))

object NexaFonts {
    fun resolve(fontKey: String): FontFamily = when (fontKey) {
        "TAJAWAL" -> TajawalFontFamily
        "AMIRI" -> AmiriFontFamily
        "READEX_PRO" -> ReadexProFontFamily
        else -> CairoFontFamily
    }

    val options = listOf(
        "CAIRO" to "Cairo",
        "TAJAWAL" to "Tajawal",
        "READEX_PRO" to "Readex Pro",
        "AMIRI" to "Amiri"
    )
}

fun buildNexaTypography(fontKey: String): Typography {
    val family = NexaFonts.resolve(fontKey)
    return Typography(
        displayLarge = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 44.sp),
        displayMedium = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 38.sp),
        headlineLarge = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 34.sp),
        headlineMedium = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 30.sp),
        headlineSmall = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 26.sp),
        titleLarge = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 25.sp),
        titleMedium = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 23.sp),
        titleSmall = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
        bodyLarge = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
        bodySmall = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp),
        labelLarge = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
        labelMedium = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
        labelSmall = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp)
    )
}
