package com.robinlunde.mailbox

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.lang.ClassCastException

private val ITEM_VIEW_TYPE_HEADER = 0
private val ITEM_VIEW_TYPE_ITEM = 1
/*
class PostRecyclerViewAdapter internal constructor(data: List<PostEntry>, context: Context, val clickListener: (Int) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val mData: List<PostEntry> = data
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mClickListener: PostEntryDiffCallback.PostEntryListener.ItemClickListener? = null




/*
    fun addHeaderAndSubmitList(list: List<DataItem.PostItem>?){
        val items = when (list) -> {
            null -> listOf(DataItem.Header)
            else -> listOf(DataItem.Header) + list.map(DataItem.PostItem(it))
        }
    }
    */
    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) -> run {
            is RecyclerView.ViewHolder -> run {
                val postData = mData[position]
                holder.postUser.text = postData.username
                val deliverDate = postData.delivered.split("T")[0].replace("-", ".")
                val deliverTime = postData.delivered.split("T")[1].subSequence(0, 8).toString()
                val pickupDate = postData.pickup.split("T")[0].replace("-", ".")
                val pickupTime = postData.pickup.split("T")[1].subSequence(0, 8).toString()
                holder.postDeliveredDate.text = deliverDate
                holder.postDeliveredTime.text = deliverTime
                holder.postPickupTime.text = pickupTime
                holder.postPickupDate.text = pickupDate
                // Calling the clickListener sent by the constructor
                holder.myOwnButton.setOnClickListener { clickListener(position) }
            }
        }
    }

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType){
            ITEM_VIEW_TYPE_HEADER -> TextViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent) /*{
                val view: View = mInflater.inflate(R.layout.recyclerview_row, parent, false)
                return RecyclerView.ViewHolder(view)
            }*/
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is DataItem.Header -> ITEM_VIEW_TYPE_HEADER
            is DataItem.PostItem -> ITEM_VIEW_TYPE_ITEM
        }
    }

    class TextViewHolder(view: View): RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup): TextViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.header_recyclerview, parent, false)
                return TextViewHolder(view)
            }
        }
    }

    // FIX TODO
    // stores and recycles views as they are scrolled off screen
    class ViewHolder internal constructor(val binding: ListPostEntryBinding) : RecyclerView.ViewHolder(binding.root),
        fun bind(item: PostEntry, clickListener: PostEntryListener) {
            // Biggest uncertainty
            binding.post = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListPostEntryBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        /*View.OnClickListener {
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
        }*/
    }

    /*
    // total number of rows
    override fun getItemCount(): Int {
        return mData.size
    }
     */

    class PostEntryDiffCallback: DiffUtil.ItemCallback<DataItem>() {
        override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem.id === newItem.id
        }

        override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return newItem === oldItem
        }
    }

    class PostEntryListener(val clickListener: (postId: Int) -> Unit) {
        fun onClick(entry: PostEntry) = clickListener(entry.id)
    }

    sealed class DataItem(){
        abstract val id: Int
        data class PostItem(val postEntry: PostEntry): DataItem() {
            override val id = postEntry.id
        }
        object Header: DataItem() {
            override val id = Int.MIN_VALUE
        }
    }
}*/