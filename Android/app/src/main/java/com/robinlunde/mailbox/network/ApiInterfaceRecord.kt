package com.robinlunde.mailbox.network

import com.robinlunde.mailbox.datamodel.pill.GenericType
import com.robinlunde.mailbox.datamodel.pill.Record
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiInterfaceRecord {
    // Get all records
    @GET("/v1/users/{user}/records")
    fun getRecords(@Path("user") user_id: Int): Call<MutableList<Record>>

    // Get all records with a pill
    @GET("/v1/users/{user}/records?pill={pill}")
    fun getRecordsByPill(
        @Path("user") user_id: Int,
        @Path("pill") pill_id: Int
    ): Call<MutableList<Record>>

    // Get all records for a day
    @GET("/v1/users/{user}/records")
    fun getRecordsByDay(
        @Path("user") user_id: Int,
        @Path("day") day_id: Int
    ): Call<MutableList<Record>>

    // Get record by id
    @GET("/v1/users/{user}/records/{record}")
    fun getRecord(
        @Path("user") user_id: Int,
        @Path("record") record_id: Int
    ): Call<Record>

    @PUT("/v1/users/{user}/records/{record}")
    fun updateRecord(
        @Path("user") user_id: Int,
        @Path("record") record_id: Int
    ): Call<Record>

    @DELETE("/v1/users/{user}/records/{record}")
    fun deleteRecord(
        @Path("user") user_id: Int,
        @Path("record") record_id: Int
    ): Call<GenericType<Record>>

}