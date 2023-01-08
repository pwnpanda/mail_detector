package com.robinlunde.mailbox.network

import com.robinlunde.mailbox.datamodel.*
import retrofit2.Call
import retrofit2.http.*

interface ApiInterfaceMailNotifications {

    // Get last update for the mailbox-status from the Web-API (AKA last status submitted by others)
    @GET("post/status")
    fun getLastMailboxStatus(): Call<PostUpdateStatus>

    // Set the latest received mailbox-status, by forwarding received data from the BT sensor
    @POST("post/status")
    fun setLastMailboxStatus(@Body data: PostUpdateStatus): Call<SetLastMailboxStatus>

    // Get all statuses from the last 14 days, for logging purposes
    @GET("posts")
    fun getRecentMailboxStatus(): Call<MutableList<PostLogEntry>>

    // Register that the mail has been picked up by the current user
    @POST("posts")
    fun setPostPickedUp(
        @Body pickupStatus: pickupStatus
    ): Call<SetPostPickedUpResponse>

    // Delete a mailbox status update by ID
    @DELETE("posts/{id}")
    fun deleteMailboxStatusById(@Path("id") post_id: Int): Call<DeleteMailboxStatusResponse>

}