package com.fypnetwork.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush

fun brandHeroBrush() = Brush.linearGradient(BrandGradients.heroColors)
fun brandPrimaryBrush() = Brush.horizontalGradient(BrandGradients.primaryColors)
fun brandSubtleBrush() = Brush.horizontalGradient(BrandGradients.subtleColors)

/** Applies the signature brand gradient as a background - the one visual
 *  thread meant to tie auth screens, profile headers, and empty states
 *  together into a single recognizable identity rather than every screen
 *  independently picking its own accent color. */
fun Modifier.brandGradientBackground(): Modifier = this.background(brandHeroBrush())

@Composable
fun GradientSurface(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Box(modifier = modifier.fillMaxWidth().brandGradientBackground()) {
        content()
    }
}
