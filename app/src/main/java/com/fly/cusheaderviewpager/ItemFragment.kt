package com.fly.cusheaderviewpager

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.fly.cusheaderviewpager.dummy.DummyContent
import com.fly.headerviewpager.ScrollableInterface


class ItemFragment : Fragment(), ScrollableInterface {

    private val mColumnCount = 1

    private var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            val context = view.getContext()
            recyclerView = view
            if (mColumnCount <= 1) {
                recyclerView!!.layoutManager = LinearLayoutManager(context)
            } else {
                recyclerView!!.layoutManager = GridLayoutManager(context, mColumnCount)
            }
            recyclerView!!.adapter = MyItemRecyclerViewAdapter(DummyContent.ITEMS)
        }
        return view
    }

    override fun getScrollableView(): View? {
        return recyclerView
    }

}
