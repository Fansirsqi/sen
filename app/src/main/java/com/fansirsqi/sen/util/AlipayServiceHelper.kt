package com.fansirsqi.sen.util

import com.fansirsqi.sen.hook.AppContextHolder
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.PackageParam

object AlipayServiceHelper {
    const val TAG = "AlipayServiceHelper"
    var microApplicationContextObject: Any? = null

    /** 获取 MicroApplicationContext */
    fun PackageParam.getMicroApplicationContext(): Any? {
        if (microApplicationContextObject == null) {
            try {
                val alipayAppClass = "com.alipay.mobile.framework.AlipayApplication"
                    .toClass()
                    .asResolver()

                val instance = alipayAppClass
                    .firstMethod {
                        name = "getInstance"
                        emptyParameters()
                        superclass()   // 查父类
                    }
                    .invoke<Any>()

                microApplicationContextObject = instance?.asResolver()
                    ?.firstMethod {
                        name = "getMicroApplicationContext"
                        emptyParameters()
                        superclass()   // 查父类
                    }
                    ?.invoke<Any>()

            } catch (t: Throwable) {
                YLog.error("getMicroApplicationContext failed", t, TAG)
                microApplicationContextObject = null
            }
        }
        return microApplicationContextObject
    }

    /** 根据接口名获取 Service 对象 */
    fun PackageParam.getServiceObject(service: String?): Any? {
        return try {
            getMicroApplicationContext()?.asResolver()
                ?.firstMethod {
                    name = "findServiceByInterface"
                    parameters(String::class.java)
                    superclass()   // 查父类
                }?.invoke(service)
        } catch (t: Throwable) {
            YLog.error("getServiceObject failed for $service", t, TAG)
            null
        }
    }

    /** 获取用户对象 */
    fun PackageParam.getUserObject(): Any? {
        return try {
            val serviceClassName = "com.alipay.mobile.personalbase.service.SocialSdkContactService"
            val serviceObject = getServiceObject(serviceClassName)
            serviceObject?.asResolver()
                ?.firstMethod {
                    name = "getMyAccountInfoModelByLocal"
                    emptyParameters()
                    superclass()
                }
                ?.invoke<Any>()
        } catch (t: Throwable) {
            YLog.error("getUserObject failed", t, TAG)
            null
        }
    }

    /** 获取用户 ID */
    fun PackageParam.getUserId(): String? {
        return try {
            getUserObject()?.asResolver()
                ?.firstField {
                    name = "userId"
                    superclass()   // 字段也可能在父类
                }
                ?.get<String>()
        } catch (t: Throwable) {
            YLog.error("getUserId failed", t, TAG)
            null
        }
    }

    fun PackageParam.listAllFields() {
        try {
            val userObject = getUserObject()?.asResolver()
            if (userObject == null) {
                YLog.debug("ServiceObject for $userObject is null", tag = "AlipayServiceHelper")
                return
            }
            var clazz: Class<*>? = userObject.javaClass
            YLog.debug("Listing all fields for $userObject:", tag = "AlipayServiceHelper")
            while (clazz != null) {
                clazz.declaredFields.forEach { field ->
                    field.isAccessible = true
                    YLog.debug("Class: ${clazz.name}, Field: ${field.name}, Type: ${field.type.name}", tag = "AlipayServiceHelper")
                }
                clazz = clazz.superclass
            }
        } catch (t: Throwable) {
            YLog.error("listAllFields failed for userObject", t, "AlipayServiceHelper")
        }
    }


    /** Hook 一些前后台检测方法 */
    fun PackageParam.otherServiceHook() {
        YLog.debug("start hook other service for background")

        val hooks = listOf(
            Triple("com.alipay.mobile.common.fgbg.FgBgMonitorImpl", "isInBackground", ClassLoader::class.java),
            Triple("com.alipay.mobile.common.fgbg.FgBgMonitorImpl", "isInBackground", Boolean::class.java),
            Triple("com.alipay.mobile.common.fgbg.FgBgMonitorImpl", "isInBackgroundV2", ClassLoader::class.java),
            Triple("com.alipay.mobile.common.transport.utils.MiscUtils", "isAtFrontDesk", ClassLoader::class.java)
        )

        hooks.forEach { (className, methodName, paramType) ->
            try {
                className.toClass().asResolver().firstMethod {
                    name = methodName
                    parameters(paramType)
                }.hook {
                    after {
                        when (methodName) {
                            "isInBackgroundV2", "isAtFrontDesk" -> resultTrue()
                            else -> resultFalse()
                        }
                    }
                }
            } catch (t: Throwable) {
                YLog.error("$className ==> $methodName hook failed", t, TAG)
            }
        }

        YLog.debug("end hook other service for background")
        AppContextHolder.hooked = true
    }
}
