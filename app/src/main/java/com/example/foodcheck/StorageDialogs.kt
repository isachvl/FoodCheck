
package com.example.foodcheck

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource

@Composable
fun StorageNameDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(title)
        },

        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = {
                    Text(stringResource(R.string.storage_name_label))
                },
                singleLine = true
            )
        },

        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(name.trim())
                },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.storage_add))
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.storage_cancel))
            }
        }
    )
}

@Composable
fun ConfirmDeleteDialog(
    name: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(stringResource(R.string.storage_delete_title))
        },

        text = {
            Text(
                stringResource(
                    R.string.storage_delete_message,
                    name
                )
            )
        },

        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.storage_delete))
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.storage_cancel))
            }
        }
    )
}
