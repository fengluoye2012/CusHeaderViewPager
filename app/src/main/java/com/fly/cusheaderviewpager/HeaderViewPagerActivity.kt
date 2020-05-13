package com.fly.cusheaderviewpager

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter

import com.fly.headerviewpager.CusViewPager
import com.fly.headerviewpager.HeaderViewPager

class HeaderViewPagerActivity : AppCompatActivity() {

    private var headerViewPager: HeaderViewPager? = null
    private var viewPager: CusViewPager? = null
    private var itemFragment: ItemFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_header_view_pager)

        val textView = findViewById<TextView>(R.id.textView)
        headerViewPager = findViewById(R.id.headerViewPager)
        viewPager = findViewById(R.id.viewPager)
        itemFragment = ItemFragment()

        viewPager!!.adapter = object : FragmentPagerAdapter(supportFragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

            override fun getCount(): Int {
                return 1
            }

            override fun getItem(position: Int): Fragment {
                return itemFragment
            }
        }

        viewPager!!.currentItem = 0
        headerViewPager!!.setViewPager(viewPager)
        headerViewPager!!.setScrollableInterface(itemFragment)

        textView.setOnClickListener { v -> Toast.makeText(v.context, "hhh", Toast.LENGTH_SHORT).show() }
    }
}
