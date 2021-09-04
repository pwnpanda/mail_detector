package com.robinlunde.mailbox.network

import com.robinlunde.mailbox.datamodel.pill.GenericType
import com.robinlunde.mailbox.datamodel.pill.Pill
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiInterfacePill {
    // Get pills
    @GET("/v1/users/{user}/pills")
    fun getPills(@Path("user") user_id: Int): Call<MutableList<Pill>>

    // Get pill by id
    @GET("/v1/users/{user}/pills/{pill}")
    fun getPill(
        @Path("user") user_id: Int,
        @Path("pill") pill_id: Int
    ): Call<Pill>

    // Update pill by ID
    @PUT("/v1/users/{user}/pills/{pill}")
    fun updatePill(
        @Path("user") user_id: Int,
        @Path("pill") pill_id: Int
    ): Call<Pill>

    // Delete pill by ID
    @DELETE("/v1/users/{user}/pills/{pill}")
    fun deletePill(
        @Path("user") user_id: Int,
        @Path("pill") pill_id: Int
    ): Call<GenericType<Pill>>
}