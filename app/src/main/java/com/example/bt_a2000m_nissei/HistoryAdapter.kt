package com.example.bt_a2000m_nissei

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bt_a2000m_nissei.data.db.HistoryItem
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryAdapter(private var historyItems: List<HistoryItem>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMachine: TextView = view.findViewById(android.R.id.text1)
        val tvDetails: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = historyItems[position]
        holder.tvMachine.text = "${item.machineName} (${item.machineCode})"
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.tvDetails.text = "Lúc: ${sdf.format(item.performedAt)} - ${item.itemCount} hạng mục"
    }

    override fun getItemCount() = historyItems.size

    fun updateData(newHistoryItems: List<HistoryItem>) {
        historyItems = newHistoryItems
        notifyDataSetChanged()
    }
}
