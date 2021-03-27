package com.robinlunde.mailbox

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

// TODO Add headers
// TODO add clickListener for delete button
class PostAdapter(val data: MutableList<PostLogEntry>): RecyclerView.Adapter<Util.LogItemViewHolder>()  {

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: Util.LogItemViewHolder, position: Int) {
        val item = data[position]
        holder.constraintLayout.findViewById<TextView>(R.id.post_user).text = item.username
        holder.constraintLayout.findViewById<TextView>(R.id.post_deliver_time).text = item.deliveredTime
        holder.constraintLayout.findViewById<TextView>(R.id.post_deliver_date).text = item.deliveredDate
        holder.constraintLayout.findViewById<TextView>(R.id.post_pickup_time).text = item.pickupTime
        holder.constraintLayout.findViewById<TextView>(R.id.post_pickup_date).text = item.pickupDate
    }
    /* TODO make sure we have data, else show error!
    * if (res != "") {
    *    renderRecyclerView(res)
    * } else {
    *   // Set error message in activity_log!
    *    findViewById<RecyclerView>(R.id.post_entries).visibility = View.INVISIBLE
    *    var error = findViewById<TextView>(R.id.error_logs)
    *    error.visibility = View.VISIBLE
    * }
    *
    */

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Util.LogItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.recyclerview_row, parent, false) as ConstraintLayout
        return Util.LogItemViewHolder(view)
    }
}


private val ITEM_VIEW_TYPE_HEADER = 0
private val ITEM_VIEW_TYPE_ITEM = 1
/*
class PostAdapter: (val clickListener: PostEntryListener) :
ListAdapter<DataItem, RecyclerView.ViewHolder>(PostEntryDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun addHeaderAndSubmitList(list: List<PostEntry>?){
        adapterScope.launch {
            val items = when (list) {
                null -> listOf(DataItem.Header)
                else -> listOf(DataItem.Header) + list.map { DataItem.PostItem(it) }
            }
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) -> {
            is ViewHolder -> {
                val postItem = getItem(position) as DataItem.PostItem
                holder.bind(postItem.postEntry, clickListener)
                /*
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
                 */
            }
        }
    }

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType){
            ITEM_VIEW_TYPE_HEADER -> TextViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
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

    // stores and recycles views as they are scrolled off screen
    class ViewHolder private constructor(val binding: ListPostEntryBinding) : RecyclerView.ViewHolder(binding.root) {

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
    }
}

class PostEntryDiffCallback: DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id === newItem.id
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
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
}*/