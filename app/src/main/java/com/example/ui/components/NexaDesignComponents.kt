package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Unified NOX visual tokens and reusable primitives applied across every NEXA screen:
 * - Pure white / dark espresso elevated cards on the calm coffee-beige canvas (#EFE6DC / #120E0C)
 * - Calm, non-garish section accent colors with readable contrast
 * - Elevated filled squircle icon badges
 * - Unified pill-style segmented tab strips & screens headers
 */
object NexaSectionAccents {
    val study = Color(0xFF2D5D8A)       // Deep calm slate-blue
    val tasks = Color(0xFF3E5668)       // Charcoal indigo-slate
    val habits = Color(0xFFA95C38)      // Warm terracotta coffee
    val focus = Color(0xFF286B5E)       // Deep forest teal
    val prayer = Color(0xFF2F6F56)      // Calm sage emerald
    val summaries = Color(0xFF6B4C82)   // Muted plum-lavender
    val life = Color(0xFF8E5536)        // Warm bronze espresso
    val badges = Color(0xFF966E2D)      // Muted antique gold
}

@Composable
fun NexaCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 2.dp,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = containerColor,
            shadowElevation = elevation,
            tonalElevation = 0.dp,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column(content = content)
        }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = containerColor,
            shadowElevation = elevation,
            tonalElevation = 0.dp,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun NexaFilledIconBadge(
    icon: ImageVector,
    tint: Color,
    contentDescription: String? = null,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    cornerRadius: Dp = 14.dp
) {
    val shape = RoundedCornerShape(cornerRadius)
    Surface(
        modifier = Modifier
            .size(size)
            .shadow(elevation = 3.dp, shape = shape, clip = false),
        shape = shape,
        color = tint,
        contentColor = Color.White
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@Composable
fun NexaTintedBanner(
    text: String,
    icon: ImageVector,
    accent: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    NexaCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        cornerRadius = 18.dp,
        elevation = 1.dp,
        borderColor = accent.copy(alpha = 0.25f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NexaFilledIconBadge(
                icon = icon,
                tint = accent,
                size = 38.dp,
                iconSize = 20.dp,
                cornerRadius = 11.dp
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            action?.invoke()
        }
    }
}

@Composable
fun NexaScreenTitleRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    accent: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (icon != null) {
            NexaFilledIconBadge(
                icon = icon,
                tint = accent,
                size = 44.dp,
                iconSize = 22.dp,
                cornerRadius = 14.dp
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun NexaSegmentedTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { idx, label ->
                val selected = idx == selectedIndex
                Surface(
                    onClick = { onSelect(idx) },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 42.dp)
                        .then(if (testTagPrefix != null) Modifier.testTag("${testTagPrefix}_$idx") else Modifier),
                    shape = RoundedCornerShape(13.dp),
                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NexaEmptyState(
    message: String,
    icon: ImageVector = Icons.Rounded.Inbox,
    accent: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    NexaCard(
        modifier = modifier,
        cornerRadius = 20.dp,
        elevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NexaFilledIconBadge(
                icon = icon,
                tint = accent.copy(alpha = 0.85f),
                size = 48.dp,
                iconSize = 24.dp,
                cornerRadius = 15.dp
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
