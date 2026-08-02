package com.sparkgym.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sparkgym.core.design.SparkColors
import com.sparkgym.core.design.SparkDimens
import com.sparkgym.core.design.SystemButton
import com.sparkgym.core.design.SystemLabel
import com.sparkgym.core.util.Dates
import com.sparkgym.core.util.ImageUtils
import kotlinx.coroutines.launch

@Composable
fun ProgressPhotosScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = ImageUtils.saveAvatar(context, uri) // Reuse saveAvatar or create savePhoto
                if (path != null) {
                    viewModel.addProgressPhoto(
                        uri = path,
                        weightKg = profile.weightKg,
                        dateEpochDay = java.time.LocalDate.now().toEpochDay()
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(SparkColors.Void)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = SparkColors.TextSecondary)
            }
            Text("PROGRESS PHOTOS", style = SystemLabel.copy(color = SparkColors.Cyan))
            Spacer(Modifier.weight(1f))
            IconButton(onClick = {
                pickerLauncher.launch(
                    androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }) {
                Icon(Icons.Filled.Add, "Add Photo", tint = SparkColors.Cyan)
            }
        }

        if (photos.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No photos yet.\nTap + to add one.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SparkColors.TextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = SparkDimens.screen,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(photos, key = { it.id }) { photo ->
                    ProgressPhotoItem(
                        photo = photo,
                        onDelete = { viewModel.deleteProgressPhoto(photo.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressPhotoItem(
    photo: com.sparkgym.data.local.ProgressPhotoEntity,
    onDelete: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val bitmap = remember(photo.imageUri) {
        val file = java.io.File(photo.imageUri)
        if (file.exists()) {
            android.graphics.BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
        } else null
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Delete Photo?") },
            text = { Text("Are you sure you want to delete this progress photo?") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    onDelete()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = SparkColors.Panel
        )
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.medium)
            .background(SparkColors.Panel)
            .clickable { showDialog = true } // Long click better? For now, click to delete
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Progress Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Missing", style = MaterialTheme.typography.labelSmall, color = SparkColors.TextMuted)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f))
                .padding(4.dp)
        ) {
            val date = java.time.LocalDate.ofEpochDay(photo.dateEpochDay)
            val dateString = date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd"))
            Text(
                "$dateString • ${photo.weightKg} kg",
                style = MaterialTheme.typography.labelSmall,
                color = androidx.compose.ui.graphics.Color.White
            )
        }
    }
}
