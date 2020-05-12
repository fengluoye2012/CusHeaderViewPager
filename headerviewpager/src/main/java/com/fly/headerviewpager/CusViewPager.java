package com.fly.headerviewpager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class CusViewPager extends ViewPager {

    /**
     * 是否允许ViewPager拦截事件
     */
    private boolean allowIntercept;

    public CusViewPager(@NonNull Context context) {
        super(context);
    }

    public CusViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!allowIntercept) {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }


    public void setAllowIntercept(boolean allowIntercept) {
        this.allowIntercept = allowIntercept;
    }
}
