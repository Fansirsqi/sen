package com.fansirsqi.sen.hook

import android.content.Context
import com.fansirsqi.sen.BuildConfig
import com.fansirsqi.sen.util.AlipayServiceHelper.getUserId
import com.fansirsqi.sen.util.AlipayServiceHelper.getUserObject
import com.fansirsqi.sen.util.AlipayServiceHelper.listAllFields
import com.fansirsqi.sen.util.AlipayServiceHelper.otherServiceHook
import com.fansirsqi.sen.util.Log
import com.fansirsqi.sen.util.PackageHelper
import com.fansirsqi.sen.util.Sender
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

@InjectYukiHookWithXposed(entryClassName = "sen", isUsingResourcesHook = true)
class HookEntry : IYukiHookXposedInit {
    private val tag = "sen"
    val rpcHookMap = ConcurrentHashMap<Any, Array<Any?>>()


    override fun onInit() = configs {
        isDebug = BuildConfig.DEBUG
        debugLog {
            tag = "sen"
            isEnable = isDebug
            elements(TAG, PRIORITY, PACKAGE_NAME, USER_ID)
        }
        isEnableModuleAppResourcesCache = true

    }

    override fun onHook() = encase {
        loadApp(name = PackageHelper.TARGET_PACKAGE) {
            if (AppContextHolder.hooked) {
                YLog.debug("already hooked ", tag = tag)
                return@encase
            } else {
                hookAttach()
                matchVersionHook()
                hookonResume()
                hookonCreate()
                hookonDestroy()
            }
        }
    }

    private fun PackageParam.hookAttach() {
        YLog.debug("start hooked attach", tag = tag)
        "android.app.Application".toClass().resolve().apply {
            firstMethod {
                name = "attach"
                parameters(Context::class.java)
            }.hook {
                after {
                    val appContext = args[0] as Context
                    AppContextHolder.context = appContext
                    AppContextHolder.classLoader = appClassLoader
                    val ver = appContext.packageManager?.getPackageInfo(packageName, 0)?.versionName
//                    Toast.makeText(appContext, "sen load", Toast.LENGTH_SHORT).show()
                    YLog.debug("[attach] Alipay version: $ver", tag = tag)
                    hookAccountLimit(ver)
                }
            }
            hookRpcBridgeExtension(isDebug = BuildConfig.DEBUG, debugUrl = "http://192.168.1.100:8080/sen/hook")
            hookDefaultBridgeCallback()
        }
        YLog.debug("attach hooked fun end", tag = tag)
    }


    private fun PackageParam.matchVersionHook() {
        "com.alipay.mobile.nebulaappproxy.api.rpc.H5AppRpcUpdate".toClass().resolve().apply {
            firstMethod {
                name = "matchVersion"
                parameters("com.alipay.mobile.h5container.api.H5Page", Map::class.java, String::class.java)
            }.hook().replaceToFalse()
        }
    }

    private fun PackageParam.hookonResume() {
        "com.alipay.mobile.quinox.LauncherActivity".toClass().resolve().apply {
            firstMethod {
                name = "onResume"
                emptyParameters()
            }.hook {
                after {
                    YLog.debug("onResume hook", tag = tag)
                    val userId = appClassLoader?.let { getUserId() }
                    YLog.debug("userId = $userId", tag = tag)
                    listAllFields()
                }
            }
        }
    }

    /** 在 attach 之后调用；用目标 app 的 classLoader 创建 Class<Any> 再 resolve */
    private fun PackageParam.hookAccountLimit(ver: String? = null) {

        when (ver) {
            "10.6.66.8000" -> {
                YLog.debug("hookAccountLimit start version is $ver", tag = tag)
                "com.alipay.mobile.security.accountmanager.b.a".toClass().resolve().apply {
                    firstMethod {
                        name = "getCount"
                        emptyParameters()
                    }.hook {
                        after {
                            val old = result
                            result = 30
                            YLog.debug("fuck the limit of $old to $result")
                        }
                    }
                }
            }

            "10.7.66.8000" -> {
                YLog.debug("hookAccountLimit start version is $ver", tag = tag)
                "com.alipay.mobile.security.accountmanager.data.AccountManagerListAdapter".toClass().resolve().apply {
                    firstMethod {
                        name = "getCount"
                        emptyParameters()
                    }.hook {
                        after {
                            val old = result
                            result = 30
                            YLog.debug("fuck the limit of $old to $result")
                        }
                    }
                }
            }

            else -> {
                YLog.debug("hookAccountLimit not support this version")
            }
        }
        YLog.debug("hookAccountLimit end", tag = tag)
    }


    private fun PackageParam.hookonCreate() {
        "android.app.Service".toClass().resolve().apply {
            firstMethod {
                name = "onCreate"
                emptyParameters()
            }.hook {
                after {
                    val service = instance as android.app.Service
                    if (service.javaClass.name != PackageHelper.CURRENT_USING_SERVICE) {
                        return@after
                    } else {
                        YLog.debug("onCreate hook $this", tag = tag)
                    }
                }

            }
        }
    }


    private fun PackageParam.hookonDestroy() {
        "android.app.Service".toClass().resolve().apply {
            firstMethod {
                name = "onDestroy"
            }.hook {
                after {
                    val service = instance as android.app.Service
                    if (service.javaClass.name != PackageHelper.CURRENT_USING_SERVICE) {
                        return@after
                    } else {
                        YLog.debug("onDestroy hook $this", tag = tag)
                    }
                    otherServiceHook()
                }
            }
        }
    }


    fun PackageParam.hookRpcBridgeExtension(isDebug: Boolean, debugUrl: String?) {
        try {
            val className = "com.alibaba.ariver.commonability.network.rpc.RpcBridgeExtension".toClass().resolve()
            // Hook 两个重载
            val rpcOverloads = listOf(
                arrayOf(
                    String::class.java, Boolean::class.java, Boolean::class.java, String::class.java,
                    "com.alibaba.fastjson.JSONObject", String::class.java, "com.alibaba.fastjson.JSONObject",
                    Boolean::class.java, Boolean::class.java, Int::class.java, Boolean::class.java, String::class.java,
                    "com.alibaba.ariver.app.api.App", "com.alibaba.ariver.app.api.Page",
                    "com.alibaba.ariver.engine.api.bridge.extension.BridgeCallback"
                ),
                arrayOf(
                    String::class.java, Boolean::class.java, Boolean::class.java, String::class.java,
                    "com.alibaba.fastjson.JSONObject", String::class.java, "com.alibaba.fastjson.JSONObject",
                    Boolean::class.java, Boolean::class.java, Int::class.java, Boolean::class.java, String::class.java,
                    "com.alibaba.ariver.app.api.App", "com.alibaba.ariver.app.api.Page",
                    "com.alibaba.ariver.engine.api.bridge.model.ApiContext",
                    "com.alibaba.ariver.engine.api.bridge.extension.BridgeCallback"
                )
            )

            rpcOverloads.forEach { params ->
                className.firstMethod { name = "rpc"; parameters(*params) }.hook {
                    before {
                        val callbackIndex = args.size - 1
                        val callback = args.getOrNull(callbackIndex) ?: return@before
                        val recordArray = arrayOfNulls<Any>(4).apply {
                            this[0] = System.currentTimeMillis()
                            this[1] = args.getOrNull(0) ?: "null"
                            this[2] = args.getOrNull(4) ?: "null"
                        }
                        rpcHookMap[callback] = recordArray
                    }
                    after {
                        val callbackIndex = args.size - 1
                        val callback = args.getOrNull(callbackIndex) ?: return@after
                        val recordArray = rpcHookMap.remove(callback)
                        recordArray?.let {
                            val time = it[0]
                            val method = it[1]
                            val params = it[2]
                            val data = it[3]
                            val res = JSONObject().apply {
                                put("TimeStamp", time)
                                put("Method", method)
                                put("Params", params)
                                put("Data", data ?: JSONObject.NULL)
                            }
                            if (isDebug) Sender.sendHookData(res, debugUrl)
                            YLog.debug(res.toString())
                            Log.d(tag, res.toString())
                        }
                    }
                }
            }

            YLog.info("Hook RpcBridgeExtension#rpc end <===")
        } catch (t: Throwable) {
            YLog.error("Hook RpcBridgeExtension#rpc 失败: ${t.message}", t)
        }
    }


    fun PackageParam.hookDefaultBridgeCallback() {
        try {
            YLog.debug("hookDefaultBridgeCallback start ===>")
            val className = "com.alibaba.ariver.engine.common.bridge.internal.DefaultBridgeCallback".toClass().resolve()
            className.apply {
                firstMethod {
                    name = "sendJSONResponse"
                    parameters("com.alibaba.fastjson.JSONObject")
                }.hook {
                    before {
                        val callback = instance   // ✅ 这里用 instance
                        val recordArray = rpcHookMap[callback]
                        if (recordArray != null && args.isNotEmpty()) {
                            recordArray[3] = args[0]?.toString() ?: "null" // ✅ 改成 args[0]
                        }
                    }
                    YLog.debug("Hook DefaultBridgeCallback#sendJSONResponse 成功")
                }
            }
            YLog.debug("Hook DefaultBridgeCallback#sendJSONResponse end <===")
        } catch (t: Throwable) {
            YLog.error("Hook DefaultBridgeCallback#sendJSONResponse 失败: ${t.message}", t)
        }
    }


}
