package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.domain.i18n.LocalNexaStrings
import kotlin.math.roundToInt

/**
 * Draggable and width-resizable ruled-paper side notebook for Study lessons.
 * Auto-saves changes on every text edit.
 */
@Composable
fun RuledSideNoteOverlay(
    noteText: String,
    onNoteChange: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalNexaStrings.current
    val density = LocalDensity.current
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(40f) }
    var panelWidthDp by remember { mutableFloatStateOf(290f) }

    val lineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.38f)
    val marginColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.45f)
    val paperBg = MaterialTheme.colorScheme.surface

    Surface(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .width(panelWidthDp.dp)
            .heightIn(min = 240.dp, max = 380.dp)
            .shadow(10.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp)),
        color = paperBg,
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Draggable header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.DragIndicator,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = strings.sideNoteTitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                // Width resize handle
                Icon(
                    imageVector = Icons.Rounded.SwapHoriz,
                    contentDescription = "Resize width",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .size(28.dp)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val deltaDp = with(density) { dragAmount.x.toDp().value }
                                panelWidthDp = (panelWidthDp + deltaDp).coerceIn(220f, 380f)
                            }
                        }
                )
                IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = strings.close,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Ruled paper canvas + auto-saving TextField
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val step = 28.dp.toPx()
                        var y = step
                        while (y < size.height) {
                            drawLine(
                                color = lineColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                            y += step
                        }
                        drawLine(
                            color = marginColor,
                            start = Offset(24.dp.toPx(), 0f),
                            end = Offset(24.dp.toPx(), size.height),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                    .padding(start = 28.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
            ) {
                TextField(
                    value = noteText,
                    onValueChange = onNoteChange,
                    placeholder = {
                        Text(
                            text = strings.sideNoteHint,
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
