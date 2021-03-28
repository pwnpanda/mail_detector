package com.robinlunde.mailbox

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URL
import java.time.LocalDateTime

class HttpRequestLib (context: Context) {

    var context: Context = context
        set(value) { field = value.applicationContext }

    private val client = OkHttpClient()
    private var url : URL = URL(context.getString(R.string.normal_url))

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
    @RequiresApi(Build.VERSION_CODES.O)
    fun sendDataWeb(timestamp: String): Boolean {

        val pickupTime = LocalDateTime.now()
        //Using jackson to get string to JSON
        val mapperAll = ObjectMapper()
        val jacksonObj = mapperAll.createObjectNode()
        jacksonObj.put("delivered", timestamp)
        jacksonObj.put("username", MailboxApp.getUsername())
        jacksonObj.put("pickup", pickupTime.toString())

        val mediaType = "application/json; charset=utf-8".toMediaType()
        // TODO remove this, since it seems not needed, see above
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
            val toast = Toast.makeText(context, "Timestamp saved!", Toast.LENGTH_LONG)
            // Show toast
            toast.show()
        }

        // TODO check return code for create
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