package com.wongchoi500.babylog.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ── Pink (Girl) ──
private val PinkLightColorScheme = lightColorScheme(
    primary = PrimaryPink,
    secondary = SecondaryPink,
    tertiary = SoftYellow,
    background = BackgroundCream,
    surface = SurfaceWhite,
    primaryContainer = SecondaryPink.copy(alpha = 0.4f),
    onPrimary = Color.White,
    onSecondary = Color(0xFF5D4037),
    onTertiary = Color(0xFF5D4037),
    onBackground = Color(0xFF5D4037),
    onSurface = Color(0xFF5D4037),
    onPrimaryContainer = Color(0xFF5D4037)
)

private val PinkDarkColorScheme = darkColorScheme(
    primary = FontPinkDark,
    secondary = SecondaryDark,
    tertiary = SoftBlue,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E)
)

// ── Blue (Boy) ──
private val BlueLightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryBlue,
    tertiary = SoftGreen,
    background = BackgroundBlueCream,
    surface = SurfaceWhite,
    primaryContainer = SecondaryBlue.copy(alpha = 0.4f),
    onPrimary = Color.White,
    onSecondary = Color(0xFF37474F),
    onTertiary = Color(0xFF37474F),
    onBackground = Color(0xFF37474F),
    onSurface = Color(0xFF37474F),
    onPrimaryContainer = Color(0xFF37474F)
)

private val BlueDarkColorScheme = darkColorScheme(
    primary = FontBlueDark,
    secondary = SecondaryBlueDark,
    tertiary = SoftGreen,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E)
)

// ── Neutral (Unknown) ──
private val NeutralLightColorScheme = lightColorScheme(
    primary = PrimaryNeutral,
    secondary = SecondaryNeutral,
    tertiary = SoftYellow,
    background = BackgroundNeutral,
    surface = SurfaceWhite,
    primaryContainer = SecondaryNeutral.copy(alpha = 0.4f),
    onPrimary = Color.White,
    onSecondary = Color(0xFF424242),
    onTertiary = Color(0xFF424242),
    onBackground = Color(0xFF424242),
    onSurface = Color(0xFF424242),
    onPrimaryContainer = Color(0xFF424242)
)

private val NeutralDarkColorScheme = darkColorScheme(
    primary = PrimaryNeutralDark,
    secondary = SecondaryNeutralDark,
    tertiary = SoftYellow,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E)
)

@Composable
fun BabyLogTheme(
    babyGender: String = "",
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> when (babyGender) {
            "男" -> BlueDarkColorScheme
            "女" -> PinkDarkColorScheme
            else -> NeutralDarkColorScheme
        }
        else -> when (babyGender) {
            "男" -> BlueLightColorScheme
            "女" -> PinkLightColorScheme
            else -> NeutralLightColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
