package com.example.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServerHealthStatus
import com.example.ui.theme.FzStatusAmber
import com.example.ui.theme.FzStatusGreen
import com.example.ui.theme.FzStatusOrange
import com.example.ui.theme.FzStatusRed
import com.example.ui.theme.FzSurfaceBorder

@Composable
fun ServerStatusBadge(
    status: ServerHealthStatus,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val (statusColor, statusBg) = when (status) {
        ServerHealthStatus.ONLINE -> Pair(FzStatusGreen, Color(0xFF0F291E))
        ServerHealthStatus.DEGRADED -> Pair(FzStatusAmber, Color(0xFF2C2211))
        ServerHealthStatus.MAINTENANCE -> Pair(FzStatusOrange, Color(0xFF2D1B11))
        ServerHealthStatus.OFFLINE -> Pair(FzStatusRed, Color(0xFF2E1215))
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateColor(
        initialValue = statusColor,
        targetValue = statusColor.copy(alpha = 0.35f),
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colorPulse"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .testTag("server_status_badge")
            .clip(RoundedCornerShape(20.dp))
            .background(statusBg)
            .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (status == ServerHealthStatus.ONLINE) alpha else statusColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = status.displayName.uppercase(),
            color = statusColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        if (status.latencyMs > 0) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${status.latencyMs}ms",
                color = statusColor.copy(alpha = 0.8f),
                fontSize = 10.sp
            )
        }
    }
}
