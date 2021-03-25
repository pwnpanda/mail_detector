package com.robinlunde.mailbox

import android.content.Context
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Util (context: Context){
    class TextItemViewHolder(val textView: TextView): RecyclerView.ViewHolder(textView)
    public val httpRequests = HttpRequestLib(context)

}