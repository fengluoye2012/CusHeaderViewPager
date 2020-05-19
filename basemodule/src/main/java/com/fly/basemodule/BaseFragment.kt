package com.fly.basemodule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(getLayId(), container, false)
        initListeners()
        initData()
        return view
    }

    protected abstract fun getLayId(): Int

    open fun initListeners() {
    }

    open fun initData() {
    }
}