package com.naveen.arles

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.naveen.arles.ui.components.applisting.AppListing
import com.naveen.arles.ui.components.applisting.AppListingItemData
import com.naveen.arles.ui.components.navrail.LetterNavigationRail
import com.naveen.arles.ui.theme.ArlesAppDrawerTheme
import java.util.Collections
import java.util.SortedMap

class AppDrawerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var appList by remember {
                mutableStateOf(Collections.emptySortedMap<String, List<AppListingItemData>>())
            }
            val startingLetters by remember {
                derivedStateOf {
                    var currentIndex: Int = 0
                    val startingChars = mutableMapOf<String, Int>()
                    appList.forEach { (letter, apps) ->
                        startingChars[letter] = currentIndex
                        currentIndex += apps.size + 1
                    }
                    return@derivedStateOf startingChars.toSortedMap()
                }
            }

            LaunchedEffect(Unit) {
                appList = getAppList()
            }

            AppDrawer(appList = appList, startingLetters = startingLetters)

        }

        window.setDecorFitsSystemWindows(false)
        window.insetsController?.let {
            it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            it.hide(WindowInsets.Type.statusBars())
        }
    }

    private fun getAppList(): SortedMap<String, List<AppListingItemData>> {
        val packageManager = applicationContext.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(
            mainIntent,
            PackageManager.ResolveInfoFlags.of(PackageManager.GET_META_DATA.toLong())
        ).map {
            AppListingItemData(
                it.loadLabel(packageManager).toString(),
                it.loadIcon(packageManager),
                it.activityInfo.packageName,
            )
        }.groupBy {
            (it.name.first().takeIf { char -> "[a-zA-Z]".toRegex().matches(char.toString()) }
                ?: '#').uppercase()
        }.toSortedMap()
    }
}

@Composable
fun AppDrawer(
    appList: SortedMap<String, List<AppListingItemData>>,
    startingLetters: SortedMap<String, Int>
) {
    val appListScrollState = rememberScrollState()
    val positionMap = remember { mutableStateMapOf<String, Float>() }
    val (selectedLetter, setSelectedLetter) = remember { mutableStateOf("") }
    val (isPressed, setIsPressed) = remember { mutableStateOf(false) }

    val backgroundAlpha by animateFloatAsState(targetValue = if (appList.isNotEmpty()) 0.75F else 1F)

    ArlesAppDrawerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = backgroundAlpha),
        ) {
            AnimatedVisibility(
                visible = appList.isNotEmpty(),
                enter = fadeIn() + slideInVertically(
                    animationSpec = tween(),
                    initialOffsetY = { it / 2 })
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 15.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    AppListing(
                        appList = appList,
                        listState = appListScrollState,
                        positionMap = positionMap,
                        selectedLetter = selectedLetter,
                        isPressed = isPressed,
                    )
                    LetterNavigationRail(
                        letterMap = startingLetters,
                        listState = appListScrollState,
                        positionMap = positionMap,
                        selectedLetter = selectedLetter,
                        setSelectedLetter = setSelectedLetter,
                        isPressed = isPressed,
                        setIsPressed = setIsPressed,
                    )
                }
            }
        }
    }
}
