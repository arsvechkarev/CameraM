package com.arsvechkarev.letta.editing

import android.view.View
import android.view.View.TRANSLATION_X
import android.widget.ImageButton
import com.arsvechkarev.letta.R
import com.arsvechkarev.letta.animations.animateFadeIn
import com.arsvechkarev.letta.animations.animateFadeOut
import com.arsvechkarev.letta.utils.animate
import com.arsvechkarev.letta.views.DrawingCanvas
import com.arsvechkarev.letta.views.GradientPalette
import com.arsvechkarev.letta.views.ShowerCircle
import com.arsvechkarev.letta.views.VerticalSeekbar

class PaintContainer(
  view: View,
  private val drawingCanvas: DrawingCanvas
) : Container(view) {
  
  private var palette: GradientPalette = findViewById(R.id.palette)
  private var verticalSeekbar: VerticalSeekbar = findViewById(R.id.verticalSeekbar)
  private var buttonBack: ImageButton = findViewById(R.id.buttonBack)
  private var showerCircle: ShowerCircle = findViewById(R.id.showCircle)
  
  init {
    verticalSeekbar.onPercentChanged = {
      drawingCanvas.changePaintWidth(it * 30)
      showerCircle.draw(palette.currentColor, it * 30)
    }
    verticalSeekbar.onUp = {
      showerCircle.erase()
    }
    palette.onColorChanged = {
      drawingCanvas.setPaintColor(it)
    }
    buttonBack.setOnClickListener {
      drawingCanvas.undo()
    }
  }
  
  override fun animateEnter() {
    view.post {
      buttonBack.animateFadeIn()
      buttonBack.animate(TRANSLATION_X, -buttonBack.width / 2f, 0f)
      verticalSeekbar.animateFadeIn()
      verticalSeekbar.animate(TRANSLATION_X, -verticalSeekbar.width / 2f, 0f)
      palette.animateFadeIn()
      palette.animate(TRANSLATION_X, palette.width / 2f, 0f)
    }
  }
  
  override fun animateExit(andThen: () -> Unit) {
    buttonBack.animateFadeOut()
    buttonBack.animate(TRANSLATION_X, -buttonBack.width / 2f)
    verticalSeekbar.animateFadeOut()
    verticalSeekbar.animate(TRANSLATION_X, -verticalSeekbar.width / 2f)
    palette.animateFadeOut(andThen)
    palette.animate(TRANSLATION_X, palette.width / 2f, onEnd = {
      andThen()
    })
  }
}