package com.fansirsqi.sen.util

import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.PackageParam
import org.luckypray.dexkit.DexKitBridge

/**
 * DexKit 工具类
 */
object DexkitHelper {
    /** 是否已装载 */
    private var isLoaded = false

    /** 装载 */
    fun load() {
        if (isLoaded) return
        runCatching {
            System.loadLibrary("dexkit")
            isLoaded = true
        }.onFailure { YLog.error("Load DexKit failed!", it) }
    }

    /**
     * 创建 [DexKitBridge]
     * @param initiate 方法体
     */
    fun create(param: PackageParam, initiate: DexKitBridge.() -> Unit) {
        load()
        runCatching {
            YLog.debug("Create DexKitBridge...")
            DexKitBridge.create(param.appInfo.sourceDir).use {
                initiate(it)
            }
        }
    }
}