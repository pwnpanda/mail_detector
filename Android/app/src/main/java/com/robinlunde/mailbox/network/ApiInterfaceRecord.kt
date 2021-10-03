package com.robinlunde.mailbox.network

import com.robinlunde.mailbox.datamodel.pill.ConcreteGenericType
import com.robinlunde.mailbox.datamodel.pill.Record
import retrofit2.http.*

interface ApiInterfaceRecord {
    // Get all records
    @GET("v1/users/{user}/records")
    suspend fun getRecords(@Path("user") user_id: Int): MutableList<Record>

    // Get all records with a pill
    @GET("v1/users/{user}/records?pill={pill}")
    suspend fun getRecordsByPill(
        @Path("user") user_id: Int,
        @Path("pill") pill_id: Int
    ): MutableList<Record>

    // Get all records for a day
    @GET("v1/users/{user}/records")
    suspend fun getRecordsByDay(
        @Path("user") user_id: Int,
        @Path("day") day_id: Int
    ): MutableList<Record>

    // Get record by id
    @GET("v1/users/{user}/records/{record}")
    suspend fun getRecord(
        @Path("user") user_id: Int,
        @Path("record") record_id: Int
    ): Record

    // Create record
    @POST("v1/users/{user}/records")
    suspend fun createRecord(
        @Path("user") user_id: Int,
        @Body record: Record
    ): Record

    @PUT("v1/users/{user}/records/{record}")
    suspend fun updateRecord(
        @Path("user") user_id: Int,
        @Path("record") record_id: Int,
        @Body record: Record
    ): Record

    @DELETE("v1/users/{user}/records/{record}")
    suspend fun deleteRecord(
        @Path("user") user_id: Int,
        @Path("record") record_id: Int
    ): ConcreteGenericType

}