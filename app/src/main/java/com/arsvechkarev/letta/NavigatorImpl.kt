package com.arsvechkarev.letta

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.arsvechkarev.letta.core.NavigableFragment
import com.arsvechkarev.letta.core.Navigator
import com.arsvechkarev.letta.features.drawing.presentation.DrawingFragment
import com.arsvechkarev.letta.features.projects.presentation.ProjectsListFragment

class NavigatorImpl(private var activity: MainActivity?) : Navigator {
  
  fun init(savedInstantState: Bundle?) {
    if (savedInstantState == null) {
      openProjectsList()
    }
  }
  
  private fun openProjectsList() {
    goToFragment(ProjectsListFragment(), animate = false, addToBackStack = false)
  }
  
  fun allowPressBack(): Boolean {
    val fragments = activity!!.supportFragmentManager.fragments
    var resultFragment: NavigableFragment? = null
    for (fragment in fragments) {
      // Finding last fragment that visible, since fragments in fragment manager located
      // in the order they were added
      if (fragment.isVisible) {
        resultFragment = fragment as NavigableFragment
      }
    }
    return resultFragment?.onBackPressed() ?: true
  }
  
  fun onDestroy() {
    activity = null
  }
  
  override fun openProject(arguments: Bundle) {
    val fragment = DrawingFragment().apply { this.arguments = arguments }
    goToFragment(fragment, animate = true, addToBackStack = true)
  }
  
  private fun goToFragment(
    fragment: NavigableFragment,
    animate: Boolean = true,
    addToBackStack: Boolean = false
  ) {
    val transaction = activity!!.supportFragmentManager.beginTransaction()
    if (animate) {
      transaction.setCustomAnimations(
        R.anim.transition_fragment_enter,
        R.anim.transition_fragment_exit,
        R.anim.transition_fragment_pop_enter,
        R.anim.transition_fragment_pop_exit
      )
    }
    transaction.add(R.id.fragmentContainer, fragment as Fragment)
    if (addToBackStack) {
      transaction.addToBackStack(null)
    }
    transaction.commit()
  }
}