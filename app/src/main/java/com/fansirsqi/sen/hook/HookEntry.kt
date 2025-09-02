package com.fansirsqi.sen.hook

import android.app.AndroidAppHelper.currentProcessName
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit

@InjectYukiHookWithXposed(entryClassName = "sen", isUsingResourcesHook = true)
class HookEntry : IYukiHookXposedInit {

    override fun onInit() = configs {
        debugLog {
            tag = "sen"
        }
    }

    override fun onHook() = encase {
        // Your code here.
        loadApp ("com.eg.android.AlipayGphone")
    }
}