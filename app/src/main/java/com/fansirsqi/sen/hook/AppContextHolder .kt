package com.fansirsqi.sen.hook

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object AppContextHolder {
    var context: Context? = null
    var hooked: Boolean = false
    var classLoader: ClassLoader? = null
}