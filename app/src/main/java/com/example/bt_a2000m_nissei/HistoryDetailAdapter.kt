package com.example.bt_a2000m_nissei

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bt_a2000m_nissei.data.db.ChecklistItemType
import com.example.bt_a2000m_nissei.data.db.HistoryDetailRow
import com.example.bt_a2000m_nissei.data.db.InspectionPeriod

class HistoryDetailAdapter(
    private var rows: List<HistoryDetailRow>
) : RecyclerView.Adapter<HistoryDetailAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvLeft: TextView = v.findViewById(R.id.tvLeft)
        val tvTitle: TextView = v.findViewById(R.id.tvTitle)
        val tvResult: TextView = v.findViewById(R.id.tvResult)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_detail_row, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = rows[position]

        holder.tvLeft.text = "${r.period.name}\n#${r.displayOrder}"

        holder.tvTitle.text = r.itemContent

        holder.tvResult.text = formatResult(r)
    }

    override fun getItemCount() = rows.size

    fun updateData(newRows: List<HistoryDetailRow>) {
        rows = newRows
        notifyDataSetChanged()
    }

    private fun formatResult(r: HistoryDetailRow): String {
        return when (r.itemType) {
            ChecklistItemType.BOOLEAN -> when (r.resultBoolean) {
                true -> "Đạt (OK)"
                false -> "Không đạt (NG)"
                null -> "(chưa có)"
            }

            ChecklistItemType.INPUT_TEXT -> {
                val t = r.resultText ?: ""
                if (t.isBlank()) "(trống)" else t
            }

            ChecklistItemType.INPUT_NUMBER -> {
                r.resultNumber?.toString() ?: "(chưa có)"
            }
        }
    }
}
