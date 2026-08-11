package com.fypnetwork.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * Runs [action] every time this composable's screen resumes - including the
 * very first time it appears, and again whenever the user navigates back to
 * it (e.g. after creating a post from the Feed, or a task from a Group).
 *
 * This is what makes "create X, go back, see the new X in the list" work
 * without a full shared-ViewModel/event-bus setup: each screen just refetches
 * on resume instead of relying on the previous screen to somehow push an
 * update into it.
 */
@Composable
fun OnResume(action: () -> Unit) {
    val currentAction by rememberUpdatedState(action)
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                currentAction()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
