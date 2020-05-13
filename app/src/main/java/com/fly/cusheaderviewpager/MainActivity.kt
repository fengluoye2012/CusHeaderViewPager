package com.fly.cusheaderviewpager

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var tvCustomLinearLayout: TextView? = null
    private var act: Activity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        act = this
        setContentView(R.layout.activity_main)

        tvCustomLinearLayout = findViewById(R.id.tv_custom_linearLayout)
        tvCustomLinearLayout!!.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        startActivity(Intent(act, HeaderViewPagerActivity::class.java))
    }
}
