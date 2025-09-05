package com.fansirsqi.sen.util

import android.util.Log
import com.highcapable.yukihookapi.hook.log.YLog
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import okhttp3.*

object Sender {

    private const val TAG = "HookSender"

    /** 控制是否发送数据（只打印一次失败日志） */
    @Volatile
    private var sendFlag: Boolean = true

    private val client: OkHttpClient by lazy { OkHttpClient() }

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    /**
     * 发送 Hook 数据到 DEBUG 服务器
     * @param jo JSON 数据
     * @param url 服务器地址
     */
    fun sendHookData(jo: JSONObject, url: String?) {
        if (url.isNullOrEmpty()) return  // URL 不可用直接返回

        try {
            val body = jo.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // 只打印一次失败日志
                    if (sendFlag) {
                        sendFlag = false
                        YLog.error("Failed to send hook data: ", e, tag = TAG)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use { // 自动关闭 Response
                        if (!it.isSuccessful) {
                            YLog.error("Failed to receive response: $it ", tag = TAG)
                        } else {
                            YLog.debug("Hook data sent successfully.", tag = TAG)
                        }
                    }
                }
            })
        } catch (t: Throwable) {
            YLog.error("Exception in sendHookData:", t, TAG)
        }
    }
}