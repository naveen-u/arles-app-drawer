package com.naveen.arles.ui.components.applisting

import android.content.Context
import android.os.VibrationEffect
import android.os.VibratorManager
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.google.accompanist.drawablepainter.DrawablePainter


@Composable
fun AppListingItem(item: AppListingItemData, alpha: Float) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val leftPadding: Dp by animateDpAsState(if (isPressed) 100.dp else 0.dp)
    val vibratorManager =
        (LocalContext.current.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator

    val vibrationEffect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interactionSource, indication = null) {
                launchActivity(
                    context = context,
                    item.packageName
                )
                vibratorManager.vibrate(vibrationEffect)
            }
            .absolutePadding(left = leftPadding)
            .alpha(alpha),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = DrawablePainter(item.icon),
            contentDescription = item.name,
            modifier = Modifier
                .size(50.dp),
        )
        Text(
            text = item.name,
            modifier = Modifier.padding(horizontal = 15.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun launchActivity(context: Context, packageName: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        ?: throw Error("Could not launch application")
    startActivity(context, intent, null)
}
