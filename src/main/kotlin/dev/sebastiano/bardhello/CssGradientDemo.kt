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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.tan

@Composable
internal fun CssGradientDemo(modifier: Modifier = Modifier) {
  Column(
    modifier,
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    var rawAngle by remember { mutableIntStateOf(-16) }

    CssGradient(rawAngle, Modifier.fillMaxWidth().weight(1f))

    Spacer(Modifier.height(16.dp))

    AngleSetter(rawAngle) { rawAngle = it }
  }
}

@Composable
private fun CssGradient(angleDegrees: Int, modifier: Modifier = Modifier, debug: Boolean = true) {
  Canvas(modifier.padding(horizontal = 128.dp, vertical = 64.dp)) {
    val normalizedAngle = angleDegrees.toDouble() % 360.0

    // Handle base cases (vertical and horizontal gradient) separately
    when {
      abs(normalizedAngle % 180.0) < angleEpsilon -> {
        val leftToRight = abs(normalizedAngle) < 90.0
        drawHorizontalGradient(leftToRight, debug)
      }

      abs(abs(normalizedAngle) - 90.0) < angleEpsilon -> {
        val startsFromTop = normalizedAngle >= 0.0
        drawVerticalGradient(startsFromTop, debug)
      }

      else -> drawLinearGradient(normalizedAngle, debug)
    }
  }
}

private fun DrawScope.drawHorizontalGradient(leftToRight: Boolean, debug: Boolean) {
  val startX = if (leftToRight) 0f else size.width
  val endX = if (leftToRight) size.width else 0f
  val brush = Brush.horizontalGradient(*colorStops, startX = startX, endX = endX)
  drawRect(brush)

  /////////////// VISUALIZATION /////////////////
  if (!debug) return

  val gradientStart = Offset(startX, center.y)
  val gradientEnd = Offset(endX, center.y)
  val startCorner = if (leftToRight) Offset(0f, 0f) else Offset(size.width, 0f)
  val endCorner = if (leftToRight) Offset(size.width, size.height) else Offset(0f, size.height)
  val angleRadians = if (leftToRight) 0.0 else PI
  drawDebug(gradientStart, gradientEnd, startCorner, endCorner, angleRadians)
}

private fun DrawScope.drawVerticalGradient(startsFromTop: Boolean, debug: Boolean) {
  val startY = if (startsFromTop) 0f else size.height
  val endY = if (startsFromTop) size.height else 0f
  val brush = Brush.verticalGradient(*colorStops, startY = startY, endY = endY)
  drawRect(brush)

  /////////////// VISUALIZATION /////////////////
  if (!debug) return

  val gradientStart = Offset(center.x, startY)
  val gradientEnd = Offset(center.x, endY)
  val startCorner = if (startsFromTop) Offset(size.width, 0f) else Offset(size.width, size.height)
  val endCorner = if (startsFromTop) Offset(0f, size.height) else Offset(0f, 0f)
  val angleRadians = if (startsFromTop) PI / 2 else -PI / 2
  drawDebug(gradientStart, gradientEnd, startCorner, endCorner, angleRadians)
}

private fun DrawScope.drawLinearGradient(angleDegrees: Double, debug: Boolean) {
  // Calculate the angle in radians
  val angleRadians = Math.toRadians(angleDegrees)

  // Determine the closest corners to the intersection points
  val normalizedAngle = (angleDegrees + 180) % 360.0
  val (startCorner, endCorner) = when {
    normalizedAngle < 90.0 -> Offset(size.width, size.height) to Offset(0f, 0f)
    normalizedAngle < 180.0 -> Offset(0f, size.height) to Offset(size.width, 0f)
    normalizedAngle < 270.0 -> Offset(0f, 0f) to Offset(size.width, size.height)
    else -> Offset(size.width, 0f) to Offset(0f, size.height)
  }

  val gradientStart = calculateProjection(center, angleRadians, startCorner)
  val gradientEnd = calculateProjection(center, angleRadians, endCorner)

  // We need to reverse gradient end and start points to get the intended effect
  val brush = Brush.linearGradient(
    colorStops = colorStops,
    start = gradientStart,
    end = gradientEnd,
  )

  drawRect(brush)

  /////////////// VISUALIZATION /////////////////
  if (!debug) return
  drawDebug(gradientStart, gradientEnd, startCorner, endCorner, angleRadians)
}

private fun DrawScope.drawDebug(
  gradientStart: Offset,
  gradientEnd: Offset,
  startCorner: Offset,
  endCorner: Offset,
  angleRadians: Double,
) {
  drawRect(Color.Gray, style = Stroke(1f))

  val center = size.center

  // Draw the zero-degree line and the center
  drawLine(
    color = Color.LightGray,
    start = center,
    end = Offset(size.width, center.y),
    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
  )
  drawCircle(Color.Black, 2f, center = center)

  // Draw the sector swept by the angle
  val radius = min(size.width / 4, size.height / 4)
  drawArc(
    Color.Cyan,
    0f,
    Math.toDegrees(angleRadians).toFloat(),
    false,
    topLeft = center - Offset(radius, radius),
    size = Size(radius * 2, radius * 2),
    style = Stroke(2f)
  )

  // Draw dashed projection lines
  drawPoints(
    points = listOf(gradientStart, startCorner),
    pointMode = PointMode.Lines,
    color = Color.Black,
    strokeWidth = 2f,
    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
  )
  drawPoints(
    points = listOf(gradientEnd, endCorner),
    pointMode = PointMode.Lines,
    color = Color.Black,
    strokeWidth = 2f,
    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
  )

  // Draw the gradient line
  drawLine(
    color = Color.Black,
    start = gradientStart,
    end = gradientEnd,
    strokeWidth = 2f
  )
  drawArrowHead(gradientEnd, angleRadians, Color.Black, strokeWidth = 2f)

  // Draw gradientEnd and gradientStart
  drawCircle(Color.Magenta, 4f, gradientStart)
  drawCircle(Color.Magenta, 6f, gradientEnd)

  // Draw reference corners
  drawCircle(Color.Red, 4f, startCorner)
  drawCircle(Color.Red, 4f, endCorner)

  // Draw where the gradient line intersects with the bounds
  if (abs(angleRadians) > angleEpsilon) {
    val deltaX = calculateDeltaX(center, angleRadians)
    val intersectionTop = Offset(center.x + deltaX, 0f)
    val intersectionBottom = Offset(center.x - deltaX, size.height)

    drawLine(
      color = Color.Black,
      start = intersectionTop,
      end = intersectionBottom,
      strokeWidth = 1f,
      pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
    )

    drawCircle(Color.Green, 4f, intersectionTop)
    drawCircle(Color.Green, 4f, intersectionBottom)
  }
}

private fun DrawScope.drawArrowHead(
  point: Offset,
  angleRadians: Double,
  color: Color,
  size: Int = 15,
  strokeWidth: Float = 1f,
  closedShape: Boolean = false,
) {
  // Calculate the two points of the arrowhead
  val angle = if (abs(angleRadians) >= angleEpsilon) angleRadians else 0.01
  val arrowHeadPoint1 = Offset(
    (point.x - size * cos(angle - Math.PI / 6)).toFloat(),
    (point.y - size * sin(angle - Math.PI / 6)).toFloat(),
  )
  val arrowHeadPoint2 = Offset(
    (point.x - size * cos(angle + Math.PI / 6)).toFloat(),
    (point.y - size * sin(angle + Math.PI / 6)).toFloat(),
  )

  // Draw the arrowhead
  drawPath(
    path = Path().apply {
      moveTo(arrowHeadPoint1.x, arrowHeadPoint1.y)
      lineTo(point.x, point.y)
      lineTo(arrowHeadPoint2.x, arrowHeadPoint2.y)
      if (closedShape) close()
    },
    color = color,
    style = if (closedShape) Fill else Stroke(width = strokeWidth)
  )
}

private fun calculateDeltaX(center: Offset, angleRadians: Double): Float =
  -(center.y * cotan((angleRadians))).toFloat()

private fun cotan(angleRadians: Double): Double {
  val sin = sin(angleRadians)
  if (sin == 0.0) return 0.0
  return cos(angleRadians) / sin
}


private fun calculateProjection(linePoint: Offset, angleRadians: Double, pointToProject: Offset): Offset {
  // Calculate slope from angle
  val m = tan(angleRadians)

  // Calculate y-intercept (b) using the point-slope form
  val b = linePoint.y - m * linePoint.x

  // Slope of the perpendicular line
  val mPerpendicular = -1.0 / m

  // Equation of the perpendicular line passing through pointToProject
  // y - y1 = m_perp (x - x1)
  // y = m_perp * (x - x1) + y1

  // Solve for intersection point (xp, yp)
  val xp = (b - pointToProject.x / m - pointToProject.y) / (mPerpendicular - m)
  val yp = m * xp + b

  return Offset(xp.toFloat(), yp.toFloat())
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

private val colorStops = arrayOf(
  0f to Color.Yellow,
  .001f to colors[0],
  stops[1] to colors[1],
  stops[2] to colors[2],
  stops[3] to colors[3],
  stops[4] to colors[4],
  stops[5] to colors[5],
  stops[6] to colors[6],
  stops[7] to colors[7],
  stops[8] to colors[8],
  .999f to colors[9],
  1f to Color.Green
)

private const val angleEpsilon = .001
