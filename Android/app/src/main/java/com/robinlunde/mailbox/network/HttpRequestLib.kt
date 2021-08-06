package com.robinlunde.mailbox.network

import android.content.Context
import android.util.Log
import com.fasterxml.jackson.databind.ObjectMapper
import com.robinlunde.mailbox.MailboxApp
import com.robinlunde.mailbox.R
import com.robinlunde.mailbox.datamodel.PostUpdateStatus
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URL


class HttpRequestLib {
    var context: Context = MailboxApp.getInstance()

    private val client = OkHttpClient()
    private var url: URL = URL(context.getString(R.string.normal_url))

    /** TODO Refactor all http requests to be async
     * Seems better to be callback based!
     * THink about how to do it.
     * https://www.baeldung.com/guide-to-okhttp
     * https://stackoverflow.com/a/34967554
     */

    // Get results for last 14 days
    fun getDataWeb(myUrl: URL?): String {
        val urlNow: URL = myUrl ?: url
        val request = Request.Builder()
            .url(urlNow)
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
        Log.d("HTTP-SendLog-Post", "Timestamp in: $timestamp")
        val pickupTime = MailboxApp.getUtil().getTime()
        //Using jackson to get string to JSON
        val mapperAll = ObjectMapper()
        val jacksonObj = mapperAll.createObjectNode()
        jacksonObj.put("delivered", timestamp)
        jacksonObj.put("username", MailboxApp.getUsername())
        jacksonObj.put("pickup", pickupTime)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jacksonObj.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body!!.string()
        Log.d("HTTP-SendLog-Post", "Full transmission: $request")
        Log.d("HTTP-SendLog-Post", "Full response: $response")
        //Response
        Log.d("HTTP-SendLog-Post", "Response Body: $responseBody")
        if (response.code == 200) {
            Log.d("HTTP-SendLog-Post", "Timestamp for pickup logged successfully")

        }
        return response.code == 200
    }

    // delete entry
    fun deleteLog(id: Int): Boolean {
        Log.d("HTTP-Delete", "Entered process for id:$id")
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


    // Send latest data to Server
    fun setNewUpdateWeb(data: PostUpdateStatus): Boolean {
        val urlNow: URL = URL(MailboxApp.getInstance().getString(R.string.update_url))
        //Using jackson to get string to JSON
        val mapperAll = ObjectMapper()
        val jacksonObj = mapperAll.createObjectNode()
        jacksonObj.put("newMail", data.newMail)
        jacksonObj.put("username", data.username)
        jacksonObj.put("timestamp", data.timestamp)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jacksonObj.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(urlNow)
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body!!.string()
        Log.d("HTTP-Status-Post", "Full transmission: $request")
        Log.d("HTTP-Status-Post", "Full response: $response")
        //Response
        Log.d("HTTP-Status-Post", "Response Body: $responseBody")
        if (response.code == 200) {
            Log.d("HTTP-Status-Post", "Timestamp for pickup logged successfully")

        }
        return response.code == 200
    }
}
