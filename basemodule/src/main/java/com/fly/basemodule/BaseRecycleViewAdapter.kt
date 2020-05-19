package com.fly.basemodule

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * 类的修饰符：默认情况下使用final 修饰  不可被继承
 * abstract    // 抽象类
 * final       // 类不可继承，默认属性
 * enum        // 枚举类
 * open        // 类可继承，类默认是final的
 * annotation  // 注解类
 *
 * 方法的修饰符和Java 类似
 *
 * private    // 仅在同一个文件中可见
 * protected  // 同一个文件中或子类可见
 * public     // 所有调用的地方都可见
 * internal   // 同一个模块中可见
 */
abstract class BaseRecyclerViewAdapter<T> constructor() : RecyclerView.Adapter<CusViewHolder>() {

    private var list: MutableList<T>? = null


    public constructor(list: MutableList<T>) : this() {
        this.list = list
    }

    init {
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    /**
     * 根据pos获取对应的实体类
     */
    fun getPosBean(pos: Int): T? {
        return if (pos >= 0 && pos < list?.size ?: 0) {
            list?.get(pos)
        } else {
            null
        }
    }

    override fun onBindViewHolder(holder: CusViewHolder, position: Int) {
        //条目点击事件
        holder.itemView.setOnClickListener(View.OnClickListener {
            itemClickListener?.onItemClick(position)
        })
    }

    public fun setData(list: MutableList<T>) {
        this.list = list
        notifyDataSetChanged()
    }

    public fun append(list: MutableList<T>) {
        var startPos: Int = this.list?.size ?: 0
        this.list?.addAll(list)
        notifyItemRangeInserted(startPos, list.size)
    }

    //条目点击事件
    public var itemClickListener: ItemClickListener? = null
        set(value) {
            if (value != null) {
                field = value
            }
        }
}