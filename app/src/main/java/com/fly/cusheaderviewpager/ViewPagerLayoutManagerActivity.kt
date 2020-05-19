package com.fly.cusheaderviewpager

import com.fly.basemodule.BaseActivity
import com.fly.layoutmanager.ViewPagerLayoutManager
import kotlinx.android.synthetic.main.activity_view_pager_layout_manager.*

class ViewPagerLayoutManagerActivity : BaseActivity() {
    override fun getLayoutId(): Int {
        return R.layout.activity_view_pager_layout_manager
    }

    override fun initData() {
        super.initData()
        recyclerView.layoutManager = ViewPagerLayoutManager(act)

    }

}