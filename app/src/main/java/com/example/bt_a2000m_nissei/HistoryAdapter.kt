package com.example.bt_a2000m_nissei

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bt_a2000m_nissei.data.db.HistoryItem
import com.example.bt_a2000m_nissei.databinding.ListItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryAdapter(
    private var historyItems: List<HistoryItem>,
    private val onItemClick: (HistoryItem) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(val binding: ListItemHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = historyItems[position]
        holder.binding.tvMachineName.text = "${item.machineName} (${item.machineCode})"
        holder.binding.tvPeriod.text = item.periods.replace(",", " + ")
        holder.binding.tvItemCount.text = "Tổng cộng: ${item.itemCount} mục"
        holder.binding.tvPerformedBy.text = "Thực hiện bởi: ${item.performedByName}"

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.binding.tvDateTime.text = sdf.format(item.performedAt)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = historyItems.size

    fun updateData(newHistoryItems: List<HistoryItem>) {
        historyItems = newHistoryItems
        notifyDataSetChanged()
    }
}
