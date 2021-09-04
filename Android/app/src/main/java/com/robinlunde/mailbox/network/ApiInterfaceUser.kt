package com.robinlunde.mailbox.network

import com.robinlunde.mailbox.datamodel.pill.GenericType
import com.robinlunde.mailbox.datamodel.pill.User
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiInterfaceUser {
    // Get users
    @GET("/v1/users")
    fun getUsers(): Call<User>

    // Get user by id
    @GET("/v1/users/{user}")
    fun getUser(@Path("user") user_id: Int): Call<User>

    // Update user by ID
    @PUT("/v1/users/{user}/users/{user}")
    fun updateUser(@Path("user") user_id: Int): Call<User>

    // Delete user by ID
    @DELETE("/v1/users/{user}/users/{user}")
    fun deleteUser(@Path("user") user_id: Int): Call<GenericType<User>>
}