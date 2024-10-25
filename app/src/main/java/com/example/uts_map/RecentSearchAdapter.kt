package com.example.uts_map

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecentSearchAdapter(
    private val context: Context,
    private val searches: List<String>,
    private val onRemoveClick: (String) -> Unit,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<RecentSearchAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val searchTextView: TextView = view.findViewById(R.id.searchTextView)
        val removeButton: ImageButton = view.findViewById(R.id.removeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_recent_search, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val search = searches[position]
        holder.searchTextView.text = search
        holder.removeButton.setOnClickListener { onRemoveClick(search) }
        holder.itemView.setOnClickListener { onItemClick(search) }
    }

    override fun getItemCount(): Int {
        return searches.size
    }
}