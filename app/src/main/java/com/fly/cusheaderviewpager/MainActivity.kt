package com.fly.cusheaderviewpager

import android.content.Intent
import android.view.View
import com.fly.basemodule.BaseActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity(), View.OnClickListener {
    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun initListeners() {
        super.initListeners()
        tv_custom_linearLayout.setOnClickListener(this)
    }


    override fun onClick(v: View) {
        startActivity(Intent(act, HeaderViewPagerActivity::class.java))
    }
}
