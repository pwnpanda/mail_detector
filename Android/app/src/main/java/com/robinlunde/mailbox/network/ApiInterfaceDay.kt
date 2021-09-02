package com.robinlunde.mailbox.network

import com.robinlunde.mailbox.datamodel.pill.Day
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiInterfaceDay {
    // Get days
    @GET("/v1/users/{user}/days")
    fun getDays(@Path("user") user_id: Int): Call<MutableList<Day>>

    // Get day by id
    @GET("/v1/users/{user}/days/{day}")
    fun getDay(@Path("user") user_id: Int,
               @Path("day") day_id: Int): Call<MutableList<Day>>

    /*
    @PUT(/v1/users/{user}/days/{day})
    fun updateDay(@Path("user") user_id: Int,
     @Path("day") day_id: Int): Call<MutableList<Day>> // Will not be correct - how to handle?
    @DELETE(/v1/users/{user}/days/{day})
    fun deleteDay(@Path("user") user_id: Int,
     @Path("day") day_id: Int): // What kind of return value?
    */

}