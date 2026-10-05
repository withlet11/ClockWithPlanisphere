package io.github.withlet11.clockwithplanisphere.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.withlet11.clockwithplanisphere.CwpTopAppBar
import io.github.withlet11.clockwithplanisphere.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isSouthernSky: Boolean,
    onSouthernSkyChanged: (Boolean) -> Unit,
    onSettingsClick: () -> Unit,
    onBgColorClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onLicensesClick: () -> Unit,
    onCreditsClick: () -> Unit,
    content: @Composable (Modifier) -> Unit,
    // adViewContent: @Composable () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CwpTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.app_name),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                    )
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.north_label),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = isSouthernSky,
                            onCheckedChange = onSouthernSkyChanged,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                uncheckedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                checkedBorderColor = MaterialTheme.colorScheme.primary,
                                uncheckedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            thumbContent = {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                )
                            }
                        )
                        Text(
                            text = stringResource(R.string.south_label),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Menu"
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.locationSettings)) },
                                    onClick = {
                                        menuExpanded = false
                                        onSettingsClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.bg_color)) },
                                    onClick = {
                                        menuExpanded = false
                                        onBgColorClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.privacy_policy_header)) },
                                    onClick = {
                                        menuExpanded = false
                                        onPrivacyPolicyClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.license)) },
                                    onClick = {
                                        menuExpanded = false
                                        onLicensesClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.opensource_licenses)) },
                                    onClick = {
                                        menuExpanded = false
                                        onCreditsClick()
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                content(Modifier.fillMaxSize())
            }

//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .wrapContentHeight(),
//                contentAlignment = Alignment.Center
//            ) {
//                adViewContent()
//            }
        }
    }
}
