package com.fly.cusheaderviewpager

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fly.basemodule.BaseFragment
import com.fly.cusheaderviewpager.dummy.DummyContent
import com.fly.headerviewpager.ScrollableInterface
import kotlinx.android.synthetic.main.fragment_item_list.*


class ItemFragment : BaseFragment(), ScrollableInterface {

    /**
     * lateinit 延迟加载
     * lateinit 只能修饰变量var，不能修饰常量val
     * lateinit 不能对可空类型使用
     * lateinit 不能对java基本类型使用，例如：Double、Int、Long等
     * 在调用lateinit修饰的变量时，如果变量还没有初始化，则会抛出未初始化异常，报错
     */
    override fun getLayId(): Int {
        return R.layout.fragment_item_list
    }

    override fun initData() {
        super.initData()
        // Set the adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = MyItemRecyclerViewAdapter(DummyContent.ITEMS)
    }

    override val scrollableView: View
        get() = recyclerView

}
