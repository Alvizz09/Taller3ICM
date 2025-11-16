package com.alviz.talle3icm.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RowItem(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, action: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
            .height(50.dp)
            .padding(4.dp)
            .clickable { action() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(12.dp))
        Text(text)
    }
}