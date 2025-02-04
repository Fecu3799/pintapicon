package com.example.pintapiconv3.adapter

import android.graphics.Typeface
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.utils.Const.Entities.MATCHES
import com.example.pintapiconv3.utils.Const.Entities.RESERVATIONS
import com.example.pintapiconv3.utils.Const.Entities.USERS

class DynamicReportAdapter(
    private var columnTitles: List<String> = emptyList(),
    private var data: List<DynamicReportItem> = emptyList(),
    private var entity: String = ""
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_HEADER = 0
    private val VIEW_TYPE_ITEM = 1

    private val columnWidths = mapOf(
        RESERVATIONS to listOf(250, 200, 200, 200, 200, 250), // Ancho en px por columna para Reservas
        MATCHES to listOf(250, 200, 230, 250, 250),           // Ancho en px por columna para Partidos
        USERS to listOf(300, 300, 200, 200, 210)             // Ancho en px por columna para Usuarios
    )

    fun setData(columnTitles: List<String>, data: List<DynamicReportItem>, entity: String) {
        this.columnTitles = columnTitles
        this.data = data
        this.entity = entity
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_HEADER else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dynamic_report, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dynamic_report, parent, false)
            DynamicViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.bind(columnTitles, entity)
        } else if (holder is DynamicViewHolder) {
            val item = data[position - 1]
            holder.bind(item, columnTitles, entity)
        }
    }

    override fun getItemCount(): Int = data.size + 1

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rowContainer: LinearLayout = itemView.findViewById(R.id.row_container)

        fun bind(columnTitles: List<String>, entity: String) {
            rowContainer.removeAllViews()


            val widths = columnWidths[entity] ?: List(columnTitles.size) {
                itemView.context.resources.displayMetrics.widthPixels / columnTitles.size
            }

            columnTitles.forEachIndexed { index, title ->
                val textView = TextView(itemView.context).apply {
                    text = title
                    layoutParams = LinearLayout.LayoutParams(
                        widths.getOrElse(index) {
                            itemView.context.resources.displayMetrics.widthPixels / columnTitles.size
                        },
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginEnd = 8
                    }
                    setPadding(8, 8, 8, 8)
                    setTextColor(itemView.context.getColor(R.color.black))
                    setTypeface(null, Typeface.BOLD)
                    gravity = Gravity.CENTER
                }
                rowContainer.addView(textView)
            }
        }

    }

    inner class DynamicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rowContainer: LinearLayout = itemView.findViewById(R.id.row_container)

        fun bind(item: DynamicReportItem, columnTitles: List<String>, entity: String) {
            rowContainer.removeAllViews()

            val widths = columnWidths[entity] ?: List(columnTitles.size) {
                itemView.context.resources.displayMetrics.widthPixels / columnTitles.size
            }

            item.fields.forEachIndexed { index, field ->
                val textView = TextView(itemView.context).apply {
                    text = field
                    layoutParams = LinearLayout.LayoutParams(
                        widths.getOrElse(index) {
                            itemView.context.resources.displayMetrics.widthPixels / columnTitles.size
                        },
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginEnd = 8
                    }
                    setPadding(8, 8, 8, 8)
                    setTextColor(itemView.context.getColor(R.color.black))
                    setLines(1)
                    ellipsize = TextUtils.TruncateAt.END
                    gravity = Gravity.CENTER
                }
                rowContainer.addView(textView)
            }
        }
    }
}

data class DynamicReportItem(
    val fields: List<String>
)