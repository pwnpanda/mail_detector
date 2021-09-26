package com.robinlunde.mailbox.network

import com.robinlunde.mailbox.datamodel.pill.ConcreteGenericType
import com.robinlunde.mailbox.datamodel.pill.User
import retrofit2.http.*

interface ApiInterfaceUser {

    // Signup
    @POST("v1/signup")
    @Headers("No-Auth: true")
    suspend fun signup(@Body user: User): User

    // Login
    @POST("v1/login")
    @Headers("No-Auth: true")
    suspend fun login(@Body user: User): User

    // Get users
    @GET("v1/users")
    suspend fun getUsers(): User

    // Get user by id
    @GET("v1/users/{user}")
    suspend fun getUser(@Path("user") user_id: Int): User

    // Update user by ID
    @PUT("v1/users/{user}")
    suspend fun updateUser(@Path("user") user_id: Int, @Body user: User): User

    // Delete user by ID
    @DELETE("v1/users/{user}")
    suspend fun deleteUser(@Path("user") user_id: Int): ConcreteGenericType
}