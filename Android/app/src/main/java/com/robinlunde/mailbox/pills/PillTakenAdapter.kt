package com.robinlunde.mailbox.pills

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.robinlunde.mailbox.R
import com.robinlunde.mailbox.datamodel.pill.Pill

class PillTakenAdapter(
    private val frag: Fragment,
    val data: MutableList<Pill>,
    ctx: Context,
    private val spinner: Spinner
) : BaseAdapter() {
    private val inflater: LayoutInflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val pill = data[position]
        val view: View =
            convertView ?: inflater.inflate(R.layout.custom_dropdown_layout, parent, false)

        val color = view.findViewById<TextView>(R.id.pill_color)
        val num = view.findViewById<TextView>(R.id.pill_num)
        val name = view.findViewById<TextView>(R.id.pill_name)
        num.text = frag.getString(R.string.pill_num, position)
        name.text = pill.name
        color.setBackgroundColor(pill.color)
        view.setPadding(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 5.toFloat(), frag.resources.displayMetrics
            ).toInt()
        )
        view.setOnClickListener {
            spinner.setSelection(position)
        }

        return view
    }

    override fun getItem(position: Int): Pill {
        return data[position]
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}