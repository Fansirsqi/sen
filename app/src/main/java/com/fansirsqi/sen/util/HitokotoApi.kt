package com.fansirsqi.sen.util

import android.util.Log
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// HitokotoResponse 数据类，用于解析一言 API 返回的 JSON 数据
data class HitokotoResponse(
    val id: Int,
    val uuid: String,
    val hitokoto: String,  // 随机的一句话文本
    val type: String,  // 句子的类型，如 "i"
    val from: String?,  // 作品名称
    val fromWho: String?,  // 作者名称
    val creator: String,
    val createdAt: Long,  // 创建时间
    val length: Int,  // 句子的长度
    val fullHitokoto: String  // 完整的句子文本
)

// 用于请求一言 API 的工具类
object HitokotoApiClient {

    private val client = OkHttpClient()  // 创建 OkHttp 客户端

    /**
     * 使用 OkHttp 获取随机一句话的网络请求
     * @return HitokotoResponse，包含随机一句话的响应数据
     */
    suspend fun getHitokoto(): HitokotoResponse {
        val request = Request.Builder()
            .url("https://v1.hitokoto.cn/?c=i&encode=json")  // 请求的 URL
            .build()

        // 使用协程在 IO 线程中发起请求
        return withContext(Dispatchers.IO) {
            try {
                val response: Response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val json = response.body.string()
                    // 使用 Gson 解析 JSON 数据
                    val hitokotoResponse = Gson().fromJson(json, HitokotoResponse::class.java)
                    hitokotoResponse
                } else {
                    Log.e("HitokotoApiClient", "请求失败: ${response.code}")
                    // 如果请求失败，返回默认的一言
                    defaultHitokotoResponse()
                }
            } catch (e: Exception) {
                Log.e("HitokotoApiClient", "请求异常: ${e.message}")
                // 如果请求异常，返回默认的一言
                defaultHitokotoResponse()
            }
        }
    }

    // 默认的返回内容
    fun defaultHitokotoResponse(): HitokotoResponse {
        val defaultHitokoto = "一往情深深几许？深山夕照深秋雨。"
        val defaultFrom = "蝶恋花·出塞"
        val defaultFromWho = "纳兰性德"

        return HitokotoResponse(
            id = 0,
            uuid = "default-uuid",
            hitokoto = defaultHitokoto,
            type = "default",
            from = defaultFrom,
            fromWho = defaultFromWho,
            creator = "default-creator",
            createdAt = System.currentTimeMillis() / 1000,
            length = 40,
            fullHitokoto = "$defaultHitokoto \n                           -----Re:$defaultFromWho《$defaultFrom》"  // 使用局部变量
        )
    }
}
