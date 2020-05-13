package com.fly.headerviewpager

import android.util.SparseArray
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class CusViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {

    fun <T : View> findView(viewId: Int): T {
        return mView.findViewOften(viewId)
    }

    private fun <T> View.findViewOften(viewId: Int): T {
        var viewHolder: SparseArray<View> = tag as?SparseArray<View> ?: SparseArray()
        tag = viewHolder
        var childView: View? = viewHolder.get(viewId)
        if (null == childView) {
            childView = findViewById(viewId)
            viewHolder.put(viewId, childView)
        }
        return childView as T
    }
}