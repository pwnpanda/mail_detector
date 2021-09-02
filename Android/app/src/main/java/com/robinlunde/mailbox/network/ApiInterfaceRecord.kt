package com.robinlunde.mailbox.network

import com.robinlunde.mailbox.datamodel.pill.Record
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiInterfaceRecord {
    // Get records
    @GET("/v1/users/{user}/records")
    fun getRecords(@Path("user") user_id: Int): Call<MutableList<Record>>

    // Get record by id
    @GET("/v1/users/{user}/records/{record}")
    fun getRecord(
        @Path("user") user_id: Int,
        @Path("record") record_id: Int
    ): Call<MutableList<Record>>
    /*
    @PUT(/v1/users/{user}/records/{record})
    fun updateRecord(@Path("user") user_id: Int,
     @Path("record") record_id: Int): Call<MutableList<Record>> // Will not be correct - how to handle?
    @DELETE(/v1/users/{user}/records/{record})
    fun deleteRecord(@Path("user") user_id: Int,
     @Path("record") record_id: Int): // What kind of return value?
    */
}