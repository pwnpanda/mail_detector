package com.robinlunde.mailbox

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class PostRecyclerViewAdapter internal constructor(data: List<SinglePostEntry>, context: Context, val clickListener: (Int) -> Unit) :
    RecyclerView.Adapter<PostRecyclerViewAdapter.ViewHolder>() {
    private val mData: List<SinglePostEntry> = data
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mClickListener: ItemClickListener? = null

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = mInflater.inflate(R.layout.recyclerview_row, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val postData = mData[position]
        holder.postUser.text = postData.username
        val deliverDate = postData.delivered.split("T")[0].replace("-", ".")
        val deliverTime = postData.delivered.split("T")[1].subSequence(0,8).toString()
        val pickupDate = postData.pickup.split("T")[0].replace("-", ".")
        val pickupTime = postData.pickup.split("T")[1].subSequence(0,8).toString()
        holder.postDeliveredDate.text = deliverDate
        holder.postDeliveredTime.text = deliverTime
        holder.postPickupTime.text = pickupTime
        holder.postPickupDate.text = pickupDate
        // Calling the clickListener sent by the constructor
        holder.myOwnButton.setOnClickListener { clickListener(position) }
    }

    // total number of rows
    override fun getItemCount(): Int {
        return mData.size
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var myOwnButton: ImageButton = itemView.findViewById(R.id.delete_button)
        var postUser: TextView = itemView.findViewById(R.id.post_user)
        var postDeliveredTime: TextView = itemView.findViewById(R.id.post_deliver_time)
        var postDeliveredDate: TextView = itemView.findViewById(R.id.post_deliver_date)
        var postPickupTime: TextView = itemView.findViewById(R.id.post_pickup_time)
        var postPickupDate: TextView = itemView.findViewById(R.id.post_pickup_date)

        override fun onClick(view: View?) {
            if (mClickListener != null) mClickListener!!.onItemClick(view, adapterPosition)
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    // convenience method for getting data at click position
    fun getItem(id: Int): String {
        return mData[id].id.toString()
    }

    // allows clicks events to be caught
    fun setClickListener(itemClickListener: ItemClickListener?) {
        mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

}