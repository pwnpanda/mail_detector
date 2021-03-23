package com.robinlunde.mailbox

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URL

class HttpRequestLib(context: Context) {

    var context: Context = context
        set(value) { field = value.applicationContext }

    private val client = OkHttpClient()
    private val url = URL("https://robinlunde.com/api/posts")

    // Send latest data to Server
    fun sendDataWeb(timestamp: String): Boolean {


        //or using jackson
        val mapperAll = ObjectMapper()
        val jacksonObj = mapperAll.createObjectNode()
        jacksonObj.put("timestamp", timestamp)
        val jacksonString = jacksonObj.toString()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jacksonString.toRequestBody(mediaType)

        val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

        val response = client.newCall(request).execute()

        val responseBody = response.body!!.string()
        // Log.d("HTTP-Post", "Response code: ${response.code}")
        //Response
        Log.d("HTTP-Post", "Response Body: $responseBody")
        if (response.code == 200) {
            val toast = Toast.makeText(context, "Timestamp saved!", Toast.LENGTH_LONG)
            // Show toast
            toast.show()
        }

        return response.code == 200
    }

    // Get results for last 14 days
    fun getDataWeb(): String {
        val client = OkHttpClient()
        val url = URL("https://robinlunde.com/api/posts")

        val request = Request.Builder()
                .url(url)
                .build()
        val response = client.newCall(request).execute()

        val responseBody = response.body!!.string()
        Log.d("HTTP-Get", "Response code: ${response.code}")
        //Response
        Log.d("HTTP-Get", "Response Body: $responseBody")
        if (response.code == 200) {
            // Create entry for each one in responseBody and display details in view
            // val toast = Toast.makeText(applicationContext, "Timestamp saved!", Toast.LENGTH_LONG)
            // Show toast
            // toast.show()
            return responseBody
        } else return ""
    }
}