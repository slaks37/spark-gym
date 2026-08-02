package com.sparkgym.ui.profile

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.sparkgym.core.design.SparkColors
import com.sparkgym.core.design.SparkDimens
import com.sparkgym.core.design.SystemLabel
import com.sparkgym.core.design.SystemPanel
import com.sparkgym.core.util.S
import com.sparkgym.domain.model.Attributions

/**
 * Open-source notices.
 *
 * Required, not decorative: the anatomical model is CC BY-SA 4.0 and Open Food
 * Facts is ODbL, and both oblige the app to credit them *where the user can see
 * it*. A file in the repository does not discharge that — this screen does.
 */
@Composable
fun LicencesScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SparkColors.Void),
        contentPadding = SparkDimens.screen,
        verticalArrangement = Arrangement.spacedBy(SparkDimens.CardGap)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = S.back,
                        tint = SparkColors.TextSecondary
                    )
                }
                Text(
                    S.openSourceLicences,
                    style = MaterialTheme.typography.titleLarge,
                    color = SparkColors.TextPrimary
                )
            }
        }

        item {
            Text(
                S.licencesIntro,
                style = MaterialTheme.typography.bodyMedium,
                color = SparkColors.TextSecondary
            )
        }

        items(Attributions.all.size) { i ->
            val entry = Attributions.all[i]
            SystemPanel(accent = if (entry.shareAlike) SparkColors.Amber else SparkColors.Cyan) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        entry.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = SparkColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        entry.licence,
                        style = SystemLabel.copy(
                            color = if (entry.shareAlike) SparkColors.Amber else SparkColors.Cyan
                        )
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    entry.use,
                    style = MaterialTheme.typography.bodySmall,
                    color = SparkColors.TextSecondary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    entry.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = SparkColors.Cyan,
                    modifier = Modifier
                        .clickable {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, entry.url.toUri()))
                            }
                        }
                        .padding(vertical = 2.dp)
                )
            }
        }

        item {
            SystemPanel(accent = SparkColors.Amber, title = S.shareAlikeNotice) {
                Text(
                    S.shareAlikeBody,
                    style = MaterialTheme.typography.bodySmall,
                    color = SparkColors.TextSecondary
                )
            }
        }
    }
}
