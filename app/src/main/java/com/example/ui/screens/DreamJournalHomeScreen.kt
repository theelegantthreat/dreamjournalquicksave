package com.example.ui.screens

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.DreamEntry
import com.example.data.model.PsychologicalInterpretation
import com.example.ui.theme.SleekCanvas
import com.example.ui.theme.SleekCardBorder
import com.example.ui.theme.SleekCardBorderLight
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
import com.example.ui.viewmodel.DreamViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DreamJournalHomeScreen(
    viewModel: DreamViewModel,
    onNavigateToRecord: () -> Unit,
    onSelectDream: (Long) -> Unit
) {
    val dreams by viewModel.allDreams.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Favorites", "Archetypes"
    var currentNavTab by remember { mutableStateOf("Journal") } // "Journal", "Patterns", "Settings"
    var isListView by remember { mutableStateOf(false) }

    val filteredDreams = remember(dreams, searchQuery, selectedFilter, currentNavTab) {
        dreams.filter { dream ->
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                dream.title.contains(searchQuery, ignoreCase = true) ||
                        dream.rawTranscription.contains(searchQuery, ignoreCase = true) ||
                        dream.emotionalTheme.contains(searchQuery, ignoreCase = true) ||
                        dream.wakingMood.contains(searchQuery, ignoreCase = true) ||
                        dream.symbolsJson.contains(searchQuery, ignoreCase = true) ||
                        dream.archetypesJson.contains(searchQuery, ignoreCase = true)
            }
            val matchesFilter = when {
                currentNavTab == "Patterns" -> dream.archetypesJson.length > 10
                selectedFilter == "Favorites" -> dream.isFavorite
                selectedFilter == "Archetypes" -> dream.archetypesJson.length > 10
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    // Stats calculations
    val totalDreams = dreams.size
    val favoriteCount = dreams.count { it.isFavorite }
    val totalArchetypesMet = remember(dreams) {
        dreams.sumOf { dream ->
            PsychologicalInterpretation.parseArchetypes(dream.archetypesJson).size
        }
    }

    val latestDream = dreams.firstOrNull()
    val latestLogText = remember(latestDream) {
        if (latestDream != null) {
            val sdf = SimpleDateFormat("MMMM d, hh:mm a", Locale.getDefault())
            "Logged: ${sdf.format(Date(latestDream.timestamp))}"
        } else {
            "Awakening AI Codex"
        }
    }

    Scaffold(
        containerColor = SleekCanvas,
        bottomBar = {
            // Sleek Interface Bottom Navigation
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SleekCanvas,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(SleekCardBorder.copy(alpha = 0.4f))
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Journal Tab
                        val isJournalActive = currentNavTab == "Journal"
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    currentNavTab = "Journal"
                                    selectedFilter = "All"
                                }
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoStories,
                                contentDescription = "Journal",
                                tint = if (isJournalActive) SleekPrimary else TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Journal",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = if (isJournalActive) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isJournalActive) SleekPrimary else TextSecondary
                            )
                        }

                        // Patterns Tab
                        val isPatternsActive = currentNavTab == "Patterns"
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    currentNavTab = "Patterns"
                                    selectedFilter = "Archetypes"
                                }
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = "Patterns",
                                tint = if (isPatternsActive) SleekPrimary else TextSecondary.copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Patterns",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = if (isPatternsActive) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isPatternsActive) SleekPrimary else TextSecondary.copy(alpha = 0.6f)
                            )
                        }

                        // Settings / Info Tab
                        val isSettingsActive = currentNavTab == "Settings"
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (dreams.isEmpty()) {
                                        viewModel.seedSampleDreams()
                                    }
                                    currentNavTab = "Settings"
                                }
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = if (isSettingsActive) SleekPrimary else TextSecondary.copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Settings",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = if (isSettingsActive) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSettingsActive) SleekPrimary else TextSecondary.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToRecord,
                containerColor = SleekPrimary,
                contentColor = SleekWhite,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 8.dp, end = 8.dp)
                    .size(60.dp)
                    .testTag("record_dream_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Record Awakening Dream",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Sleek Header Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Sleek Interface Top Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(SleekPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = SleekWhite,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Dream Oracle",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 19.sp
                                    ),
                                    color = TextPrimary
                                )
                                Text(
                                    text = latestLogText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    color = TextSecondary
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (dreams.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(SleekPrimaryContainer)
                                        .clickable { viewModel.seedSampleDreams() }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = SleekOnPrimaryContainer,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Sample Dreams",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = SleekOnPrimaryContainer
                                            )
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        if (dreams.isEmpty()) viewModel.seedSampleDreams()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sleek Fast Voice Input / Ask Bar
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .clickable { onNavigateToRecord() },
                        color = SleekPrimaryContainer,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp)
                                .padding(end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(SleekPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Record",
                                    tint = SleekWhite,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Record waking dream or ask subconscious...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                ),
                                color = TextSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SleekPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Statistics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            label = "Dreams Logged",
                            value = "$totalDreams",
                            icon = Icons.Default.NightlightRound,
                            accentColor = SleekPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Archetypes Met",
                            value = "$totalArchetypesMet",
                            icon = Icons.Default.Psychology,
                            accentColor = SleekSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Starred Dreams",
                            value = "$favoriteCount",
                            icon = Icons.Default.Star,
                            accentColor = SleekPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sleek Search Input Box
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search symbols, archetypes, or themes...", color = TextMuted, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TextSecondary
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dream_search_field"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekPrimary,
                            unfocusedBorderColor = SleekCardBorder,
                            focusedContainerColor = SleekWhite,
                            unfocusedContainerColor = SleekWhite,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter Chips and View Mode Toggle Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("All", "Favorites", "Archetypes").forEach { filter ->
                                val isSelected = selectedFilter == filter
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            if (isSelected) SleekPrimary else SleekWhite
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) SleekPrimary else SleekCardBorder,
                                            RoundedCornerShape(20.dp)
                                        )
                                        .clickable { selectedFilter = filter }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = filter,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) SleekWhite else TextSecondary
                                    )
                                }
                            }
                        }

                        // View Layout Mode Toggle (Cards vs List)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(SleekWhite)
                                .border(1.dp, SleekCardBorder, RoundedCornerShape(18.dp))
                                .padding(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (!isListView) SleekPrimary else Color.Transparent)
                                    .clickable { isListView = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ViewAgenda,
                                    contentDescription = "Card View",
                                    tint = if (!isListView) SleekWhite else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isListView) SleekPrimary else Color.Transparent)
                                    .clickable { isListView = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ViewList,
                                    contentDescription = "List View",
                                    tint = if (isListView) SleekWhite else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Dream Cards Stream
            if (filteredDreams.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(SleekPrimaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NightlightRound,
                                contentDescription = null,
                                tint = SleekPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No dreams match '$searchQuery'" else "Your dream codex is waiting",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Try another symbol or clear search" else "Record your first awakening dream to generate surrealist art & Jungian analysis",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (dreams.isEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(SleekPrimary)
                                    .clickable { viewModel.seedSampleDreams() }
                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "Load Sample Surrealist Dreams",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = SleekWhite
                                )
                            }
                        }
                    }
                }
            } else {
                items(filteredDreams, key = { it.id }) { dream ->
                    if (isListView) {
                        DreamJournalCompactListItem(
                            dream = dream,
                            onClick = { onSelectDream(dream.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(dream.id) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                        )
                    } else {
                        DreamJournalCard(
                            dream = dream,
                            onClick = { onSelectDream(dream.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(dream.id) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, SleekCardBorder, RoundedCornerShape(18.dp)),
        color = SleekWhite,
        shadowElevation = 0.5.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    ),
                    color = accentColor
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = TextSecondary
            )
        }
    }
}

@Composable
fun DreamJournalCard(
    dream: DreamEntry,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("MMMM d, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(dream.timestamp) { dateFormatter.format(Date(dream.timestamp)) }
    val archetypes = remember(dream.archetypesJson) {
        PsychologicalInterpretation.parseArchetypes(dream.archetypesJson)
    }

    val decodedBitmap = remember(dream.imageUrl) {
        if (dream.imageUrl != null && !dream.imageUrl.startsWith("http")) {
            try {
                val clean = if (dream.imageUrl.contains(",")) dream.imageUrl.substringAfter(",") else dream.imageUrl
                val bytes = Base64.decode(clean, Base64.DEFAULT)
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
            .clip(RoundedCornerShape(28.dp))
            .border(1.dp, SleekCardBorder, RoundedCornerShape(28.dp))
            .clickable { onClick() }
            .testTag("dream_card_${dream.id}"),
        color = SleekWhite,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Image / Surrealist banner matching Sleek Interface spec
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SleekDarkCard),
                contentAlignment = Alignment.Center
            ) {
                if (decodedBitmap != null) {
                    Image(
                        bitmap = decodedBitmap,
                        contentDescription = dream.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (!dream.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(dream.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = dream.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SleekDarkCardAlt)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(SleekLilac.copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, SleekLilac.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SleekLilac,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Surrealist Synthesis Generated",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = TextLight
                        )
                        if (dream.imagePrompt.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "'${dream.imagePrompt}'",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                color = TextLightMuted,
                                maxLines = 2,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Dark gradient overlay for bottom text clarity
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0x99000000))
                            )
                        )
                )

                // Top right favorite button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .background(Color(0x80000000), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = if (dream.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (dream.isFavorite) Color(0xFFFF5252) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Core emotional theme and waking mood badge at bottom left
                val coreTheme = if (dream.emotionalTheme.isNotBlank()) dream.emotionalTheme else "Subconscious Flow"
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dream.wakingMood.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(SleekPrimary.copy(alpha = 0.9f))
                                .border(1.dp, Color(0x4DFFFFFF), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "MOOD: ${dream.wakingMood.uppercase()}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.8.sp
                                ),
                                color = Color.White
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x33FFFFFF))
                            .border(1.dp, Color(0x4DFFFFFF), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "CORE: ${coreTheme.uppercase()}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Jungian Analysis section inside card
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = SleekPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "JUNGIAN ANALYSIS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = SleekPrimary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Archetype highlight box with left accent border
            val topArchetype = archetypes.firstOrNull()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SleekSurfaceVariant)
                    .padding(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Left accent border bar
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(44.dp)
                            .background(SleekPrimary, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "The Archetype: ${topArchetype?.name ?: dream.title}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = topArchetype?.manifestation ?: (if (dream.psychologicalAnalysis.isNotBlank()) dream.psychologicalAnalysis else dream.rawTranscription),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = TextSecondary,
                            maxLines = 2,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Transcribed Note excerpt
            Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(
                    text = "Transcribed Note:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    ),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "\"${dream.rawTranscription}\"",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    color = TextPrimary.copy(alpha = 0.85f),
                    maxLines = 2,
                    lineHeight = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom action hint: Ask Psychologist
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SleekPrimaryContainer.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = SleekPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ask Psychologist about dream symbols",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = SleekOnPrimaryContainer
                    )
                }

                Text(
                    text = "Explore →",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = SleekPrimary
                )
            }
        }
    }
}

@Composable
fun DreamJournalCompactListItem(
    dream: DreamEntry,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }
    val formattedDate = remember(dream.timestamp) { dateFormatter.format(Date(dream.timestamp)) }
    val archetypes = remember(dream.archetypesJson) {
        PsychologicalInterpretation.parseArchetypes(dream.archetypesJson)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, SleekCardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("dream_compact_item_${dream.id}"),
        color = SleekWhite,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail / Icon box
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SleekSurfaceVariant)
                    .border(1.dp, SleekCardBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!dream.imageUrl.isNullOrBlank()) {
                    val bitmap = remember(dream.imageUrl) {
                        try {
                            val decoded = Base64.decode(dream.imageUrl, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Dream Surrealist Thumbnail",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = SleekPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.NightlightRound,
                        contentDescription = null,
                        tint = SleekPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = TextSecondary
                    )

                    if (dream.imageSize.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SleekPrimaryContainer.copy(alpha = 0.5f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = dream.imageSize,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekPrimary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = dream.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = TextPrimary,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "\"${dream.rawTranscription}\"",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    color = TextSecondary,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (dream.wakingMood.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SleekPrimary)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = dream.wakingMood,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekWhite
                                ),
                                maxLines = 1
                            )
                        }
                    }

                    // Emotional theme tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SleekPrimaryContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = dream.emotionalTheme,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekOnPrimaryContainer
                            ),
                            maxLines = 1
                        )
                    }

                    if (archetypes.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SleekSurfaceVariant)
                                .border(1.dp, SleekCardBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${archetypes.size} archetypes",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Favorite star button
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (dream.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (dream.isFavorite) Color(0xFFBA1A1A) else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}


