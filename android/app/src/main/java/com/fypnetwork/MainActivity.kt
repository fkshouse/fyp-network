package com.fypnetwork

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.fypnetwork.data.repository.AuthRepository
import com.fypnetwork.ui.navigation.Destinations
import com.fypnetwork.ui.navigation.FypNavGraph
import com.fypnetwork.ui.theme.FypNetworkTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Field injection is the standard Hilt pattern for Activities/Fragments -
    // constructor injection isn't available since the Android framework
    // instantiates these classes, not Hilt directly.
    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FypNetworkTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot(authRepository)
                }
            }
        }
    }
}

@Composable
private fun AppRoot(authRepository: AuthRepository) {
    // null while we haven't checked DataStore yet, then true/false once known.
    val isLoggedIn by authRepository.isLoggedIn.collectAsState(initial = null)

    when (isLoggedIn) {
        null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        true -> FypNavGraph(startDestination = Destinations.FEED)
        false -> FypNavGraph(startDestination = Destinations.LOGIN)
    }
}
