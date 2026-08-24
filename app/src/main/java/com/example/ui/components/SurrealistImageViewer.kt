package com.example.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.SleekCardBorder
import com.example.ui.theme.SleekDarkCard
import com.example.ui.theme.SleekDarkCardAlt
import com.example.ui.theme.SleekLilac
import com.example.ui.theme.SleekOnPrimaryContainer
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSecondary
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekWhite
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextLightMuted
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SurrealistImageViewer(
    imageUrl: String?,
    imagePrompt: String,
    imageSize: String, // "1K", "2K", "4K"
    isGenerating: Boolean,
    onRegenerate: ((size: String, prompt: String, style: String, aspectRatio: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showFullscreen by remember { mutableStateOf(false) }
    var showPromptDetails by remember { mutableStateOf(false) }
    var showStudioDialog by remember { mutableStateOf(false) }
    var selectedSize by remember(imageSize) { mutableStateOf(imageSize) }
    var selectedStyle by remember { mutableStateOf("Masterwork Surrealism") }
    var selectedAspectRatio by remember { mutableStateOf("1:1") }
    var customPromptText by remember(imagePrompt) { mutableStateOf(imagePrompt) }

    val decodedBitmap = remember(imageUrl) {
        if (imageUrl != null && !imageUrl.startsWith("http")) {
            try {
                val cleanBase64 = if (imageUrl.contains(",")) {
                    imageUrl.substringAfter(",")
                } else {
                    imageUrl
                }
                val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, SleekCardBorder, RoundedCornerShape(24.dp)),
        color = SleekWhite,
        shadowElevation = 1.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .background(SleekDarkCard),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isGenerating -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            CircularProgressIndicator(
                                color = SleekLilac,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Painting Surrealist Vision...",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = SleekLilac
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "AI Dream Synthesis at $selectedSize ($selectedStyle)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextLightMuted
                            )
                        }
                    }

                    decodedBitmap != null -> {
                        Image(
                            bitmap = decodedBitmap,
                            contentDescription = "Surrealist Dream Artwork",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { showFullscreen = true }
                        )
                    }

                    !imageUrl.isNullOrBlank() -> {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Surrealist Dream Artwork",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { showFullscreen = true }
                        )
                    }

                    else -> {
                        // Empty / placeholder state
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SleekDarkCardAlt)
                                .padding(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(SleekLilac.copy(alpha = 0.2f), CircleShape)
                                    .border(1.dp, SleekLilac.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = SleekLilac,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Surrealist Subconscious Art",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextLight
                            )
                            Text(
                                text = "Generate with Gemini image models in 1K, 2K, or 4K",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextLightMuted
                            )
                        }
                    }
                }

                // Top Badge Overlays: Resolution & Fullscreen
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Size affordance badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x99000000))
                            .border(1.dp, Color(0x4DFFFFFF), RoundedCornerShape(8.dp))
                            .clickable { showStudioDialog = true }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = selectedSize,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = SleekLilac
                        )
                    }

                    if (decodedBitmap != null || !imageUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0x99000000))
                                .clickable { showFullscreen = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "View Fullscreen",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Bottom controls & affordance bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SleekWhite)
                    .padding(14.dp)
            ) {
                // Resolution Selector bar & Studio trigger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Resolution:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = TextSecondary
                        )
                        listOf("1K", "2K", "4K").forEach { sizeOpt ->
                            val isSelected = selectedSize == sizeOpt
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) SleekPrimary else SleekSurfaceVariant
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) SleekPrimary else SleekCardBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedSize = sizeOpt
                                        onRegenerate?.invoke(sizeOpt, customPromptText.ifBlank { imagePrompt }, selectedStyle, selectedAspectRatio)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = sizeOpt,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) SleekWhite else TextSecondary
                                )
                            }
                        }
                    }

                    if (onRegenerate != null) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SleekPrimaryContainer)
                                .clickable { showStudioDialog = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("open_visual_studio_button"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Studio",
                                tint = SleekPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (imageUrl.isNullOrBlank()) "Generate" else "Visual Studio",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SleekOnPrimaryContainer
                                )
                            )
                        }
                    }
                }

                if (imagePrompt.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPromptDetails = !showPromptDetails },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Surrealist Prompt Vision",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SleekPrimary
                            )
                        )
                        Text(
                            text = if (showPromptDetails) "Hide" else "Show",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = TextSecondary
                        )
                    }

                    AnimatedVisibility(visible = showPromptDetails) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                                .background(SleekSurfaceVariant, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = imagePrompt,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = TextPrimary,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Surrealist Visual Studio Dialog (Custom Prompt, Style & Aspect Ratio)
    if (showStudioDialog && onRegenerate != null) {
        Dialog(
            onDismissRequest = { showStudioDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(24.dp)),
                color = SleekWhite
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SleekPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Surrealist Vision Studio",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }

                        IconButton(onClick = { showStudioDialog = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Surrealist Aesthetic Movement",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val styles = listOf(
                        "Masterwork Surrealism" to "Dali & Magritte Classic",
                        "Dalí Metamorphic" to "Melting Clocks & Deserts",
                        "Magritte Paradox" to "Impossible Day Skies",
                        "Chirico Metaphysical" to "Arcades & Deep Shadows",
                        "Varo Alchemical" to "Occult Towers & Starlight",
                        "Carrington Mythic" to "Folkloric Chimeras"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        styles.chunked(2).forEach { rowStyles ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowStyles.forEach { (styleKey, styleSub) ->
                                    val isSelected = selectedStyle == styleKey
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) SleekPrimary else SleekSurfaceVariant)
                                            .border(
                                                1.dp,
                                                if (isSelected) SleekPrimary else SleekCardBorder,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable { selectedStyle = styleKey }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = styleKey,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 11.sp
                                                ),
                                                color = if (isSelected) SleekWhite else TextPrimary
                                            )
                                            Text(
                                                text = styleSub,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = if (isSelected) SleekLilac else TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Aspect Ratio & Resolution
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Aspect Ratio",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("1:1", "16:9", "4:3").forEach { ratio ->
                                    val isSelected = selectedAspectRatio == ratio
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) SleekPrimary else SleekSurfaceVariant)
                                            .border(
                                                1.dp,
                                                if (isSelected) SleekPrimary else SleekCardBorder,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedAspectRatio = ratio }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = ratio,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 11.sp
                                            ),
                                            color = if (isSelected) SleekWhite else TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        Column {
                            Text(
                                text = "Resolution",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("1K", "2K", "4K").forEach { size ->
                                    val isSelected = selectedSize == size
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) SleekPrimaryContainer else SleekSurfaceVariant)
                                            .border(
                                                1.dp,
                                                if (isSelected) SleekPrimary else SleekCardBorder,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedSize = size }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = size,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                                fontSize = 11.sp
                                            ),
                                            color = if (isSelected) SleekOnPrimaryContainer else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Custom Dream Vision Prompt",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customPromptText,
                        onValueChange = { customPromptText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp)
                            .testTag("custom_image_prompt_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekPrimary,
                            unfocusedBorderColor = SleekCardBorder,
                            focusedContainerColor = SleekSurfaceVariant,
                            unfocusedContainerColor = SleekSurfaceVariant
                        ),
                        textStyle = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            showStudioDialog = false
                            val promptToUse = customPromptText.ifBlank { imagePrompt }
                            onRegenerate(selectedSize, promptToUse, selectedStyle, selectedAspectRatio)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("generate_visual_submit_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SleekPrimary,
                            contentColor = SleekWhite
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Paint Surrealist Artwork ($selectedSize)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }

    // Fullscreen Image Lightbox Dialog
    if (showFullscreen && (decodedBitmap != null || !imageUrl.isNullOrBlank())) {
        Dialog(
            onDismissRequest = { showFullscreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xF51D1B20))
            ) {
                if (decodedBitmap != null) {
                    Image(
                        bitmap = decodedBitmap,
                        contentDescription = "Surrealist Dream Art Fullscreen",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Surrealist Dream Art Fullscreen",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Top bar with close and resolution tag
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 20.dp, vertical = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xCC000000))
                            .border(1.dp, SleekLilac, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Surrealist Dreamscape ($selectedSize)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = SleekLilac
                        )
                    }

                    IconButton(
                        onClick = { showFullscreen = false },
                        modifier = Modifier
                            .background(Color(0xCC000000), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

