package com.sparkgym.ui.connect

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sparkgym.core.design.SparkColors
import com.sparkgym.core.design.SystemButton
import com.sparkgym.core.design.SystemChip
import com.sparkgym.core.design.SystemLabel
import com.sparkgym.core.design.SystemPanel
import com.sparkgym.core.util.Dates
import com.sparkgym.data.prefs.WearableSource
import com.sparkgym.data.remote.HealthConnectSource
import com.sparkgym.data.repository.WearableRepository
import com.sparkgym.di.AppContainer

@Composable
fun ConnectScreen(
    viewModel: ConnectViewModel,
    container: AppContainer,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val days by viewModel.recentDays.collectAsStateWithLifecycle()
    val pendingRedirect by container.pendingFitbitRedirect.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // The OAuth redirect is delivered to the activity; pick it up here exactly once.
    LaunchedEffect(pendingRedirect) {
        pendingRedirect?.let {
            viewModel.handleRedirect(it)
            container.pendingFitbitRedirect.value = null
        }
    }

    val healthPermissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        if (granted.containsAll(viewModel.healthConnectPermissions.take(2).toSet())) {
            viewModel.onHealthConnectGranted()
        } else {
            viewModel.refresh()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SparkColors.Void),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = SparkColors.TextSecondary)
                }
                Column {
                    Text("CONNECTIONS", style = SystemLabel.copy(color = SparkColors.Cyan))
                    Text(
                        "Wearables and health data",
                        style = MaterialTheme.typography.titleMedium,
                        color = SparkColors.TextPrimary
                    )
                }
            }
        }

        // ---------------------------------------------------- Health Connect
        item {
            val available = state.healthConnectAvailability
            SystemPanel(
                title = "Health Connect",
                accent = if (state.healthConnectGranted) SparkColors.Success else SparkColors.Cyan,
                trailing = {
                    SystemChip(
                        if (state.healthConnectGranted) "connected" else available.name.lowercase().replace('_', ' '),
                        accent = if (state.healthConnectGranted) SparkColors.Success else SparkColors.TextMuted,
                        filled = state.healthConnectGranted
                    )
                }
            ) {
                Text(
                    "The recommended route. The Fitbit app writes your steps, heart rate, sleep and " +
                        "calories into Health Connect, and Spark Gym reads them from there — no account " +
                        "linking, no tokens, and it works with Samsung Health and Wear OS too.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SparkColors.TextSecondary
                )
                Spacer(Modifier.height(12.dp))
                when (available) {
                    HealthConnectSource.Availability.AVAILABLE -> {
                        if (state.healthConnectGranted) {
                            SystemButton(
                                "Sync now",
                                { viewModel.sync() },
                                accent = SparkColors.Success,
                                enabled = !state.syncing,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            SystemButton(
                                "Grant access",
                                { healthPermissionLauncher.launch(viewModel.healthConnectPermissions) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    HealthConnectSource.Availability.UPDATE_REQUIRED -> Text(
                        "Health Connect needs updating in the Play Store before it can be used.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SparkColors.Amber
                    )
                    HealthConnectSource.Availability.NOT_SUPPORTED -> Text(
                        "Health Connect is not available on this device. Use the direct Fitbit link below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SparkColors.TextMuted
                    )
                }
            }
        }

        // ------------------------------------------------------------ Fitbit
        item {
            SystemPanel(
                title = "Fitbit account",
                accent = if (state.fitbitLinked) SparkColors.Success else SparkColors.Violet,
                trailing = {
                    SystemChip(
                        if (state.fitbitLinked) "linked" else if (state.fitbitConfigured) "not linked" else "not configured",
                        accent = if (state.fitbitLinked) SparkColors.Success else SparkColors.TextMuted,
                        filled = state.fitbitLinked
                    )
                }
            ) {
                Text(
                    "Signs in to Fitbit directly with OAuth 2.0 and PKCE, and pulls the same daily " +
                        "summary the Fitbit app shows. Use this if Health Connect is unavailable.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SparkColors.TextSecondary
                )
                Spacer(Modifier.height(12.dp))
                if (!state.fitbitConfigured) {
                    Text(
                        "No client id is compiled into this build. Register an app at dev.fitbit.com, " +
                            "set the redirect URI to sparkgym://fitbit-callback, then add " +
                            "fitbit.clientId=… to local.properties and rebuild.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SparkColors.Amber
                    )
                } else if (state.fitbitLinked) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SystemButton(
                            "Sync now",
                            { viewModel.sync() },
                            accent = SparkColors.Success,
                            enabled = !state.syncing,
                            modifier = Modifier.weight(1f)
                        )
                        SystemButton(
                            "Unlink",
                            { viewModel.unlinkFitbit() },
                            accent = SparkColors.Danger,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    SystemButton(
                        "Link Fitbit account",
                        { viewModel.linkFitbit(context) },
                        accent = SparkColors.Violet,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // ------------------------------------------------------ source pick
        if (state.fitbitLinked && state.healthConnectGranted) {
            item {
                SystemPanel(title = "Preferred source") {
                    Text(
                        "Both sources are available. Pick which one wins when they disagree.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SparkColors.TextSecondary
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SystemButton(
                            "Health Connect",
                            { viewModel.preferSource(WearableSource.HEALTH_CONNECT) },
                            accent = if (state.preferredSource == WearableSource.HEALTH_CONNECT)
                                SparkColors.Cyan else SparkColors.TextMuted,
                            modifier = Modifier.weight(1f)
                        )
                        SystemButton(
                            "Fitbit",
                            { viewModel.preferSource(WearableSource.FITBIT) },
                            accent = if (state.preferredSource == WearableSource.FITBIT)
                                SparkColors.Violet else SparkColors.TextMuted,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        state.lastResult?.let { result ->
            item {
                SystemPanel(
                    accent = when (result.status) {
                        WearableRepository.SyncStatus.SUCCESS -> SparkColors.Success
                        WearableRepository.SyncStatus.FAILED -> SparkColors.Danger
                        else -> SparkColors.Amber
                    }
                ) {
                    Text(
                        when (result.status) {
                            WearableRepository.SyncStatus.SUCCESS ->
                                "Synced ${result.daysSynced} days from ${result.source.replace('_', ' ')}"
                            WearableRepository.SyncStatus.NOT_CONFIGURED -> "No source connected yet"
                            WearableRepository.SyncStatus.FAILED -> "Sync failed"
                            else -> "Idle"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = SparkColors.TextPrimary
                    )
                    result.message?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall, color = SparkColors.TextMuted)
                    }
                }
            }
        }

        if (days.isNotEmpty()) {
            item {
                Text(
                    "LAST 7 DAYS",
                    style = SystemLabel.copy(color = SparkColors.TextSecondary),
                    modifier = Modifier.height(20.dp)
                )
            }
            items(days.sortedByDescending { it.dateEpochDay }, key = { it.id }) { day ->
                SystemPanel(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                Dates.pretty(day.dateEpochDay),
                                style = MaterialTheme.typography.bodyMedium,
                                color = SparkColors.TextPrimary
                            )
                            Text(
                                day.source.replace('_', ' '),
                                style = SystemLabel,
                                color = SparkColors.TextMuted
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${day.steps} steps",
                                style = MaterialTheme.typography.bodySmall,
                                color = SparkColors.Cyan
                            )
                            Text(
                                "${day.activeCalories} kcal · ${day.sleepMinutes / 60}h ${day.sleepMinutes % 60}m",
                                style = MaterialTheme.typography.bodySmall,
                                color = SparkColors.TextMuted
                            )
                            day.restingHeartRate?.let {
                                Text(
                                    "$it bpm resting",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SparkColors.Danger
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                "Health data stays on the device. Nothing is uploaded anywhere except to Fitbit itself, " +
                    "and only if you link the account above.",
                style = MaterialTheme.typography.bodySmall,
                color = SparkColors.TextMuted,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
