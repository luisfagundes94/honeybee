package com.luisfagundes.library.impl.presentation.components

import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrashBadgedBox(
    itemsInTrash: Int,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    BadgedBox(
        badge = {
            if (itemsInTrash > 0) {
                Badge(
                    modifier = Modifier.offset(x = (-12).dp, y = 12.dp)
                ) {
                    val displayCount = if (itemsInTrash > 99) "99+" else itemsInTrash.toString()
                    Text(text = displayCount)
                }
            }
        },
        modifier = modifier
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = if (itemsInTrash > 0) {
                    Icons.Default.Delete
                } else {
                    Icons.Default.RestoreFromTrash
                },
                contentDescription = contentDescription,
            )
        }
    }
}
