package com.arsvechkarev.letta.views.gradientpalette

import android.graphics.Rect
import android.graphics.RectF

class ValuesHolder(
  val swapperBounds: Rect,
  val swapperStrokeBounds: Rect,
  val gradientRectBounds: RectF,
  val rectRadius: Float,
  val startAnimAxis: Float,
  val endAnimAxis: Float,
  val radiusFloating: Float,
  val radiusSelected: Float,
  val currentAnimAxis: Float,
  val currentAnimRadius: Float,
  val currentAxisValue: Float,
  val minSizeHalf: Float,
  val bezierSpotStart: Float,
  val bezierSpotEnd: Float
)