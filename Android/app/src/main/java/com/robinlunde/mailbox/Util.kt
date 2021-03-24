package com.robinlunde.mailbox

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Util {
    class TextItemViewHolder(val textView: TextView): RecyclerView.ViewHolder(textView)
    private var httpReq: HttpRequestLib? = null
    fun init(httpRequests: HttpRequestLib){
        httpReq = httpRequests
    }
}