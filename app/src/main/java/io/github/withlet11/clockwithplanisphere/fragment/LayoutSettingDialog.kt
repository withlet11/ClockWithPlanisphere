/**
 * LayoutSettingDialog.kt
 *
 * Copyright 2026 Yasuhiro Yamakawa <withlet11@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.withlet11.clockwithplanisphere.fragment

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.edit
import io.github.withlet11.clockwithplanisphere.HORIZONTAL_POSITION_KEY
import io.github.withlet11.clockwithplanisphere.PREFERENCES_NAME
import io.github.withlet11.clockwithplanisphere.R
import io.github.withlet11.clockwithplanisphere.VERTICAL_POSITION_KEY

enum class VerticalPosition { TOP, BOTTOM, NONE }
enum class HorizontalPosition { LEFT, RIGHT, NONE }

@Composable
fun LayoutSettingDialog(
    onDismiss: () -> Unit,
    onLayoutSaved: () -> Unit,
) {
    val context = LocalContext.current

    var verticalPosition by rememberSaveable { mutableStateOf(VerticalPosition.TOP) }
    var horizontalPosition by rememberSaveable { mutableStateOf(HorizontalPosition.LEFT) }
    var currentVerticalPosition by rememberSaveable { mutableStateOf(VerticalPosition.TOP) }
    var currentHorizontalPosition by rememberSaveable { mutableStateOf(HorizontalPosition.LEFT) }

    LaunchedEffect(Unit) {
        val preferences = context.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

        verticalPosition = preferences.getString(
            VERTICAL_POSITION_KEY,
            null
        )?.let {
            VerticalPosition.valueOf(it)
        } ?: VerticalPosition.TOP

        horizontalPosition = preferences.getString(
            HORIZONTAL_POSITION_KEY,
            null
        )?.let {
            HorizontalPosition.valueOf(it)
        } ?: HorizontalPosition.LEFT

        currentVerticalPosition = verticalPosition
        currentHorizontalPosition = horizontalPosition
    }

    val isPositiveButtonEnabled =
        verticalPosition != currentVerticalPosition || horizontalPosition != currentHorizontalPosition

    val dialogColor = MaterialTheme.colorScheme.surfaceContainer

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = dialogColor,
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 500.dp)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.layoutSettings),
                    style = MaterialTheme.typography.titleLarge,
                )

                Text(text = "Information Panel Position")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(
                        modifier = Modifier.weight(1.1f),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .width(113.dp)
                                .height(80.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf(
                                "Left" to HorizontalPosition.LEFT,
                                "Right" to HorizontalPosition.RIGHT,
                            ).forEach { (label, value) ->
                                val selected = horizontalPosition == value

                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (selected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        )
                                        .fillMaxHeight()
                                        .weight(1f)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .clickable {
                                            horizontalPosition =
                                                if (horizontalPosition == value) HorizontalPosition.NONE else value
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column {
                                        Text(
                                            text = label,
                                            color = if (selected) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                        Icon(
                                            imageVector = if (selected) {
                                                Icons.Outlined.CheckCircleOutline
                                            } else {
                                                Icons.Outlined.Circle
                                            },
                                            contentDescription = null,
                                            tint = if (selected) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Text(text = "LandScape")
                    }

                    Column(
                        modifier = Modifier.weight(0.9f),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .width(80.dp)
                                .height(113.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf(
                                "Top" to VerticalPosition.TOP,
                                "Bottom" to VerticalPosition.BOTTOM,
                            ).forEach { (label, value) ->
                                val selected = verticalPosition == value

                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (selected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        )
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .clickable {
                                            verticalPosition =
                                                if (verticalPosition == value) VerticalPosition.NONE else value
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row {
                                        Text(
                                            text = label,
                                            color = if (selected) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                        Icon(
                                            imageVector = if (selected) {
                                                Icons.Outlined.CheckCircleOutline
                                            } else {
                                                Icons.Outlined.Circle
                                            },
                                            contentDescription = null,
                                            tint = if (selected) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Text(text = "Portrait")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }

                    TextButton(
                        onClick = {
                            context.getSharedPreferences(
                                PREFERENCES_NAME,
                                Context.MODE_PRIVATE
                            ).edit {
                                putString(
                                    "horizontalPosition",
                                    horizontalPosition.name
                                )
                                putString(
                                    "verticalPosition",
                                    verticalPosition.name
                                )
                                commit()
                            }

                            onLayoutSaved()
                            onDismiss()
                        },
                        enabled = isPositiveButtonEnabled
                    ) {
                        Text(stringResource(R.string.modify))
                    }
                }
            }
        }
    }
}
