package com.naveen.arles.ui.components.navrail

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.MotionEvent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.util.SortedMap
import java.util.TreeMap

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LetterNavigationRail(
    letterMap: SortedMap<String, Int>,
    listState: ScrollState,
    positionMap: SnapshotStateMap<String, Float>,
    selectedLetter: String,
    setSelectedLetter: (String) -> Unit,
    isPressed: Boolean,
    setIsPressed: (Boolean) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var navRailTopPos by remember { mutableStateOf(0F) }
    var draggingOffset by remember { mutableStateOf(0F) }
    val letterOffsets = TreeMap<Float, String>()

    val vibratorManager =
        (LocalContext.current.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator

    val vibrationEffect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .draggable(orientation = Orientation.Vertical, state = rememberDraggableState { delta ->
                draggingOffset += delta
                coroutineScope.launch {
                    val floorOffset = letterOffsets.floorKey(draggingOffset) ?: 0F
                    val target = letterOffsets[floorOffset]
                    if (target != selectedLetter) {
                        listState.scrollTo(
                            (positionMap[target]?.let { (it - navRailTopPos).toInt() }) ?: 0
                        )
                        vibratorManager.vibrate(vibrationEffect)
                        target?.let { setSelectedLetter(it) }
                    }
                }
            }, onDragStarted = {
                draggingOffset = it.y
                setIsPressed(true)
            }, onDragStopped = {
                setIsPressed(false)
            }), verticalArrangement = Arrangement.Top
    ) {

        Spacer(modifier = Modifier.height(350.dp))
        (letterMap).map { (letter, index) ->
            val distance = if (isPressed) {
                val (fromLetter, toLetter) = if (letter < selectedLetter) (letter to selectedLetter) else (selectedLetter to letter)
                letterMap.subMap(fromLetter, toLetter).size
            } else Int.MAX_VALUE
            val rightPadding = if (isPressed) when (distance) {
                0 -> (-50).dp
                1 -> (-45).dp
                2 -> (-35).dp
                3 -> (-20).dp
                4 -> (-10).dp
                5 -> (-5).dp
                else -> 0.dp
            } else 0.dp
            Text(text = letter,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .wrapContentSize()
                    .offset(x = rightPadding)
                    .size(Dp(MaterialTheme.typography.titleLarge.fontSize.value))
                    .onGloballyPositioned {
                        if (index == 0) {
                            navRailTopPos = it.positionInParent().y
                        }
                        letterOffsets[it.positionInParent().y] = letter
                    }
                    .pointerInteropFilter {
                        if (it.action == MotionEvent.ACTION_DOWN) {
                            setIsPressed(true)
                            setSelectedLetter(letter)
                            coroutineScope.launch {
                                listState.scrollTo(
                                    (positionMap[letter]?.let { (it - navRailTopPos).toInt() }) ?: 0
                                )
                                vibratorManager.vibrate(vibrationEffect)
                            }
                            return@pointerInteropFilter true
                        } else if (it.action == MotionEvent.ACTION_UP) {
                            setIsPressed(false)
                        }
                        return@pointerInteropFilter false
                    })
        }
    }
}