package com.fly.cusheaderviewpager

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import kotlinx.android.synthetic.main.activity_header_view_pager.*

class HeaderViewPagerActivity : AppCompatActivity() {

    private var itemFragment: ItemFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_header_view_pager)

        itemFragment = ItemFragment()

        viewPager.adapter = object : FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

            override fun getCount(): Int {
                return 1
            }

            override fun getItem(position: Int): Fragment {
                return itemFragment as ItemFragment
            }
        }

        viewPager.currentItem = 0
        headerViewPager.setViewPager(viewPager)
        headerViewPager.setScrollableInterface(itemFragment!!)

        textView.setOnClickListener { v -> Toast.makeText(v.context, "hhh", Toast.LENGTH_SHORT).show() }
    }
}
