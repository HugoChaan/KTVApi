package io.agora.ktvdemo.api

import android.util.Log
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import io.agora.ktvdemo.BuildConfig
import io.agora.ktvdemo.MyApplication
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Request.Builder
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 云端合流请求
 */
class CloudApiManager private constructor() {

    companion object {
        fun getInstance(): CloudApiManager {
            return InstanceHolder.apiManager
        }

        //private const val testIp = "218.205.37.50"
        private const val domain = "https://api.sd-rtn.com"
        private const val TAG = "ApiManager"
        private const val cloudRtcUid = 20232023
    }

    internal object InstanceHolder {
        val apiManager = CloudApiManager()
    }

    private var tokenName = ""
    private var taskId = ""
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .addInterceptor(CurlInterceptor(object : Logger {
            override fun log(message: String) {
                Log.d(TAG, message)
            }
        }))
        .build()

    private fun fetchCloudToken(): String {
        var token = ""
        try {
            val acquireOjb = JSONObject()
            acquireOjb.put("instanceId", System.currentTimeMillis().toString() + "")
            //acquireOjb.put("testIp", testIp)
            val request: Request = Builder()
                .url(getTokenUrl(domain, BuildConfig.AGORA_APP_ID))
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", basicAuth)
                .post(acquireOjb.toString().toRequestBody())
                .build()

            val responseToken = okHttpClient.newCall(request).execute()
            if (responseToken.isSuccessful) {
                val body = responseToken.body!!
                val bodyString = body.string()
                val jsonToken = JSONObject(bodyString)
                if (jsonToken.has("tokenName")) {
                    token = jsonToken.getString("tokenName")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "getToken error " + e.message)
        }
        return token
    }

    fun fetchStartCloud(mainChannel: String) {
        val token = fetchCloudToken()
        tokenName = token.ifEmpty {
            Log.e(TAG, "云端合流uid 请求报错 token is null")
            return
        }
        var taskId = ""
        try {
            val transcoderObj = JSONObject()
            val inputRetObj = JSONObject()
                .put("rtcUid", 0)
                .put("rtcToken", BuildConfig.AGORA_APP_ID)
                .put("rtcChannel", mainChannel)
            val intObj = JSONObject()
                .put("rtc", inputRetObj)
            transcoderObj.put("audioInputs", JSONArray().put(intObj))
            transcoderObj.put("idleTimeout", 300)
            val audioOptionObj = JSONObject()
                .put("profileType", "AUDIO_PROFILE_MUSIC_HIGH_QUALITY_STEREO")
                .put("fullChannelMixer", "native-mixer-weighted")
            val outputRetObj = JSONObject()
                .put("rtcUid", cloudRtcUid)
                .put("rtcToken", BuildConfig.AGORA_APP_ID)
                .put("rtcChannel", mainChannel + "_ad")
            val dataStreamObj = JSONObject()
                .put("source", JSONObject().put("audioMetaData", true))
                .put("sink", JSONObject())
            val outputsObj = JSONObject()
                .put("audioOption", audioOptionObj)
                .put("rtc", outputRetObj)
                .put("metaDataOption", dataStreamObj)
            transcoderObj.put("outputs", JSONArray().put(outputsObj))
            val postBody = JSONObject()
                .put(
                    "services", JSONObject()
                        .put(
                            "cloudTranscoder", JSONObject()
                                .put("serviceType", "cloudTranscoderV2")
                                .put(
                                    "config", JSONObject()
                                        .put("transcoder", transcoderObj)
                                )
                        )
                )
            val request: Request = Builder()
                .url(startTaskUrl(domain, BuildConfig.AGORA_APP_ID, tokenName))
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", basicAuth)
                .post(postBody.toString().toRequestBody())
                .build()

            val responseStart = okHttpClient.newCall(request).execute()
            if (responseStart.isSuccessful) {
                val body = responseStart.body!!
                val bodyString = body.string()
                val jsonUid = JSONObject(bodyString)
                if (jsonUid.has("taskId")) {
                    taskId = jsonUid.getString("taskId")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "云端合流uid 请求报错 " + e.message)
        }
        if (taskId.isNotEmpty()) {
            this.taskId = taskId
        }
    }

    fun fetchStopCloud() {
        if (taskId.isEmpty() || tokenName.isEmpty()) {
            Log.e(TAG, "云端合流任务停止失败 taskId || tokenName is null")
            return
        }
        try {
            val request: Request = Builder()
                .url(deleteTaskUrl(domain, BuildConfig.AGORA_APP_ID, taskId, tokenName))
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", basicAuth)
                .delete()
                .build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body!!
                val bodyString = body.string()
            }
        } catch (e: Exception) {
            Log.e(TAG, "云端合流任务停止失败 " + e.message)
        }
    }

    private fun getTokenUrl(domain: String, appId: String): String {
        return String.format("%s/v1/projects/%s/rtsc/cloud-transcoder/builderTokens", domain, appId)
    }

    private fun startTaskUrl(domain: String, appId: String, tokenName: String): String {
        return String.format("%s/v1/projects/%s/rtsc/cloud-transcoder/tasks?builderToken=%s", domain, appId, tokenName)
    }

    private fun deleteTaskUrl(domain: String, appid: String, taskid: String, tokenName: String): String {
        return String.format(
            "%s/v1/projects/%s/rtsc/cloud-transcoder/tasks/%s?builderToken=%s",
            domain,
            appid,
            taskid,
            tokenName
        )
    }

    private val basicAuth: String
        private get() {
            // 拼接客户 ID 和客户密钥并使用 base64 编码
            val plainCredentials = BuildConfig.RESTFUL_API_KEY + ":" + BuildConfig.RESTFUL_API_SECRET
            var base64Credentials: String? = null
            base64Credentials = String(Base64.getEncoder().encode(plainCredentials.toByteArray()))
            // 创建 authorization header
            return "Basic $base64Credentials"
        }

    private fun getString(resId:Int):String{
        return MyApplication().getString(resId)
    }
}