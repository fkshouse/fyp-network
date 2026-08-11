package com.fypnetwork.ui.theme

import androidx.compose.ui.graphics.Color

// Bold/vibrant palette - an electric violet -> hot pink -> sunset orange
// range, used as the emotional core of the redesign (buttons, active states,
// the gradient header treatments, like/heart accents). Deliberately more
// saturated and higher-contrast than Material's tonal defaults.
val Violet = Color(0xFF7C3AED)
val VioletDark = Color(0xFF5B21B6)
val Magenta = Color(0xFFE0289D)
val HotPink = Color(0xFFEC4899)
val SunsetOrange = Color(0xFFF97316)
val ElectricBlue = Color(0xFF3B82F6)

val LikeRed = Color(0xFFF43F5E)
val SuccessGreen = Color(0xFF10B981)
val WarningAmber = Color(0xFFF59E0B)

val BrandPrimary = Violet
val BrandPrimaryContainer = Color(0xFFF3E8FF)
val BrandSecondary = HotPink
val BrandSecondaryContainer = Color(0xFFFCE7F3)
val BrandTertiary = SunsetOrange

val BrandBackground = Color(0xFFFAFAFC)
val BrandSurface = Color(0xFFFFFFFF)
val BrandSurfaceVariant = Color(0xFFF4F0FA)
val BrandError = Color(0xFFDC2626)

val BrandBackgroundDark = Color(0xFF141019)
val BrandSurfaceDark = Color(0xFF1F1A29)
val BrandSurfaceVariantDark = Color(0xFF2A2338)

// Named, reusable gradients rather than one-off Brush definitions scattered
// through screens - anywhere the app wants "the brand gradient" it pulls
// from here, so the palette can change in one place.
object BrandGradients {
    val heroColors = listOf(Violet, Magenta, SunsetOrange)
    val primaryColors = listOf(Violet, HotPink)
    val subtleColors = listOf(BrandPrimaryContainer, BrandSecondaryContainer)
}
