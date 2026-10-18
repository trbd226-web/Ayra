package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AyraCardBorder
import com.example.ui.theme.AyraCyan
import com.example.ui.theme.AyraError
import com.example.ui.theme.AyraMidnightBg
import com.example.ui.theme.AyraSurfaceDark
import com.example.ui.theme.AyraSurfaceVariant
import com.example.ui.theme.AyraTextPrimary
import com.example.ui.theme.AyraTextSecondary
import com.example.ui.theme.AyraViolet
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LocalFileInfo(
    val file: File,
    val name: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val isDirectory: Boolean
)

@Composable
fun FileManagerScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val filesList = remember { mutableStateListOf<LocalFileInfo>() }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog states
    var fileToDelete by remember { mutableStateOf<LocalFileInfo?>(null) }
    var deleteConfirmInput by remember { mutableStateOf("") }
    var deleteErrorMsg by remember { mutableStateOf("") }

    var fileToRename by remember { mutableStateOf<LocalFileInfo?>(null) }
    var newFileNameInput by remember { mutableStateOf("") }

    var showCreateDialog by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }
    var newFileContent by remember { mutableStateOf("") }

    var viewedFileContent by remember { mutableStateOf<Pair<String, String>?>(null) }

    fun refreshFiles() {
        val storageDir = File(context.filesDir, "ayra_documents")
        if (!storageDir.exists()) storageDir.mkdirs()

        // Create sample guide file if empty
        val readme = File(storageDir, "ayra_voice_guide.txt")
        if (!readme.exists()) {
            readme.writeText("AYRA Voice Assistant Documentation\n\nSupports Bengali, Banglish, and English voice commands.\nSay 'Hey Ayra, open YouTube' or 'Ayra, turn on flashlight'.")
        }

        filesList.clear()
        val all = storageDir.listFiles()?.map {
            LocalFileInfo(
                file = it,
                name = it.name,
                sizeBytes = it.length(),
                lastModified = it.lastModified(),
                isDirectory = it.isDirectory
            )
        }?.sortedByDescending { it.lastModified } ?: emptyList()

        filesList.addAll(all)
    }

    LaunchedEffect(Unit) {
        refreshFiles()
    }

    // Document Picker launcher
    val docPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, context.contentResolver.getType(uri) ?: "*/*")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    // Strict Delete Confirmation Dialog (Mandatory exact name matching)
    if (fileToDelete != null) {
        val target = fileToDelete!!
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = { Text("Confirm File Deletion", color = AyraError, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "To permanently delete '${target.name}', please type the exact file name below for confirmation:",
                        color = AyraTextPrimary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = deleteConfirmInput,
                        onValueChange = {
                            deleteConfirmInput = it
                            deleteErrorMsg = ""
                        },
                        placeholder = { Text(target.name, color = AyraTextSecondary.copy(alpha = 0.5f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AyraError,
                            unfocusedBorderColor = AyraCardBorder,
                            focusedTextColor = AyraTextPrimary,
                            unfocusedTextColor = AyraTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (deleteErrorMsg.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = deleteErrorMsg, color = AyraError, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deleteConfirmInput.trim() == target.name.trim()) {
                            target.file.delete()
                            fileToDelete = null
                            deleteConfirmInput = ""
                            refreshFiles()
                        } else {
                            deleteErrorMsg = "Name does not match exact file name."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AyraError, contentColor = Color.White)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) {
                    Text("Cancel", color = AyraTextSecondary)
                }
            },
            containerColor = AyraSurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Rename Dialog
    if (fileToRename != null) {
        val target = fileToRename!!
        AlertDialog(
            onDismissRequest = { fileToRename = null },
            title = { Text("Rename File", color = AyraCyan, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newFileNameInput,
                    onValueChange = { newFileNameInput = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AyraCyan,
                        unfocusedBorderColor = AyraCardBorder,
                        focusedTextColor = AyraTextPrimary,
                        unfocusedTextColor = AyraTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFileNameInput.isNotBlank()) {
                            val newFile = File(target.file.parentFile, newFileNameInput.trim())
                            target.file.renameTo(newFile)
                            fileToRename = null
                            refreshFiles()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AyraCyan, contentColor = Color(0xFF001F29))
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToRename = null }) {
                    Text("Cancel", color = AyraTextSecondary)
                }
            },
            containerColor = AyraSurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // View Content Dialog
    if (viewedFileContent != null) {
        val (name, body) = viewedFileContent!!
        AlertDialog(
            onDismissRequest = { viewedFileContent = null },
            title = { Text(name, color = AyraCyan, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = body,
                    color = AyraTextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewedFileContent = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AyraCyan, contentColor = Color(0xFF001F29))
                ) {
                    Text("Close")
                }
            },
            containerColor = AyraSurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Create File Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Text File", color = AyraCyan, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newFileName,
                        onValueChange = { newFileName = it },
                        placeholder = { Text("Filename (e.g. todo.txt)", color = AyraTextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AyraCyan,
                            unfocusedBorderColor = AyraCardBorder,
                            focusedTextColor = AyraTextPrimary,
                            unfocusedTextColor = AyraTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newFileContent,
                        onValueChange = { newFileContent = it },
                        placeholder = { Text("File content...", color = AyraTextSecondary) },
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AyraCyan,
                            unfocusedBorderColor = AyraCardBorder,
                            focusedTextColor = AyraTextPrimary,
                            unfocusedTextColor = AyraTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val nameWithExt = if (newFileName.contains(".")) newFileName.trim() else "${newFileName.trim()}.txt"
                        if (nameWithExt.isNotBlank()) {
                            val storageDir = File(context.filesDir, "ayra_documents")
                            File(storageDir, nameWithExt).writeText(newFileContent)
                            newFileName = ""
                            newFileContent = ""
                            showCreateDialog = false
                            refreshFiles()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AyraCyan, contentColor = Color(0xFF001F29))
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = AyraTextSecondary)
                }
            },
            containerColor = AyraSurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AyraMidnightBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                        text = "Authorized File Manager",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AyraTextPrimary
                    )
                }

                IconButton(onClick = { docPickerLauncher.launch(arrayOf("*/*")) }) {
                    Icon(imageVector = Icons.Default.FolderOpen, contentDescription = "Pick Document", tint = AyraCyan)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name or extension (.txt, .pdf)", color = AyraTextSecondary, fontSize = 13.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = AyraTextSecondary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AyraCyan,
                    unfocusedBorderColor = AyraCardBorder,
                    focusedTextColor = AyraTextPrimary,
                    unfocusedTextColor = AyraTextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            val filteredList = filesList.filter {
                searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No files found",
                        fontSize = 14.sp,
                        color = AyraTextSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.name }) { item ->
                        FileItemRow(
                            item = item,
                            onClick = {
                                try {
                                    val content = item.file.readText()
                                    viewedFileContent = Pair(item.name, content)
                                } catch (_: Exception) {
                                    viewedFileContent = Pair(item.name, "Binary file or cannot display text directly. Size: ${item.sizeBytes} bytes.")
                                }
                            },
                            onRename = {
                                fileToRename = item
                                newFileNameInput = item.name
                            },
                            onDelete = {
                                fileToDelete = item
                                deleteConfirmInput = ""
                                deleteErrorMsg = ""
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = AyraCyan,
            contentColor = Color(0xFF001F29),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "New File")
        }
    }
}

@Composable
private fun FileItemRow(
    item: LocalFileInfo,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    val dateStr = dateFormat.format(Date(item.lastModified))
    val sizeKb = item.sizeBytes / 1024.0

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AyraSurfaceDark),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AyraCardBorder)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(AyraCyan.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = AyraCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AyraTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f KB • %s", sizeKb, dateStr),
                        fontSize = 11.sp,
                        color = AyraTextSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRename, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Rename", tint = AyraViolet, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = AyraError, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
