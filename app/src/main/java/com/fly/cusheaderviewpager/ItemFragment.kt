package com.fly.cusheaderviewpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fly.cusheaderviewpager.dummy.DummyContent
import com.fly.headerviewpager.ScrollableInterface


class ItemFragment : Fragment(), ScrollableInterface {

    private val mColumnCount = 1

    /**
     * lateinit 延迟加载
     * lateinit 只能修饰变量var，不能修饰常量val
     * lateinit 不能对可空类型使用
     * lateinit 不能对java基本类型使用，例如：Double、Int、Long等
     * 在调用lateinit修饰的变量时，如果变量还没有初始化，则会抛出未初始化异常，报错
     */
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            val context = view.getContext()
            recyclerView = view
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = MyItemRecyclerViewAdapter(DummyContent.ITEMS)
        }
        return view
    }

    override val scrollableView: View
        get() = recyclerView

}
