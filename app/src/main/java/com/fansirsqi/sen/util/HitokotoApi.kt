package com.fansirsqi.sen.util


import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
// HitokotoResponse 数据类，用于解析一言 API 返回的 JSON 数据
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import android.util.Log


@Serializable
data class HitokotoResponse(
    val id: Int = 0,
    val uuid: String = "default-uuid",
    val hitokoto: String = "一往情深深几许？深山夕照深秋雨。",
    val type: String = "default",
    val from: String? = "蝶恋花·出塞",
    @SerialName("from_who")
    val fromWho: String? = "纳兰性德",
    val creator: String? = "default-creator",
    @SerialName("created_at")
    val createdAt: String? = null,
    val length: Int = 40,
    val fullHitokoto: String? = null
)


object HitokotoApiClient {
    private const val TAG = "HitokotoApiClient"
    private val client = OkHttpClient()
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    suspend fun getHitokoto(): HitokotoResponse {

        val request = Request.Builder()
            .url("https://v1.hitokoto.cn/?c=a&c=b&c=c&c=d&c=e&c=f&c=g&c=h&c=i&c=j&c=k&c=l&c=&encode=json") // 修复空格！
            .build()

        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e(TAG, "请求失败: ${response.code}")
                        return@withContext defaultHitokotoResponse()
                    }
                    val json = response.body.string()
                    val res = try {
                        jsonParser.decodeFromString<HitokotoResponse>(json)
                    } catch (e: Exception) {
                        Log.e(TAG, "kotlinx.serialization 解析失败: ${e.message}")
                        null
                    }
                    Log.d(TAG, "元数据: $json \n解析数据: $res")
                    res ?: defaultHitokotoResponse()
                }
            } catch (e: Exception) {
                Log.e(TAG, "请求异常: ${e.message}")
                e.printStackTrace()
                defaultHitokotoResponse()
            }
        }
    }

    fun defaultHitokotoResponse() = HitokotoResponse()
}