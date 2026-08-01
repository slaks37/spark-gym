package com.sparkgym

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.sparkgym.core.design.SparkGymTheme
import com.sparkgym.core.util.LocalAppLanguage
import com.sparkgym.data.prefs.UserProfile
import com.sparkgym.ui.navigation.SparkNavHost
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val container get() = (application as SparkGymApp).container

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Hold the splash until we know whether to show onboarding, so the user
        // never sees the dashboard flash before being redirected.
        var onboarded: Boolean? = null
        splash.setKeepOnScreenCondition { onboarded == null }

        lifecycleScope.launch {
            onboarded = container.prefs.profile.first().onboarded
            setContent {
                val profile by container.prefs.profile.collectAsState(initial = UserProfile())
                CompositionLocalProvider(LocalAppLanguage provides profile.language) {
                    SparkGymTheme {
                        SparkNavHost(
                            container = container,
                            startOnboarding = onboarded != true
                        )
                    }
                }
            }
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    /** Catches the Fitbit OAuth redirect and hands it to whoever is listening. */
    private fun handleIntent(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == BuildConfig.FITBIT_REDIRECT_SCHEME && data.host == "fitbit-callback") {
            container.pendingFitbitRedirect.value = data
        }
    }
}
