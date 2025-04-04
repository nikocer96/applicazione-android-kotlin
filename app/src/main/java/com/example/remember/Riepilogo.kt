package com.example.remember

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.remember.data.Nota
import com.example.remember.viewModel.NotesViewModel
import kotlinx.coroutines.launch
import kotlin.math.exp

@Composable
fun RiepilogoScreen(
    navController: NavHostController,
    viewModel: NotesViewModel,
    isDarkTheme: MutableState<Boolean>
) {
    val context = LocalContext.current
    val allNotes by viewModel.getAllNotes.collectAsState(initial = emptyList())
    var showDialog = remember { mutableStateOf(false) }
    var showDeleteDialog = remember { mutableStateOf(false) }
    var selectedNote = remember { mutableStateOf<Nota?>(null) }

    var hour by remember { mutableStateOf(0) }
    var minute by remember { mutableStateOf(0) }
    var isDialogOpen by remember { mutableStateOf(false) }

    val snackbarHostState = remember {SnackbarHostState()}
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
    ) {
        SnackbarHost(hostState = snackbarHostState)
        LazyColumn() {
            items(allNotes) { nota ->
                var expanded = remember { mutableStateOf(false) }
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp), // Distanziamento interno
                        horizontalArrangement = Arrangement.SpaceBetween, // Spazio tra testo e icona
                        verticalAlignment = Alignment.CenterVertically // Allinea il testo e l'icona verticalmente
                    ) {
                        // Testo della nota
                        Column(modifier = Modifier.width(310.dp)) {
                            Text(
                                text = "Fase: ${nota.fase} \n Contenuto: ${nota.contenuto} \n Data: ${nota.data} \n Ora: ${nota.orario}",
                                style = MaterialTheme.typography.bodyMedium

                            )
                        }

                        Box() {
                            IconButton(onClick = { expanded.value = true }) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "Opzioni riepilogo"
                                )
                            }
                            DropdownMenu(
                                expanded = expanded.value,
                                onDismissRequest = { expanded.value = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(text = "Elimina") },
                                    onClick = {
                                        selectedNote.value = nota
                                        showDeleteDialog.value = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Elimina")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(text = "Modifica") },
                                    onClick = {
                                        selectedNote.value = nota
                                        showDialog.value = true
                                        expanded.value = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = "Modifica"
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(text = "Notifica") },
                                    onClick = {
                                        isDialogOpen = true
                                        selectedNote.value = nota // Salva la nota selezionata per usarla nella notifica
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Notifications,
                                            contentDescription = "Programma notifica"
                                        )
                                    }
                                )
                            }
                        }

                        // Icona per eliminare la nota
                        /*
                        IconButton(
                            onClick = {
                                selectedNote.value = nota
                                showDeleteDialog.value = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Elimina",
                                tint = Color.Red
                            )
                        }
                        IconButton(
                            onClick = {
                                selectedNote.value = nota
                                showDialog.value = true
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Modifica",
                                tint = if (isDarkTheme.value) Color.White else Color.Black
                            )
                        }
                        IconButton(
                            onClick = {
                                ""
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Notifica",
                                tint = Color.Red
                            )
                        }

                         */
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (isDialogOpen) {
            val timePickerDialog = TimePickerDialog(
                context,
                { _, selectedHour, selectedMinute ->
                    hour = selectedHour
                    minute = selectedMinute
                    isDialogOpen = false

                    // Passiamo il contenuto della nota selezionata
                    viewModel.sendNotificationAt(context, hour, minute, selectedNote.value?.contenuto ?: "Nessun contenuto")
                },
                hour,
                minute,
                true
            )
            timePickerDialog.show()
        }


        if (showDialog.value && selectedNote.value != null) {
            ModficaNota(
                nota = selectedNote.value!!,
                onDismiss = { showDialog.value = false },
                onSave = {updateNote ->
                    viewModel.updateNote(updateNote)
                    showDialog.value = false
                }
            )
        }
        if (showDeleteDialog.value && selectedNote.value != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog.value = false },
                title = {Text(text = "Conferma eliminazione")},
                text = {Text(text = "Sei sicuro di voler eliminare questa nota?")},
                confirmButton = {
                    Button(onClick = {
                        viewModel.deleteNote(selectedNote.value!!)
                        showDeleteDialog.value = false

                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Nota eliminata con successo",
                                actionLabel = "ok"
                            )
                        }
                    }) {
                        Text("Si",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                            )
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        showDeleteDialog.value = false
                    }) {
                        Text("No",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                            )
                    }
                }
            )

        }
    }
}

@Composable
fun ModficaNota(nota: Nota, onDismiss: () -> Unit, onSave: (Nota) -> Unit) {
    var updateContent by remember { mutableStateOf(nota.contenuto) }
    var updateFase by remember { mutableStateOf(nota.fase) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {Text(text = "Modifica Nota")},
        text = {
               Column {
                   TextField(
                       value = updateFase,
                       onValueChange = {updateFase = it},
                       label = { Text(text = "Fase")}
                   )
                   TextField(
                       value = updateContent, 
                       onValueChange = {updateContent = it},
                       label = { Text(text = "Contenuto")}
                   )
               }
        },
        confirmButton = {
            Button(onClick = {
                onSave(nota.copy(fase = updateFase, contenuto = updateContent))
            }) {
                Text(text = "Salva")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = "Annulla")
            }
        }

    )
}





