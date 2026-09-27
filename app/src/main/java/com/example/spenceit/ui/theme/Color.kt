package com.example.spenceit.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * SpenceIt UI Color System
 *
 * Clean, simple finance-app design.
 * These colors are presentation-only and do not affect app functionality.
 */

// ----------------------------------------------------
// BRAND
// ----------------------------------------------------

val SpencePrimary = Color(0xFF4F63E9)
val SpencePrimaryDark = Color(0xFF3949C6)
val SpencePrimaryLight = Color(0xFFEEF1FF)
val SpencePrimaryContainer = Color(0xFFE8ECFF)


// ----------------------------------------------------
// BACKGROUNDS
// ----------------------------------------------------

val SpenceBackground = Color(0xFFF8F9FD)
val SpenceSurface = Color(0xFFFFFFFF)
val SpenceSurfaceVariant = Color(0xFFF2F4FA)


// ----------------------------------------------------
// TEXT
// ----------------------------------------------------

val SpenceTextPrimary = Color(0xFF171A2C)
val SpenceTextSecondary = Color(0xFF70758F)
val SpenceTextMuted = Color(0xFF9A9EB2)


// ----------------------------------------------------
// BORDERS
// ----------------------------------------------------

val SpenceBorder = Color(0xFFE2E5EF)
val SpenceDivider = Color(0xFFEAECF3)


// ----------------------------------------------------
// SUCCESS
// ----------------------------------------------------

val SpenceSuccess = Color(0xFF16876B)
val SpenceSuccessContainer = Color(0xFFE7F6F1)


// ----------------------------------------------------
// ERROR / MONEY OWED
// ----------------------------------------------------

val SpenceError = Color(0xFFD64040)
val SpenceErrorDark = Color(0xFFB3261E)
val SpenceErrorContainer = Color(0xFFFFEBEA)


// ----------------------------------------------------
// WARNING
// ----------------------------------------------------

val SpenceWarning = Color(0xFFF0A33A)
val SpenceWarningContainer = Color(0xFFFFF4DF)


// ----------------------------------------------------
// DASHBOARD BALANCE CARDS
// ----------------------------------------------------

val OwedCardBackground = Color(0xFFEAF0FF)
val OwedCardAccent = Color(0xFF4164D9)

val OweCardBackground = Color(0xFFFFEDEC)
val OweCardAccent = Color(0xFFD53D3D)


// ----------------------------------------------------
// DARK THEME
// ----------------------------------------------------

val SpenceDarkBackground = Color(0xFF111318)
val SpenceDarkSurface = Color(0xFF191C23)
val SpenceDarkSurfaceVariant = Color(0xFF222630)

val SpenceDarkTextPrimary = Color(0xFFF3F4FA)
val SpenceDarkTextSecondary = Color(0xFFB9BDCC)

val SpenceDarkPrimary = Color(0xFFAEB8FF)


// ----------------------------------------------------
// TEMPORARY COMPATIBILITY COLORS
//
// Keeping these prevents anything that still references
// the old default Android Studio color names from breaking.
// We can remove them later after the redesign is complete.
// ----------------------------------------------------

val Purple80 = SpenceDarkPrimary
val PurpleGrey80 = Color(0xFFCACCE0)
val Pink80 = Color(0xFFFFB3B7)

val Purple40 = SpencePrimary
val PurpleGrey40 = SpenceTextSecondary
val Pink40 = SpenceError