package com.fly.cusheaderviewpager

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.fly.cusheaderviewpager.singleton.SingletonHungry
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var act: Activity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        act = this
        setContentView(R.layout.activity_main)

        tv_custom_linearLayout.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_custom_linearLayout -> {
                startActivity(Intent(act, HeaderViewPagerActivity::class.java))
            }
        }
    }

    public fun singleton() {
        SingletonHungry.test()
    }
}
