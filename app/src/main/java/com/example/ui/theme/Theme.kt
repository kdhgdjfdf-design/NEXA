package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLayoutDirection
import com.example.data.local.DesignProfileEntity
import com.example.domain.i18n.LocalNexaStrings
import com.example.domain.i18n.NexaLang
import com.example.domain.i18n.NexaStrings

val LocalNexaIconStyle = staticCompositionLocalOf { "ROUNDED" }

@Composable
fun NexaTheme(
    profile: DesignProfileEntity?,
    languageCode: String,
    content: @Composable () -> Unit
) {
    val palette = NexaPalettes.byKey(profile?.paletteKey ?: "NOX_ESPRESSO")
    val isDark = when (profile?.themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }
    val colorScheme = if (isDark) palette.dark else palette.light
    val typography = buildNexaTypography(profile?.fontKey ?: "CAIRO")
    val cornerRadius = when (profile?.iconStyle) {
        "SHARP" -> 10.dp
        "MINIMAL" -> 14.dp
        else -> 20.dp
    }
    val shapes = Shapes(
        small = RoundedCornerShape((cornerRadius.value * 0.6f).dp),
        medium = RoundedCornerShape(cornerRadius),
        large = RoundedCornerShape((cornerRadius.value * 1.3f).dp)
    )
    val lang = NexaLang.fromCode(languageCode)
    val strings = NexaStrings.forLang(lang)
    val layoutDir = if (lang.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(
        LocalNexaStrings provides strings,
        LocalNexaIconStyle provides (profile?.iconStyle ?: "ROUNDED"),
        LocalLayoutDirection provides layoutDir
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = shapes,
            content = content
        )
    }
}
