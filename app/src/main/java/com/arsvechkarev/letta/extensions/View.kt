package com.arsvechkarev.letta.extensions

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DimenRes
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

operator fun View.contains(event: MotionEvent): Boolean {
  val x = event.x
  val y = event.y
  return x >= left && y >= top && x <= right && y <= bottom
}

infix fun MotionEvent.happenedIn(view: View): Boolean {
  return x >= 0 && y >= 0 && x <= view.width && y <= view.height
}

fun View.visible() {
  visibility = View.VISIBLE
}

fun View.invisible() {
  visibility = View.INVISIBLE
}

fun View.gone() {
  visibility = View.INVISIBLE
}

val View.isNotVisible get() = visibility != View.VISIBLE

val View.isNotGone get() = visibility != View.GONE

fun View.setSafeClickListener(block: () -> Unit) {
  setOnClickListener {
    block()
    isClickable = false
    handler.postDelayed({ isClickable = true }, 1000)
  }
}

fun ViewGroup.inflate(@LayoutRes layoutRes: Int): View {
  return LayoutInflater.from(context).inflate(layoutRes, this, false)
}

inline fun View.findParent(predicate: (View) -> Boolean): View {
  var parent = parent as? View
  while (parent != null) {
    if (predicate(parent)) {
      return parent
    }
    parent = parent.parent as? View
  }
  throw IllegalStateException("No parent matching predicate")
}

inline fun ViewGroup.findChild(predicate: (View) -> Boolean): View {
  for (i in 0 until childCount) {
    val child = getChildAt(i)
    if (predicate(child)) {
      return child
    }
  }
  throw IllegalStateException("No child matching predicate")
}

val View.totalWidth: Int
  get() {
    val params = layoutParams as ViewGroup.MarginLayoutParams
    return params.width + params.rightMargin + params.leftMargin
  }

val View.totalHeight: Int
  get() {
    val params = layoutParams as ViewGroup.MarginLayoutParams
    return params.height + params.topMargin + params.bottomMargin
  }

inline fun <reified T : CoordinatorLayout.Behavior<*>> View.getBehavior(): T {
  return (layoutParams as CoordinatorLayout.LayoutParams).behavior as T
}

inline fun <reified T : CoordinatorLayout.Behavior<*>> View.hasBehavior(): Boolean {
  return (layoutParams as? CoordinatorLayout.LayoutParams)?.behavior as? T != null
}

fun <V : View> V.withParams(
  @DimenRes width: Int,
  @DimenRes height: Int,
  @DimenRes marginRes: Int
): V {
  val params = ViewGroup.MarginLayoutParams(
    context.getDimen(width).toInt(),
    context.getDimen(height).toInt()
  )
  val margin = context.getDimen(marginRes).toInt()
  params.setMargins(margin, margin, margin, margin)
  layoutParams = params
  return this
}

@OptIn(ExperimentalContracts::class)
inline fun View.withParams(block: View.(ViewGroup.MarginLayoutParams) -> Unit) {
  contract {
    callsInPlace(block, InvocationKind.EXACTLY_ONCE)
  }
  block.invoke(this, layoutParams as ViewGroup.MarginLayoutParams)
}

fun View.layoutNormal(left: Int, top: Int, right: Int, bottom: Int) {
  layout(left, top, right, bottom)
}

fun View.layoutWithLeftTop(left: Int, top: Int, params: ViewGroup.LayoutParams) {
  layout(left, top, left + params.width, top + params.height)
}

fun View.paddings(
  start: Int = 0,
  top: Int = 0,
  end: Int = 0,
  bottom: Int = 0
) {
  if (isLayoutLeftToRight) {
    setPadding(start, top, end, bottom)
  } else {
    setPadding(end, top, start, bottom)
  }
}

fun View.childView(tag: String): View {
  return findViewWithTag(tag)
}

fun View.childImageView(tag: String): ImageView {
  return findViewWithTag(tag) as ImageView
}

fun <T : View> View.childViewAs(tag: String): T {
  return findViewWithTag(tag)
}