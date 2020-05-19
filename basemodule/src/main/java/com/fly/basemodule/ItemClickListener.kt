package com.fly.basemodule

/**
 * 接口
 */
interface ItemClickListener {
    fun onItemClick(pos: Int)
    fun onViewClick(pos: Int, id: Int)
}