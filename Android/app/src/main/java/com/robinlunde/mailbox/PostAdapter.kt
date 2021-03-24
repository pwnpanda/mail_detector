package com.robinlunde.mailbox

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PostAdapter: RecyclerView.Adapter<Util.TextItemViewHolder>()  {
    var data = listOf<PostEntry>()
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: Util.TextItemViewHolder, position: Int) {
        val item = data[position]
        holder.textView.text = item.username
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Util.TextItemViewHolder {
        val layoutInflater: LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.recyclerview_row, parent, false) as TextView
        return Util.TextItemViewHolder(view)
    }
}