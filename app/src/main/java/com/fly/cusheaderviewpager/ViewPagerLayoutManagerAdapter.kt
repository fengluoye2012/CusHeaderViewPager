package com.fly.cusheaderviewpager

import android.view.LayoutInflater
import android.view.ViewGroup
import com.fly.basemodule.BaseRecyclerViewAdapter
import com.fly.basemodule.CusViewHolder

class ViewPagerLayoutManagerAdapter(list: MutableList<String>) : BaseRecyclerViewAdapter<String>(list) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CusViewHolder {
        return CusViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_view_pager, parent, false))
    }

    override fun onBindViewHolder(holder: CusViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

    }
}