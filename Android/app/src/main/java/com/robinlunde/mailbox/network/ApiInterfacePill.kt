package com.robinlunde.mailbox.network

import com.robinlunde.mailbox.datamodel.pill.ConcreteGenericType
import com.robinlunde.mailbox.datamodel.pill.GenericType
import com.robinlunde.mailbox.datamodel.pill.Pill
import retrofit2.http.*

interface ApiInterfacePill {
    // Get pills
    @GET("v1/users/{user}/pills")
    suspend fun getPills(@Path("user") user_id: Int): MutableList<Pill>

    // Get pill by id
    @GET("v1/users/{user}/pills/{pill}")
    suspend fun getPill(
        @Path("user") user_id: Int,
        @Path("pill") pill_id: Int
    ): Pill

    // Create pill
    @POST("v1/users/{user}/pills")
    suspend fun createPill(
        @Path("user") user_id: Int,
        @Body pill: Pill
    ): Pill

    // Update pill by ID
    @PUT("v1/users/{user}/pills/{pill}")
    suspend fun updatePill(
        @Path("user") user_id: Int,
        @Path("pill") pill_id: Int,
        @Body pill: Pill
    ): Pill

    // Delete pill by ID
    @DELETE("v1/users/{user}/pills/{pill}")
    suspend fun deletePill(
        @Path("user") user_id: Int,
        @Path("pill") pill_id: Int
    ): ConcreteGenericType
}