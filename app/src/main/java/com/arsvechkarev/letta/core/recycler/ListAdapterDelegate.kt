package com.arsvechkarev.letta.core.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.reflect.KClass

abstract class ListAdapterDelegate<T : DifferentiableItem>(val modelClass: KClass<T>) {
  
  internal fun onBindViewHolderRaw(holder: RecyclerView.ViewHolder, item: DifferentiableItem) {
    onBindViewHolder(holder as DelegateViewHolder<T>, item as T)
  }
  
  abstract fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<T>
  
  open fun onBindViewHolder(holder: DelegateViewHolder<T>, item: T) {
    holder._item = item
    holder.bind(item)
  }
  
  open fun onAttachedToRecyclerView(recyclerView: RecyclerView) {}
  
  open fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {}
}