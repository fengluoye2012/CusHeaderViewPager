package com.fly.cusheaderviewpager

import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.fly.basemodule.BaseActivity
import kotlinx.android.synthetic.main.activity_header_view_pager.*

class HeaderViewPagerActivity : BaseActivity() {
    override fun getLayoutId(): Int {
        return R.layout.activity_header_view_pager
    }

    private lateinit var itemFragment: ItemFragment


    override fun initListeners() {
        super.initListeners()
        textView.setOnClickListener { v -> Toast.makeText(v.context, "hhh", Toast.LENGTH_SHORT).show() }
    }

    override fun initData() {
        super.initData()
        itemFragment = ItemFragment()
        viewPager.adapter = object : FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getCount(): Int {
                return 1
            }

            override fun getItem(position: Int): Fragment {
                return itemFragment
            }
        }

        viewPager.currentItem = 0
        headerViewPager.setViewPager(viewPager)
        headerViewPager.setScrollableInterface(itemFragment)
    }
}
