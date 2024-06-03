/*
 * Copyright 2024 Sebastiano Poggi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.sebastiano.bardhello

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField

@Composable
internal fun AngleSetter(angle: Int, enabled: Boolean = true, onAngleChange: (Int) -> Unit) {
  Row(
    Modifier.padding(16.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text("Angle:")

    TextField(
      angle.toString(),
      { it.toIntOrNull()?.let(onAngleChange) },
      Modifier.onPreviewKeyEvent {
        if (it.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

        val step = if (it.isShiftPressed) 10 else 1
        when (it.key) {
          Key.DirectionUp -> {
            onAngleChange(angle + step)
            true
          }

          Key.DirectionDown -> {
            onAngleChange(angle - step)
            true
          }

          else -> false
        }
      },
      enabled = enabled,
    )

    Text("Tip: use up/down arrows to change (shift for bigger changes)", color = JewelTheme.globalColors.text.info)
  }
}
