package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CommandHistoryEntity
import com.example.ui.AyraViewModel
import com.example.ui.theme.AyraCardBorder
import com.example.ui.theme.AyraCyan
import com.example.ui.theme.AyraError
import com.example.ui.theme.AyraMidnightBg
import com.example.ui.theme.AyraSuccess
import com.example.ui.theme.AyraSurfaceDark
import com.example.ui.theme.AyraTextPrimary
import com.example.ui.theme.AyraTextSecondary
import com.example.ui.theme.AyraViolet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommandHistoryScreen(
    viewModel: AyraViewModel,
    onBack: () -> Unit,
    onReExecute: (String) -> Unit
) {
    val historyList by viewModel.commandHistory.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Command History", color = AyraCyan, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete all recorded command logs? This action cannot be undone.", color = AyraTextPrimary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearCommandHistory()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AyraError, contentColor = Color.White)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = AyraTextSecondary)
                }
            },
            containerColor = AyraSurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AyraMidnightBg)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AyraCyan)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Command History",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AyraTextPrimary
                )
            }

            if (historyList.isNotEmpty()) {
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear History", tint = AyraError)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No command history yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = AyraTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Commands you speak or type will be logged here.",
                        fontSize = 12.sp,
                        color = AyraTextSecondary.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyList, key = { it.id }) { item ->
                    CommandHistoryRow(
                        item = item,
                        onToggleFavorite = { viewModel.toggleFavoriteCommand(item.id, !item.isFavorite) },
                        onReRun = { onReExecute(item.spokenText) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CommandHistoryRow(
    item: CommandHistoryEntity,
    onToggleFavorite: () -> Unit,
    onReRun: () -> Unit
) {
    val timeFormat = SimpleDateFormat("h:mm a, MMM d", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(item.timestamp))

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AyraSurfaceDark),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AyraCardBorder)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (item.executionStatus == "SUCCESS") AyraSuccess.copy(alpha = 0.15f) else AyraError.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = item.executionStatus,
                            color = if (item.executionStatus == "SUCCESS") AyraSuccess else AyraError,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = formattedTime,
                        fontSize = 11.sp,
                        color = AyraTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.spokenText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AyraTextPrimary
                )

                if (item.feedbackMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.feedbackMessage,
                        fontSize = 12.sp,
                        color = AyraTextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Toggle Favorite",
                        tint = if (item.isFavorite) Color(0xFFFFD700) else AyraTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onReRun, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Re-execute",
                        tint = AyraCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
