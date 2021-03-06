package com.arsvechkarev.letta.core.recycler

import androidx.recyclerview.widget.RecyclerView

/**
 * Item to be displayed in [RecyclerView]
 */
interface DifferentiableItem {
  
  /**
   * Id to distinguish two different elements
   */
  val id: Long
  
  /**
   * Every class inherits from [DifferentiableItem] should override equals in order to compare elements
   * properly
   */
  override fun equals(other: Any?): Boolean
}