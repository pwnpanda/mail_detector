package com.robinlunde.mailbox.network

import com.robinlunde.mailbox.datamodel.pill.Day
import com.robinlunde.mailbox.datamodel.pill.GenericType
import retrofit2.http.*

interface ApiInterfaceDay {
    // Get days
    @GET("/v1/users/{user}/days")
    fun getDays(@Path("user") user_id: Int): MutableList<Day>

    // Get day by id
    @GET("/v1/users/{user}/days/{day}")
    fun getDay(
        @Path("user") user_id: Int,
        @Path("day") day_id: Int
    ): Day

    // Update day by ID
    @PUT("/v1/users/{user}/days/{day}")
    fun updateDay(
        @Path("user") user_id: Int,
        @Path("day") day_id: Int,
        @Body day: Day
    ): Day

    // Delete day by ID
    @DELETE("/v1/users/{user}/days/{day}")
    fun deleteDay(
        @Path("user") user_id: Int,
        @Path("day") day_id: Int
    ): GenericType<Day>

}