package com.robinlunde.mailbox.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

const val BASEURL = "https://robinlunde.com/api"

// MailboxApp.getContext().getString(R.string.base_url)
class HttpRequestLib2 {
    companion object {
        private var retrofit: Retrofit? = null
        fun getClient(): Retrofit {
            val client = OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS).build()
            val factory = JacksonConverterFactory.create()
            if (retrofit == null) {
                retrofit =
                    Retrofit.Builder()
                        .baseUrl(BASEURL)
                        .client(client)
                        .addConverterFactory(factory)
                        .build()
            }
            return retrofit!!
        }
    }
}