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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import org.jetbrains.jewel.intui.core.theme.IntUiLightTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.ui.component.TabData
import org.jetbrains.jewel.ui.component.TabStrip
import org.jetbrains.jewel.ui.component.Text

fun main() {
  singleWindowApplication(title = "Hello, Bard!") {
    IntUiTheme {
      App()
    }
  }
}

@Composable
fun App() {
  val bgColor = IntUiLightTheme.colors.grey(14)
  Column(
    Modifier.fillMaxSize().background(bgColor)
  ) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = remember(tabIndex) {
      mutableStateListOf(
        TabData.Default(
          selected = tabIndex == 0,
          content = { Text("CSS Gradients") },
          onClick = { tabIndex = 0 },
          closable = false,
        ),
        TabData.Default(
          selected = tabIndex == 1,
          content = { Text("Gradient text") },
          onClick = { tabIndex = 1 },
          closable = false,
        ),
      )
    }

    TabStrip(tabs, modifier = Modifier.fillMaxWidth())

    val contentModifier = Modifier.fillMaxSize().padding(16.dp)
    when (tabIndex) {
      0 -> CssGradientDemo(contentModifier)
      1 -> TextGradientDemo(contentModifier)
    }
  }
}
