package com.naveen.arles.ui.components.applisting

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.dp


@Composable
fun RowScope.AppListing(
    appList: Map<String, List<AppListingItemData>>,
    listState: ScrollState,
    positionMap: SnapshotStateMap<String, Float>,
    selectedLetter: String,
    isPressed: Boolean,
) {

    Column(
        modifier = Modifier
            .weight(1f)
            .verticalScroll(listState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(modifier = Modifier.height(325.dp))
        appList.forEach { (startingLetter, apps) ->
            val alpha = if (isPressed && selectedLetter != startingLetter) 0F else 1F
            Text(
                text = startingLetter,
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 15.dp)
                    .onGloballyPositioned {
                        positionMap[startingLetter] = it.positionInParent().y
                    }
                    .alpha(alpha),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            apps.map {
                AppListingItem(item = it, alpha = alpha)
            }
        }
        Spacer(modifier = Modifier.height(50.dp))
    }
}