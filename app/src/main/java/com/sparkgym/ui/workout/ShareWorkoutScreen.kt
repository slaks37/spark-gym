package com.sparkgym.ui.workout

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.sparkgym.core.design.SparkColors
import com.sparkgym.core.design.SystemButton
import com.sparkgym.core.design.SystemLabel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareWorkoutScreen(
    viewModel: SessionViewModel,
    sessionId: Long,
    onBack: () -> Unit
) {
    LaunchedEffect(sessionId) {
        viewModel.bind(sessionId)
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val session = state.session

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Share Workout", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SparkColors.Void,
                    titleContentColor = SparkColors.TextPrimary,
                    navigationIconContentColor = SparkColors.TextPrimary
                )
            )
        },
        containerColor = SparkColors.Void
    ) { padding ->
        val currentSession = session
        if (currentSession == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SparkColors.Cyan)
            }
        } else {
            val graphicsLayer = rememberGraphicsLayer()
            val coroutineScope = rememberCoroutineScope()
            val context = LocalContext.current

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // The Instagram-style card that will be captured
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.8f) // Vertical ratio, good for stories
                        .clip(RoundedCornerShape(24.dp))
                        .background(SparkColors.TextPrimary) // Dark background for the card
                        .drawWithContent {
                            graphicsLayer.record {
                                this@drawWithContent.drawContent()
                            }
                            drawLayer(graphicsLayer)
                        }
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Header
                        Column {
                            Text(
                                text = "WORKOUT COMPLETE",
                                style = SystemLabel,
                                color = SparkColors.Cyan
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = currentSession.name,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SparkColors.Void
                                )
                            )
                        }

                        // Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val personalRecords = state.blocks.sumOf { block -> block.sets.count { it.isPersonalRecord } }
                            StatBox("Volume", "${state.volumeKg.toInt()} kg", SparkColors.Amber)
                            StatBox("Sets", "${state.completedSets}", SparkColors.Violet)
                            StatBox("PRs", "$personalRecords", SparkColors.Success)
                        }

                        // Branding
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚡ Spark Gym",
                                style = MaterialTheme.typography.titleMedium,
                                color = SparkColors.TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                SystemButton(
                    text = "Share to Story",
                    onClick = {
                        coroutineScope.launch {
                            val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                            shareBitmap(context, bitmap)
                        }
                    },
                    icon = Icons.Filled.Share,
                    modifier = Modifier.fillMaxWidth(),
                    accent = SparkColors.Cyan
                )
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, style = MaterialTheme.typography.headlineSmall, color = color, fontWeight = FontWeight.Bold)
        Text(text = label, style = SystemLabel, color = SparkColors.TextMuted)
    }
}

private fun shareBitmap(context: Context, bitmap: Bitmap) {
    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "workout_summary_${System.currentTimeMillis()}.jpg")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/SparkGym")
    }

    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    if (uri != null) {
        var stream: OutputStream? = null
        try {
            stream = resolver.openOutputStream(uri)
            if (stream != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            }
        } finally {
            stream?.close()
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Workout Summary"))
    }
}
