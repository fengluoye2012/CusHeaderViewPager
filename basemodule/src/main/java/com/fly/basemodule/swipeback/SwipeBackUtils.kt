package com.fly.basemodule.swipeback

import android.app.Activity
import android.os.Build
import android.util.Log
import com.blankj.utilcode.util.LogUtils
import java.lang.Exception
import java.lang.reflect.Method

/**
 * 工具类
 */
class SwipeBackUtils private constructor() {

    //静态方法
    companion object {
        public fun convertActivityFromTranslucent(activity: Activity) {
            try {
                val method: Method = Activity::class.java.getDeclaredMethod("convertFromTranslucent")
                method.isAccessible = true
                method.invoke(activity)
            } catch (e: Exception) {
                LogUtils.e(Log.getStackTraceString(e))
            }
        }

        public fun convertActivityToTranslucent(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            } else {

            }
        }

        public fun convertActivityToTranslucentBeforeL(activity: Activity) {
            try {
                val classes = Activity::class.java.declaredClasses
                var translucentConversionListenerClazz: Class<*>? = null

                for (clazz: Class<*> in classes) {
                    if (clazz.simpleName.contains("TranslucentConversionListener")) {
                        translucentConversionListenerClazz = clazz
                        break
                    }
                }

                val method = Activity::class.java.getDeclaredMethod("convertToTranslucent", translucentConversionListenerClazz)
                method.isAccessible = true
                method.invoke(activity)
            } catch (e: Exception) {
                LogUtils.e(Log.getStackTraceString(e))
            }
        }


    }

}