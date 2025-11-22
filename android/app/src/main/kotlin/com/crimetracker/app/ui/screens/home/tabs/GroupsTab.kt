package com.crimetracker.app.ui.screens.home.tabs

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.crimetracker.app.ui.screens.community.CommunityScreen

@Composable
fun GroupsTab(
    modifier: Modifier = Modifier,
    onNavigateToCreateGroup: () -> Unit,
    onNavigateToGroup: (String) -> Unit = {}
) {
    // Render the full CommunityScreen
    CommunityScreen(
        onNavigateToCreateGroup = onNavigateToCreateGroup,
        onNavigateToGroup = onNavigateToGroup
    )
}
