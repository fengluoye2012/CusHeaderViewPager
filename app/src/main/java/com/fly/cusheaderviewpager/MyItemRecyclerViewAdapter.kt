package com.fly.cusheaderviewpager

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.fly.basemodule.CusViewHolder
import com.fly.cusheaderviewpager.dummy.DummyContent.DummyItem


class MyItemRecyclerViewAdapter(private val mValues: List<DummyItem>) : RecyclerView.Adapter<CusViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CusViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_item, parent, false)
        return CusViewHolder(view)
    }

    override fun onBindViewHolder(holder: CusViewHolder, position: Int) {
        holder.findView<TextView>(R.id.item_number).text = mValues[position].id
        holder.findView<TextView>(R.id.content).text = mValues[position].content

        holder.mView.setOnClickListener { v ->
            Toast.makeText(v.context, "我是$position", Toast.LENGTH_LONG).show()
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }


}

