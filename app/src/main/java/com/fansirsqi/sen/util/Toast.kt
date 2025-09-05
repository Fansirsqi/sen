package com.fansirsqi.sen.util

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.highcapable.yukihookapi.hook.param.PackageParam

object Toast {

    // 模块自身 Application
    private val app: Application?
        get() = AppHolder.application

    // 自定义时长枚举
    enum class Duration(val value: Int) {
        SHORT(Toast.LENGTH_SHORT),
        LONG(Toast.LENGTH_LONG)
    }

    /**
     * 普通文本 Toast，Hook 环境优先使用 PackageParam Context
     */
    fun PackageParam.show(text: CharSequence, duration: Duration = Duration.SHORT) {
        val ctx = this.appContext ?: app
        ctx?.showToast(text, duration.value)
    }

    fun show(text: CharSequence, duration: Duration = Duration.SHORT) {
        app?.showToast(text, duration.value)
    }

    /**
     * 显示长文本，自动换行或者自定义 TextView
     */
    fun PackageParam.showLongText(text: CharSequence, duration: Duration = Duration.LONG) {
        val ctx = this.appContext ?: app
        ctx?.showLongTextToast(text, duration.value)
    }

    fun showLongText(text: CharSequence, duration: Duration = Duration.LONG) {
        app?.showLongTextToast(text, duration.value)
    }

    // 内部普通 Toast 方法
    private fun Context.showToast(text: CharSequence, duration: Int) {
        Handler(Looper.getMainLooper()).post {
            try {
                Toast.makeText(this, text, duration).show()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    // 内部长文本 Toast 方法
    private fun Context.showLongTextToast(text: CharSequence, duration: Int) {
        Handler(Looper.getMainLooper()).post {
            try {
                val tv = TextView(this)
                tv.text = text
                tv.setPadding(32, 16, 32, 16)
                tv.gravity = Gravity.CENTER
                // 使用 makeText 创建 Toast，再调用 apply 设置 view
                val toast = Toast.makeText(this, "", duration).apply {
                    // ⚠️ 这里还是 setView(tv)，API过时，但可以用
                    this.view = tv
                }
                toast.show()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }


    // 获取模块 Application 的 Holder
    @SuppressLint("PrivateApi")
    private object AppHolder {
        val application: Application? by lazy {
            try {
                val activityThreadClass = Class.forName("android.app.ActivityThread")
                val thread = activityThreadClass.getMethod("currentActivityThread").invoke(null)
                activityThreadClass.getMethod("getApplication").invoke(thread) as? Application
            } catch (e: Throwable) {
                e.printStackTrace()
                null
            }
        }
    }

}
