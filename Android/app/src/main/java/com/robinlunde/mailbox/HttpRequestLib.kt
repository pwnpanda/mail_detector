package com.robinlunde.mailbox

import android.content.Context
import android.util.Log
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*


class HttpRequestLib {
    var context: Context = MailboxApp.getInstance()

    private val client = OkHttpClient()
    private var url: URL = URL(context.getString(R.string.normal_url))

    // Get results for last 14 days
    fun getDataWeb(): String {
        val request = Request.Builder()
            .url(url)
            .build()
        val response = client.newCall(request).execute()

        val responseBody = response.body!!.string()
        Log.d("HTTP-Get", "Response code: ${response.code}")
        //Response
        Log.d("HTTP-Get", "Response Body: $responseBody")
        return if (response.code == 200) {
            responseBody
        } else ""
    }

    // Send latest data to Server
    fun sendDataWeb(timestamp: String): Boolean {

        val pickupTime = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDateTime.now()
        } else {
            val sdf = SimpleDateFormat("yyyy-MM-ddTHHmmssZ", Locale.getDefault())
            val currentDateandTime: String = sdf.format(Date())
            Log.d("Outdated Time", currentDateandTime)
            currentDateandTime
        }
        //Using jackson to get string to JSON
        val mapperAll = ObjectMapper()
        val jacksonObj = mapperAll.createObjectNode()
        jacksonObj.put("delivered", timestamp)
        jacksonObj.put("username", MailboxApp.getUsername())
        jacksonObj.put("pickup", pickupTime.toString())

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jacksonObj.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body!!.string()
        //Response
        Log.d("HTTP-Post", "Response Body: $responseBody")
        if (response.code == 200) {
            Log.d("HTTP-Post", "Timestamp for pickup logged successfully")

        }
        return response.code == 200
    }

    // delete entry
    fun deleteLog(id: Int): Boolean {
        val newUrl = URL("$url/$id")
        val request = Request.Builder()
            .url(newUrl)
            .delete()
            .build()
        val response = client.newCall(request).execute()

        val responseBody = response.body!!.string()
        //Response
        Log.d("HTTP-Delete", "Response code: ${response.code}")
        Log.d("HTTP-Delete", "Response Body: $responseBody")

        return response.code == 200
    }
}
