package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

data class NexaPaletteSpec(
    val key: String,
    val labelAr: String,
    val labelEn: String,
    val light: ColorScheme,
    val dark: ColorScheme,
    val previewColor: Color
)

object NexaPalettes {
    val noxEspresso = NexaPaletteSpec(
        key = "NOX_ESPRESSO",
        labelAr = "الأبيض والقهوة",
        labelEn = "NEXA Classic",
        previewColor = Color(0xFF171310),
        light = lightColorScheme(
            primary = Color(0xFF171310),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE5D7C8),
            onPrimaryContainer = Color(0xFF171310),
            secondary = Color(0xFF7B573F),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFF5ECE3),
            onSecondaryContainer = Color(0xFF281A11),
            tertiary = Color(0xFF2D6A5F),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFE0EFEA),
            onTertiaryContainer = Color(0xFF0E2C26),
            background = Color(0xFFEFE6DC),
            onBackground = Color(0xFF171310),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF171310),
            surfaceVariant = Color(0xFFF9F5F0),
            onSurfaceVariant = Color(0xFF5D5047),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFFFFFFF),
            surfaceContainer = Color(0xFFF7F1EA),
            surfaceContainerHigh = Color(0xFFEDE2D6),
            surfaceContainerHighest = Color(0xFFE4D6C8),
            outline = Color(0xFFD5C6B8),
            outlineVariant = Color(0xFFE8DDD2)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFAF6F0),
            onPrimary = Color(0xFF14100D),
            primaryContainer = Color(0xFF332922),
            onPrimaryContainer = Color(0xFFFAF6F0),
            secondary = Color(0xFFD0A984),
            onSecondary = Color(0xFF2B1B10),
            secondaryContainer = Color(0xFF2E241D),
            onSecondaryContainer = Color(0xFFF2E6DA),
            tertiary = Color(0xFF81C5B7),
            onTertiary = Color(0xFF0B2923),
            tertiaryContainer = Color(0xFF1C3A34),
            onTertiaryContainer = Color(0xFFDDF2EC),
            background = Color(0xFF120E0C),
            onBackground = Color(0xFFF7F2EC),
            surface = Color(0xFF1E1815),
            onSurface = Color(0xFFF7F2EC),
            surfaceVariant = Color(0xFF28201C),
            onSurfaceVariant = Color(0xFFC8BAB0),
            surfaceContainerLowest = Color(0xFF0E0B09),
            surfaceContainerLow = Color(0xFF181310),
            surfaceContainer = Color(0xFF1E1815),
            surfaceContainerHigh = Color(0xFF28201C),
            surfaceContainerHighest = Color(0xFF332924),
            outline = Color(0xFF473B33),
            outlineVariant = Color(0xFF322923)
        )
    )

    val sereneSage = NexaPaletteSpec(
        key = "SERENE_SAGE",
        labelAr = "الهدوء العشبي",
        labelEn = "Serene Sage",
        previewColor = Color(0xFF1F6E5C),
        light = lightColorScheme(
            primary = Color(0xFF1F6E5C),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFCDEEE3),
            onPrimaryContainer = Color(0xFF062920),
            secondary = Color(0xFF4D6B62),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFDCEAE4),
            onSecondaryContainer = Color(0xFF132620),
            tertiary = Color(0xFFB57B45),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFF9E2CB),
            onTertiaryContainer = Color(0xFF361E07),
            background = Color(0xFFF6F9F7),
            onBackground = Color(0xFF151F1C),
            surface = Color(0xFFFBFDFC),
            onSurface = Color(0xFF151F1C),
            surfaceVariant = Color(0xFFE6EFEA),
            onSurfaceVariant = Color(0xFF44554F),
            outline = Color(0xFFB7C7C1)
        ),
        dark = darkColorScheme(
            primary = Color(0xFF7AD9BD),
            onPrimary = Color(0xFF00382C),
            primaryContainer = Color(0xFF0B5141),
            onPrimaryContainer = Color(0xFFCDEEE3),
            secondary = Color(0xFFA7C6BB),
            onSecondary = Color(0xFF142D26),
            secondaryContainer = Color(0xFF2B463D),
            onSecondaryContainer = Color(0xFFDCEAE4),
            tertiary = Color(0xFFE9BA8A),
            onTertiary = Color(0xFF422608),
            tertiaryContainer = Color(0xFF5D3A17),
            onTertiaryContainer = Color(0xFFF9E2CB),
            background = Color(0xFF0E1614),
            onBackground = Color(0xFFE2ECE8),
            surface = Color(0xFF131D1A),
            onSurface = Color(0xFFE2ECE8),
            surfaceVariant = Color(0xFF21302B),
            onSurfaceVariant = Color(0xFFB3C5BE),
            outline = Color(0xFF3A4D46)
        )
    )

    val warmSand = NexaPaletteSpec(
        key = "WARM_SAND",
        labelAr = "رمل دافئ",
        labelEn = "Warm Sand",
        previewColor = Color(0xFFA46334),
        light = lightColorScheme(
            primary = Color(0xFFA46334),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFF6DFCE),
            onPrimaryContainer = Color(0xFF351A07),
            secondary = Color(0xFF6B6559),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFEAE3D7),
            onSecondaryContainer = Color(0xFF242019),
            tertiary = Color(0xFF2B6B62),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFD0ECE7),
            onTertiaryContainer = Color(0xFF0A2A25),
            background = Color(0xFFFCF8F3),
            onBackground = Color(0xFF221C16),
            surface = Color(0xFFFFFCF8),
            onSurface = Color(0xFF221C16),
            surfaceVariant = Color(0xFFF1E7DA),
            onSurfaceVariant = Color(0xFF5A5044),
            outline = Color(0xFFD2C4B4)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFF0B489),
            onPrimary = Color(0xFF4B270B),
            primaryContainer = Color(0xFF6F3E1A),
            onPrimaryContainer = Color(0xFFF6DFCE),
            secondary = Color(0xFFCFC6B8),
            onSecondary = Color(0xFF352F24),
            secondaryContainer = Color(0xFF4C4539),
            onSecondaryContainer = Color(0xFFEAE3D7),
            tertiary = Color(0xFF83D4C8),
            onTertiary = Color(0xFF003831),
            background = Color(0xFF181410),
            onBackground = Color(0xFFECE4DC),
            surface = Color(0xFF201A15),
            onSurface = Color(0xFFECE4DC),
            surfaceVariant = Color(0xFF332A22),
            onSurfaceVariant = Color(0xFFCBBFB2),
            outline = Color(0xFF52463A)
        )
    )

    val midnightFocus = NexaPaletteSpec(
        key = "MIDNIGHT_FOCUS",
        labelAr = "تركيز ليلي",
        labelEn = "Midnight Focus",
        previewColor = Color(0xFF365999),
        light = lightColorScheme(
            primary = Color(0xFF365999),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFD9E4FA),
            onPrimaryContainer = Color(0xFF0B1E40),
            secondary = Color(0xFF4F5E78),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFE0E7F3),
            onSecondaryContainer = Color(0xFF172133),
            tertiary = Color(0xFF227B72),
            onTertiary = Color.White,
            background = Color(0xFFF5F7FB),
            onBackground = Color(0xFF121824),
            surface = Color(0xFFFCFDFF),
            onSurface = Color(0xFF121824),
            surfaceVariant = Color(0xFFE4E9F3),
            onSurfaceVariant = Color(0xFF455066),
            outline = Color(0xFFBDC7DA)
        ),
        dark = darkColorScheme(
            primary = Color(0xFF9FBFFF),
            onPrimary = Color(0xFF0A2658),
            primaryContainer = Color(0xFF20417B),
            onPrimaryContainer = Color(0xFFD9E4FA),
            secondary = Color(0xFFB5C2D8),
            onSecondary = Color(0xFF1E293D),
            secondaryContainer = Color(0xFF334057),
            onSecondaryContainer = Color(0xFFE0E7F3),
            tertiary = Color(0xFF6CD9CC),
            onTertiary = Color(0xFF003731),
            background = Color(0xFF0B101B),
            onBackground = Color(0xFFE2E8F4),
            surface = Color(0xFF111827),
            onSurface = Color(0xFFE2E8F4),
            surfaceVariant = Color(0xFF1D273B),
            onSurfaceVariant = Color(0xFFB0BCD2),
            outline = Color(0xFF35445E)
        )
    )

    val oceanMist = NexaPaletteSpec(
        key = "OCEAN_MIST",
        labelAr = "نسيم البحر",
        labelEn = "Ocean Mist",
        previewColor = Color(0xFF0F748F),
        light = lightColorScheme(
            primary = Color(0xFF0F748F),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFCBEFF8),
            onPrimaryContainer = Color(0xFF042731),
            secondary = Color(0xFF4A6973),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFDCEBF0),
            onSecondaryContainer = Color(0xFF12262C),
            tertiary = Color(0xFF7B66A4),
            onTertiary = Color.White,
            background = Color(0xFFF3F9FB),
            onBackground = Color(0xFF111D21),
            surface = Color(0xFFFAFEFF),
            onSurface = Color(0xFF111D21),
            surfaceVariant = Color(0xFFE0EEF2),
            onSurfaceVariant = Color(0xFF40565D),
            outline = Color(0xFFB5CBD2)
        ),
        dark = darkColorScheme(
            primary = Color(0xFF7DD8EE),
            onPrimary = Color(0xFF003643),
            primaryContainer = Color(0xFF085164),
            onPrimaryContainer = Color(0xFFCBEFF8),
            secondary = Color(0xFFAEC8D0),
            onSecondary = Color(0xFF172D34),
            background = Color(0xFF0A1418),
            onBackground = Color(0xFFDFECF0),
            surface = Color(0xFF101C21),
            onSurface = Color(0xFFDFECF0),
            surfaceVariant = Color(0xFF1D2E35),
            onSurfaceVariant = Color(0xFFADC2C8),
            outline = Color(0xFF364C54)
        )
    )

    val botanicalGold = NexaPaletteSpec(
        key = "BOTANICAL_GOLD",
        labelAr = "زيتون وذهب",
        labelEn = "Olive & Gold",
        previewColor = Color(0xFF4F6F3A),
        light = lightColorScheme(
            primary = Color(0xFF4F6F3A),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFDCEAC9),
            onPrimaryContainer = Color(0xFF142608),
            secondary = Color(0xFF786942),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFF1E7CC),
            onSecondaryContainer = Color(0xFF28200D),
            tertiary = Color(0xFF2E6E65),
            onTertiary = Color.White,
            background = Color(0xFFF8FAF3),
            onBackground = Color(0xFF191F14),
            surface = Color(0xFFFDFEF9),
            onSurface = Color(0xFF191F14),
            surfaceVariant = Color(0xFFEAEFE0),
            onSurfaceVariant = Color(0xFF4B553E),
            outline = Color(0xFFC2CAAE)
        ),
        dark = darkColorScheme(
            primary = Color(0xFFAAD28B),
            onPrimary = Color(0xFF1C360B),
            primaryContainer = Color(0xFF365122),
            onPrimaryContainer = Color(0xFFDCEAC9),
            secondary = Color(0xFFD8C698),
            onSecondary = Color(0xFF392F14),
            background = Color(0xFF11160D),
            onBackground = Color(0xFFE5ECDD),
            surface = Color(0xFF181F13),
            onSurface = Color(0xFFE5ECDD),
            surfaceVariant = Color(0xFF26301F),
            onSurfaceVariant = Color(0xFFB9C4AB),
            outline = Color(0xFF404C35)
        )
    )

    val all: List<NexaPaletteSpec> = listOf(noxEspresso, sereneSage, warmSand, midnightFocus, oceanMist, botanicalGold)

    fun byKey(key: String): NexaPaletteSpec = all.firstOrNull { it.key == key } ?: noxEspresso
}
