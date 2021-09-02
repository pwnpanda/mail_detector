package com.robinlunde.mailbox.network

import com.robinlunde.mailbox.datamodel.pill.User
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiInterfaceUser {
    // Get users
    @GET("/v1/users")
    fun getUsers(): Call<MutableList<User>>
    // Get user by id
    @GET("/v1/users/{user}")
    fun getUser(@Path("user") user_id: Int): Call<MutableList<User>>

    /*
    @PUT(/v1/users/{user}/users/{user})
    fun updateUser(@Path("user") user_id: Int): Call<MutableList<User>> // Will not be correct - how to handle?
    @DELETE(/v1/users/{user}/users/{user})
    fun deleteUser(@Path("user") user_id: Int): // What kind of return value?
    */
}