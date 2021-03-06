package com.arsvechkarev.letta.views.behaviors

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_CANCEL
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_POINTER_DOWN
import android.view.MotionEvent.ACTION_POINTER_UP
import android.view.MotionEvent.ACTION_UP
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.arsvechkarev.letta.core.INVALID_POINTER
import com.arsvechkarev.letta.extensions.doOnEnd
import com.arsvechkarev.letta.extensions.getBehavior
import com.arsvechkarev.letta.views.behaviors.BottomSheetBehavior.State.HIDDEN
import com.arsvechkarev.letta.views.behaviors.BottomSheetBehavior.State.SHOWN
import kotlin.math.abs

class BottomSheetBehavior(context: Context, attrs: AttributeSet) :
  CoordinatorLayout.Behavior<View>() {
  
  private val offsetListeners = ArrayList<((percentageOpened: Float) -> Unit)>()
  private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
  private val maxFlingVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity
  private var activePointerId = INVALID_POINTER
  private var isBeingDragged = false
  private var latestY = -1
  private var velocityTracker: VelocityTracker? = null
  private var bottomSheetOffsetHelper: BottomSheetOffsetHelper? = null
  private var currentState = HIDDEN
  private var bottomSheet: View? = null
  private var parentHeight = 0
  private var slideRange = 0
  private val slideAnimator = ValueAnimator().apply {
    addUpdateListener {
      val value = it.animatedValue as Int
      bottomSheetOffsetHelper!!.updateTop(value)
    }
  }
  
  val state get() = currentState
  
  var onHide: () -> Unit = {}
  var onShow: () -> Unit = {}
  
  fun show() {
    bottomSheet!!.post {
      if (currentState == SHOWN || slideAnimator.isRunning) return@post
      slideAnimator.duration = DURATION_SLIDE
      slideAnimator.setIntValues(bottomSheet!!.top, parentHeight - slideRange)
      slideAnimator.doOnEnd { currentState = SHOWN; onShow() }
      slideAnimator.start()
    }
  }
  
  fun hide() {
    bottomSheet!!.post {
      if (currentState == HIDDEN || slideAnimator.isRunning) return@post
      slideAnimator.duration = DURATION_SLIDE
      slideAnimator.setIntValues(bottomSheet!!.top, parentHeight)
      slideAnimator.doOnEnd { currentState = HIDDEN; onHide() }
      slideAnimator.start()
    }
  }
  
  fun hideWithoutAnimation() {
    ViewCompat.offsetTopAndBottom(bottomSheet!!, slideRange)
    currentState = HIDDEN
  }
  
  fun addSlideListener(listener: (percentageOpened: Float) -> Unit) {
    offsetListeners.add(listener)
  }
  
  override fun onLayoutChild(
    parent: CoordinatorLayout,
    child: View,
    layoutDirection: Int
  ): Boolean {
    val top: Int
    when (currentState) {
      HIDDEN -> top = parent.height
      SHOWN -> top = parent.height - child.measuredHeight
    }
    child.layout(0, top, parent.width, top + child.measuredHeight)
    bottomSheet = child
    parentHeight = parent.height
    slideRange = child.measuredHeight
    bottomSheetOffsetHelper?.release()
    bottomSheetOffsetHelper = BottomSheetOffsetHelper(
      child, parentHeight, parentHeight - child.measuredHeight, ::notifyOffsetListeners,
    )
    return true
  }
  
  override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: View, event: MotionEvent): Boolean {
    if (slideAnimator.isRunning) return false
    val action = event.action
    if (action == ACTION_MOVE && isBeingDragged) {
      return true
    }
    when (event.actionMasked) {
      ACTION_DOWN -> {
        isBeingDragged = false
        val x = event.x.toInt()
        val y = event.y.toInt()
        if (parent.isPointInChildBounds(child, x, y)) {
          activePointerId = event.getPointerId(0)
          latestY = y
          ensureVelocityTracker()
        }
      }
      ACTION_POINTER_DOWN -> {
        activePointerId = event.getPointerId(event.actionIndex)
        latestY = event.getY(event.actionIndex).toInt()
      }
      ACTION_MOVE -> {
        val pointerIndex = event.findPointerIndex(activePointerId)
        if (pointerIndex == -1) {
          return false
        }
        val y = event.getY(pointerIndex).toInt()
        val yDiff = abs(y - latestY)
        if (yDiff > touchSlop) {
          isBeingDragged = true
          latestY = y
        }
      }
      ACTION_POINTER_UP -> {
        onPointerUp(event)
      }
      ACTION_CANCEL, ACTION_UP -> {
        endTouch()
      }
    }
    velocityTracker?.addMovement(event)
    return isBeingDragged
  }
  
  override fun onTouchEvent(parent: CoordinatorLayout, child: View, event: MotionEvent): Boolean {
    if (slideAnimator.isRunning) return false
    when (event.actionMasked) {
      ACTION_DOWN -> {
        val x = event.x.toInt()
        val y = event.y.toInt()
        if (parent.isPointInChildBounds(child, x, y)) {
          latestY = y
          activePointerId = event.getPointerId(0)
          ensureVelocityTracker()
        }
      }
      ACTION_POINTER_DOWN -> {
        activePointerId = event.getPointerId(event.actionIndex)
        latestY = event.getY(event.actionIndex).toInt()
      }
      ACTION_MOVE -> {
        val pointerIndex = event.findPointerIndex(activePointerId)
        if (pointerIndex == -1) {
          return true
        }
        val y = event.getY(pointerIndex).toInt()
        var dy = y - latestY
        if (!isBeingDragged && abs(dy) > touchSlop) {
          isBeingDragged = true
          if (dy > 0) {
            dy -= touchSlop
          } else {
            dy += touchSlop
          }
        }
        if (isBeingDragged) {
          latestY = y
          // We're being dragged so scroll the view
          updateDyOffset(dy)
        }
      }
      ACTION_POINTER_UP -> {
        onPointerUp(event)
      }
      ACTION_UP -> {
        handleUpEvent(event)
      }
      ACTION_CANCEL -> {
        endTouch()
      }
    }
    velocityTracker?.addMovement(event)
    return true
  }
  
  private fun onPointerUp(event: MotionEvent) {
    val actionIndex = event.actionIndex
    if (event.getPointerId(actionIndex) == activePointerId) {
      val newIndex = if (actionIndex == 0) 1 else 0
      activePointerId = event.getPointerId(newIndex)
      latestY = event.getY(newIndex).toInt()
    }
  }
  
  private fun handleUpEvent(event: MotionEvent) {
    velocityTracker?.addMovement(event)
    velocityTracker?.computeCurrentVelocity(1000)
    val yVelocity = velocityTracker?.getYVelocity(activePointerId) ?: 0f
    if (yVelocity / maxFlingVelocity > FLING_VELOCITY_THRESHOLD) {
      currentState = HIDDEN
      val timeInSeconds = abs((parentHeight - bottomSheet!!.top) / yVelocity)
      slideAnimator.duration = (timeInSeconds * 1000).toLong()
      slideAnimator.setIntValues(bottomSheet!!.top, parentHeight)
      slideAnimator.doOnEnd(onHide)
      slideAnimator.start()
    } else {
      val middlePoint = parentHeight - slideRange * 0.65
      val endY = if (bottomSheet!!.top < middlePoint) {
        currentState = SHOWN
        slideAnimator.doOnEnd(onShow)
        parentHeight - slideRange
      } else {
        currentState = HIDDEN
        slideAnimator.doOnEnd(onHide)
        parentHeight
      }
      slideAnimator.setIntValues(bottomSheet!!.top, endY)
      slideAnimator.duration = DURATION_SLIDE
      slideAnimator.start()
    }
    endTouch()
  }
  
  private fun updateDyOffset(dy: Int) {
    bottomSheetOffsetHelper!!.updateDyOffset(dy)
  }
  
  private fun ensureVelocityTracker() {
    if (velocityTracker == null) {
      velocityTracker = VelocityTracker.obtain()
    }
  }
  
  private fun endTouch() {
    isBeingDragged = false
    activePointerId = INVALID_POINTER
    velocityTracker?.recycle()
    velocityTracker = null
  }
  
  private fun notifyOffsetListeners(percentageOpened: Float) {
    offsetListeners.forEach { it.invoke(percentageOpened) }
  }
  
  enum class State {
    SHOWN, HIDDEN
  }
  
  companion object {
    
    private const val DURATION_SLIDE = 225L
    private const val FLING_VELOCITY_THRESHOLD = 0.18f
    
    val View.asBottomSheet get() = getBehavior<BottomSheetBehavior>()
  }
}