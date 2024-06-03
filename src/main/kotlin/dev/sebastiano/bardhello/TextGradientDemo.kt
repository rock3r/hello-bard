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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import kotlin.math.roundToInt

@Composable
internal fun TextGradientDemo(modifier: Modifier = Modifier) {
  Column(
    modifier,
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    var rawAngle by remember { mutableIntStateOf(-16) }
    val gradientAnimatable = remember { Animatable(-1f) }

    AnimatedGradientText(
      "Hello, Sebastiano",
      gradientAnimatable,
      gradientAngle = rawAngle.toDouble(),
      modifier = Modifier.weight(1f),
    )

    Spacer(Modifier.height(16.dp))

    val isAnimating = remember { mutableStateOf(false) }
    Row(
      modifier = Modifier.padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      var animationDurationMillis by remember { mutableIntStateOf(1500) }
      val scope = rememberCoroutineScope()

      LaunchedEffect(Unit) {
        scope.doAnimationAsync(isAnimating, gradientAnimatable, animationDurationMillis)
      }

      DefaultButton(onClick = {
        scope.doAnimationAsync(isAnimating, gradientAnimatable, animationDurationMillis)
      }, enabled = !isAnimating.value) {
        BasicText("Start animation", style = TextStyle.Default.copy(Color.White))
      }

      Spacer(Modifier.width(0.dp))

      Text("Duration (ms):")

      TextField(
        value = animationDurationMillis.toString(),
        onValueChange = {
          animationDurationMillis = it.toIntOrNull()
            ?.takeIf { millis -> millis > 0 }
            ?: animationDurationMillis
        },
        enabled = !isAnimating.value
      )

      val animatedPercent by animateFloatAsState(
        if (isAnimating.value) 100f else 0f,
        tween(durationMillis = animationDurationMillis, easing = LinearEasing)
      )
      AnimatedVisibility(isAnimating.value) {
        Text("Animating (${animatedPercent.roundToInt()}%)")
      }
    }

    AngleSetter(rawAngle, enabled = !isAnimating.value) { rawAngle = it }
  }
}

private fun CoroutineScope.doAnimationAsync(
  isAnimating: MutableState<Boolean>,
  gradientAnimatable: Animatable<Float, AnimationVector1D>,
  animationDurationMillis: Int
) {
  launch {
    isAnimating.value = true
    doAnimation(gradientAnimatable, animationDurationMillis)
    isAnimating.value = false
  }
}

private suspend fun doAnimation(animatable: Animatable<Float, AnimationVector1D>, durationMillis: Int) {
  println("Starting animation (${durationMillis} ms)")
  animatable.stop()
  animatable.snapTo(-1f)
  animatable.animateTo(
    targetValue = 0f,
    animationSpec = tween(
      durationMillis = durationMillis,
      easing = CubicBezierEasing(0.3f, 0f, 0.4f, 1f),
    )
  )
  println("Done animating")
}


@Composable
fun AnimatedGradientText(
  text: String,
  animatable: Animatable<Float, AnimationVector1D>,
  modifier: Modifier = Modifier,
  gradientColors: List<Color> = colors,
  gradientStops: List<Float> = stops,
  gradientAngle: Double = -16.0,
  gradientXScale: Float = 4f,
  style: TextStyle = TextStyle.Default.copy(fontSize = 48.sp, fontWeight = FontWeight.Medium),
) {

  // Note: on Android it's not necessary to key on the animated value, since
  // the shader creation for text paints happens inside a derivedStateOf().
  // On desktop, however, this is not the case, so we need to recompute the
  // whole brush when the animated value changes.
  // See https://github.com/JetBrains/compose-multiplatform/issues/4903
  val brush = remember(gradientXScale, animatable.value, gradientAngle, gradientColors, gradientStops) {
    CssGradientBrush(
      angleDegrees = gradientAngle,
      colors = gradientColors,
      stops = gradientStops,
      scaleX = gradientXScale,
      offset = Offset(animatable.value, 0f),
    )
  }

  BasicText(text, modifier, style.copy(brush = brush))
}

private val brandColor1 = Color(0xFF4285F4)
private val brandColor2 = Color(0xFF9B72CB)
private val brandColor3 = Color(0xFFD96570)

private val colors = listOf(
  brandColor1,
  brandColor2,
  brandColor3,
  brandColor3,
  brandColor2,
  brandColor1,
  brandColor2,
  brandColor3,
  Color.White,
  Color.White,
)

private val stops = listOf(0f, .09f, .2f, .24f, .35f, .44f, .5f, .56f, .75f, 1f)
