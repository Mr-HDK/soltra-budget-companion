package com.hdk.soltra.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdk.soltra.R
import com.hdk.soltra.domain.AppThemeMode

private val LightScheme = lightColorScheme(
    primary = SoltraPrimary,
    onPrimary = Color.White,
    primaryContainer = SoltraSupport,
    onPrimaryContainer = Color(0xFF0A403E),
    secondary = SoltraSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7E1EF),
    onSecondaryContainer = Color(0xFF0D1829),
    tertiary = SoltraAccent,
    onTertiary = Color(0xFF301C08),
    tertiaryContainer = Color(0xFFFFDFC2),
    onTertiaryContainer = Color(0xFF4A2A0E),
    background = SoltraNeutralWarm,
    onBackground = SoltraSecondary,
    surface = Color(0xFFFFFBF6),
    onSurface = SoltraSecondary,
    surfaceVariant = Color(0xFFE5E1D8),
    onSurfaceVariant = Color(0xFF3E4B5F),
    outline = SoltraBorderLight,
    outlineVariant = SoltraDividerLight,
    inverseSurface = SoltraSecondary,
    inverseOnSurface = Color(0xFFF3F6FA),
    error = SoltraErrorLight,
    onError = Color.White,
)

private val DarkScheme = darkColorScheme(
    primary = SoltraPrimaryDark,
    onPrimary = Color(0xFF083534),
    primaryContainer = SoltraPrimary,
    onPrimaryContainer = Color(0xFFCEF1EE),
    secondary = Color(0xFFADC1D9),
    onSecondary = Color(0xFF112034),
    secondaryContainer = Color(0xFF26384F),
    onSecondaryContainer = Color(0xFFD8E5F4),
    tertiary = Color(0xFFE6B17A),
    onTertiary = Color(0xFF321C08),
    tertiaryContainer = Color(0xFF5B3717),
    onTertiaryContainer = Color(0xFFFFE3C6),
    background = SoltraBackgroundDark,
    onBackground = Color(0xFFE5EDF6),
    surface = SoltraSurfaceDark,
    onSurface = Color(0xFFE5EDF6),
    surfaceVariant = SoltraSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFBCC9D8),
    outline = SoltraBorderDark,
    outlineVariant = SoltraDividerDark,
    inverseSurface = Color(0xFFE5EDF6),
    inverseOnSurface = SoltraSecondary,
    error = SoltraErrorDark,
    onError = Color(0xFF690005),
)

private val ColorfulScheme = lightColorScheme(
    primary = SoltraPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCAEAE7),
    onPrimaryContainer = Color(0xFF093736),
    secondary = SoltraSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFDDEA),
    onSecondaryContainer = Color(0xFF0D1829),
    tertiary = SoltraAccent,
    onTertiary = Color(0xFF301C08),
    tertiaryContainer = Color(0xFFFFD9B2),
    onTertiaryContainer = Color(0xFF4A2A0E),
    background = Color(0xFFFBF6EE),
    onBackground = SoltraSecondary,
    surface = Color(0xFFFFFAF2),
    onSurface = SoltraSecondary,
    surfaceVariant = Color(0xFFE8E3D7),
    onSurfaceVariant = Color(0xFF415166),
    outline = SoltraBorderLight,
    outlineVariant = SoltraDividerLight,
    error = SoltraErrorLight,
    onError = Color.White,
)

@Immutable
data class SoltraExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
    val info: Color,
    val onInfo: Color,
    val border: Color,
    val divider: Color,
    val focus: Color,
    val chartDefaults: List<Color>,
)

val LocalSoltraExtendedColors = staticCompositionLocalOf {
    SoltraExtendedColors(
        success = SoltraSuccessLight,
        onSuccess = Color.White,
        warning = SoltraWarningLight,
        onWarning = Color.White,
        info = SoltraInfoLight,
        onInfo = Color.White,
        border = SoltraBorderLight,
        divider = SoltraDividerLight,
        focus = SoltraFocusLight,
        chartDefaults = SoltraChartPalette,
    )
}

val MaterialTheme.soltra: SoltraExtendedColors
    @Composable
    get() = LocalSoltraExtendedColors.current

private val DisplayFont = FontFamily(
    Font(R.font.sora, FontWeight.Normal),
    Font(R.font.sora, FontWeight.Medium),
    Font(R.font.sora, FontWeight.SemiBold),
    Font(R.font.sora, FontWeight.Bold),
)

private val TextFont = FontFamily(
    Font(R.font.manrope, FontWeight.Normal),
    Font(R.font.manrope, FontWeight.Medium),
    Font(R.font.manrope, FontWeight.SemiBold),
    Font(R.font.manrope, FontWeight.Bold),
)

private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 56.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.8).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 44.sp,
        lineHeight = 52.sp,
        letterSpacing = (-0.5).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.35).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.3).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.15).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 23.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.02.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = DisplayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.04.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = TextFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 23.sp,
        letterSpacing = 0.2.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = TextFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = TextFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.25.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = TextFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = TextFont,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = TextFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.3.sp,
    ),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp),
)

private fun extendedColors(colorScheme: ColorScheme, dark: Boolean): SoltraExtendedColors {
    return if (dark) {
        SoltraExtendedColors(
            success = SoltraSuccessDark,
            onSuccess = Color(0xFF0E2F1E),
            warning = SoltraWarningDark,
            onWarning = Color(0xFF4A2A0E),
            info = SoltraInfoDark,
            onInfo = Color(0xFF0A2544),
            border = SoltraBorderDark,
            divider = SoltraDividerDark,
            focus = SoltraFocusDark,
            chartDefaults = SoltraChartPalette,
        )
    } else {
        SoltraExtendedColors(
            success = SoltraSuccessLight,
            onSuccess = Color.White,
            warning = SoltraWarningLight,
            onWarning = Color.White,
            info = SoltraInfoLight,
            onInfo = Color.White,
            border = SoltraBorderLight,
            divider = SoltraDividerLight,
            focus = SoltraFocusLight,
            chartDefaults = SoltraChartPalette,
        )
    }
}

@Composable
fun BudgetCompanionTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val useDarkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT, AppThemeMode.COLORFUL -> false
    }
    val scheme = when {
        themeMode == AppThemeMode.COLORFUL -> ColorfulScheme
        useDarkTheme -> DarkScheme
        else -> LightScheme
    }

    CompositionLocalProvider(
        LocalSoltraExtendedColors provides extendedColors(
            colorScheme = scheme,
            dark = useDarkTheme,
        ),
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
