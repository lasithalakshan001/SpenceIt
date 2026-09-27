package com.example.spenceit.ui.theme

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
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


// ----------------------------------------------------
// LIGHT THEME
// ----------------------------------------------------

private val LightColorScheme = lightColorScheme(

    // Brand
    primary = SpencePrimary,
    onPrimary = Color.White,

    primaryContainer = SpencePrimaryContainer,
    onPrimaryContainer = SpencePrimaryDark,

    // Secondary
    secondary = SpenceSuccess,
    onSecondary = Color.White,

    secondaryContainer = SpenceSuccessContainer,
    onSecondaryContainer = SpenceSuccess,

    // Tertiary
    tertiary = SpenceWarning,
    onTertiary = Color.White,

    tertiaryContainer = SpenceWarningContainer,
    onTertiaryContainer = SpenceTextPrimary,

    // Error
    error = SpenceError,
    onError = Color.White,

    errorContainer = SpenceErrorContainer,
    onErrorContainer = SpenceErrorDark,

    // Main surfaces
    background = SpenceBackground,
    onBackground = SpenceTextPrimary,

    surface = SpenceSurface,
    onSurface = SpenceTextPrimary,

    surfaceVariant = SpenceSurfaceVariant,
    onSurfaceVariant = SpenceTextSecondary,

    // Borders
    outline = SpenceBorder,
    outlineVariant = SpenceDivider,

    // Other
    inverseSurface = SpenceTextPrimary,
    inverseOnSurface = Color.White,
    inversePrimary = SpenceDarkPrimary,

    scrim = Color.Black
)


// ----------------------------------------------------
// DARK THEME
// ----------------------------------------------------

private val DarkColorScheme = darkColorScheme(

    // Brand
    primary = SpenceDarkPrimary,
    onPrimary = Color(0xFF20255C),

    primaryContainer = Color(0xFF30386F),
    onPrimaryContainer = Color(0xFFE0E4FF),

    // Secondary
    secondary = Color(0xFF7DD7BC),
    onSecondary = Color(0xFF00382D),

    secondaryContainer = Color(0xFF165846),
    onSecondaryContainer = Color(0xFFB1F2DE),

    // Tertiary
    tertiary = Color(0xFFFFC66E),
    onTertiary = Color(0xFF412D00),

    tertiaryContainer = Color(0xFF5D4300),
    onTertiaryContainer = Color(0xFFFFDDA8),

    // Error
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),

    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    // Main surfaces
    background = SpenceDarkBackground,
    onBackground = SpenceDarkTextPrimary,

    surface = SpenceDarkSurface,
    onSurface = SpenceDarkTextPrimary,

    surfaceVariant = SpenceDarkSurfaceVariant,
    onSurfaceVariant = SpenceDarkTextSecondary,

    // Borders
    outline = Color(0xFF8C909F),
    outlineVariant = Color(0xFF41444D),

    inverseSurface = SpenceDarkTextPrimary,
    inverseOnSurface = SpenceTextPrimary,
    inversePrimary = SpencePrimary,

    scrim = Color.Black
)


// ----------------------------------------------------
// APP THEME
// ----------------------------------------------------

@Composable
fun SpenceItTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),

    /*
     * Keep this false so SpenceIt always uses its own
     * brand colors instead of colors generated from
     * the user's Android wallpaper.
     */
    dynamicColor: Boolean = false,

    content: @Composable () -> Unit
) {

    val context = LocalContext.current

    val colorScheme = when {

        /*
         * Dynamic colors are still supported if we ever
         * manually enable them later.
         */
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        darkTheme -> DarkColorScheme

        else -> LightColorScheme
    }


    /*
     * Make Android status/navigation bar icon colors
     * match the selected light/dark theme.
     */
    val view = LocalView.current

    if (!view.isInEditMode) {

        val window = (view.context as Activity).window

        WindowCompat.getInsetsController(
            window,
            view
        ).apply {

            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }


    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}