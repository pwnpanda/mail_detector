package com.robinlunde.mailbox.network

import com.robinlunde.mailbox.datamodel.PostLogEntry
import com.robinlunde.mailbox.datamodel.PostUpdateStatus
import retrofit2.http.*

interface ApiInterfaceMailNotifications {

    // Get last update for the mailbox-status from the Web-API (AKA last status submitted by others)
    @GET("/post/status")
    suspend fun getLastMailboxStatus(): PostUpdateStatus

    // Set the latest received mailbox-status, by forwarding received data from the BT sensor
    // TODO should not return boolean I think
    @POST("/post/status")
    suspend fun setLastMailboxStatus(data: PostUpdateStatus): Boolean

    // Get all statuses from the last 14 days, for logging purposes
    @GET("/posts")
    suspend fun getRecentMailboxStatus(): MutableList<PostLogEntry>

    // Register that the mail has been picked up by the current user
    // TODO should not return boolean I think
    @POST("/posts")
    suspend fun setPostPickedUp(timestamp: String): Boolean

    // Delete a mailbox status update by ID
    // TODO should not return boolean I think
    @DELETE("/posts/{id}")
    suspend fun deleteMailboxStatusById(@Path("id") post_id: Int): Boolean

}