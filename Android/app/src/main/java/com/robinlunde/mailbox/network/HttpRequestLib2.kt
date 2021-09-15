package com.robinlunde.mailbox.network

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.robinlunde.mailbox.Util
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

// Change this later and override it in specific versions for Mail-function instead TODO
const val BASEURL = "https://robinlunde.com/api/"

// MailboxApp.getContext().getString(R.string.base_url)
class HttpRequestLib2 {
    companion object {
        private var retrofit: Retrofit? = null
        fun getClient(util: Util): Retrofit {

            if (retrofit != null)   return retrofit as Retrofit

            // TODO remove in prod
            val interceptor = HttpLoggingInterceptor()
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

            val authInterceptor = AuthenticationInterceptor()
            util.authInterceptor = authInterceptor

            val client = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addInterceptor(authInterceptor)
                .readTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS).build()

            val objectMapper = ObjectMapper()
            objectMapper.registerModule(KotlinModule())
            val factory = JacksonConverterFactory.create(objectMapper)

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