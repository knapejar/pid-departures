package com.example.piddepartures.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.piddepartures.domain.model.Departure
import com.example.piddepartures.domain.model.StopWithDepartures
import com.example.piddepartures.domain.model.getRouteTypeIcon

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val stopsWithDepartures by viewModel.stopsWithDepartures.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val haptic = LocalHapticFeedback.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PID Departures") },
                actions = {
                    IconButton(onClick = { viewModel.refreshDepartures() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add stop")
            }
        }
    ) { padding ->
        if (stopsWithDepartures.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "No saved stops",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "Tap + to add your first stop",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Mutable local list for visual reordering
            val items = remember(stopsWithDepartures) { stopsWithDepartures.toMutableStateList() }
            var draggedItem by remember { mutableStateOf<StopWithDepartures?>(null) }
            var draggedOffset by remember { mutableStateOf(0f) }
            var initialIndex by remember { mutableStateOf<Int?>(null) }
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(
                    items, 
                    key = { _, item -> item.savedStop.id }
                ) { index, stopWithDepartures ->
                    val isDragging = draggedItem?.savedStop?.id == stopWithDepartures.savedStop.id
                    val offsetY = if (isDragging) draggedOffset else 0f
                    
                    StopCard(
                        stopWithDepartures = stopWithDepartures,
                        isRefreshing = isRefreshing,
                        isDragging = isDragging,
                        offsetY = offsetY,
                        onClick = { 
                            if (draggedItem == null) {
                                onNavigateToDetail(stopWithDepartures.savedStop.id)
                            }
                        },
                        onDragStart = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            draggedItem = stopWithDepartures
                            draggedOffset = 0f
                            initialIndex = index
                        },
                        onDrag = { _, dragAmount ->
                            draggedOffset += dragAmount.y
                            
                            // Determine if we should swap items
                            val currentIndex = items.indexOf(stopWithDepartures)
                            if (currentIndex != -1) {
                                // If dragged down more than half item height, swap with next
                                if (draggedOffset > 60f && currentIndex < items.size - 1) {
                                    val temp = items[currentIndex]
                                    items[currentIndex] = items[currentIndex + 1]
                                    items[currentIndex + 1] = temp
                                    draggedOffset = 0f
                                }
                                // If dragged up more than half item height, swap with previous
                                else if (draggedOffset < -60f && currentIndex > 0) {
                                    val temp = items[currentIndex]
                                    items[currentIndex] = items[currentIndex - 1]
                                    items[currentIndex - 1] = temp
                                    draggedOffset = 0f
                                }
                            }
                        },
                        onDragEnd = {
                            // Save ONLY when drag ends - find final position
                            draggedItem?.let { dragged ->
                                initialIndex?.let { from ->
                                    val to = items.indexOf(dragged)
                                    if (to != -1 && from != to) {
                                        // Save to database ONCE
                                        viewModel.reorderStops(from, to)
                                    }
                                }
                            }
                            draggedItem = null
                            draggedOffset = 0f
                            initialIndex = null
                        },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

@Composable
fun StopCard(
    stopWithDepartures: StopWithDepartures,
    isRefreshing: Boolean,
    isDragging: Boolean = false,
    offsetY: Float = 0f,
    onClick: () -> Unit,
    onDragStart: () -> Unit = {},
    onDrag: (change: androidx.compose.ui.input.pointer.PointerInputChange, dragAmount: Offset) -> Unit = { _, _ -> },
    onDragEnd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer {
                translationY = offsetY
                alpha = if (isDragging) 0.8f else 1f
                scaleX = if (isDragging) 1.05f else 1f
                scaleY = if (isDragging) 1.05f else 1f
                shadowElevation = if (isDragging) 16f else 2f
            }
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { onDragStart() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(change, dragAmount)
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            }
            .clickable(enabled = !isDragging, onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 16.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stopWithDepartures.savedStop.stopName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Zobrazení vlastního směru pokud existuje
                    val displayDirection = stopWithDepartures.savedStop.customDirection 
                        ?: stopWithDepartures.savedStop.direction
                    
                    displayDirection?.let { direction ->
                        Text(
                            text = direction,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    stopWithDepartures.savedStop.platformCode?.let { platform ->
                        Text(
                            text = "Platform: $platform",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            when {
                stopWithDepartures.isLoading && !isRefreshing -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
                stopWithDepartures.error != null -> {
                    Text(
                        text = "Error loading departures",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                stopWithDepartures.departures.isEmpty() -> {
                    Text(
                        text = "No upcoming departures",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        stopWithDepartures.departures.take(3).forEach { departure ->
                            DepartureItem(departure)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DepartureItem(departure: Departure) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = getRouteTypeIcon(departure.routeType),
                style = MaterialTheme.typography.titleMedium
            )
            
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = departure.routeShortName,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = departure.headsign,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
                if (departure.isDelayed) {
                    Text(
                        text = "Delayed ${departure.delayMinutes ?: 0} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        
        Text(
            text = "${departure.minutes} min",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
