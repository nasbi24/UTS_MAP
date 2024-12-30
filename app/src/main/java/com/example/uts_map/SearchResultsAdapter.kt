package com.example.uts_map

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class SearchResultsAdapter(
    private val context: Context,
    private val notes: List<Note>,
    private val onNoteClick: (Note) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.card_view)
        val titleTextView: TextView = view.findViewById(R.id.tv_card_title)
        val contentTextView: TextView = view.findViewById(R.id.tv_card_content)
        val categoryTextView: TextView = view.findViewById(R.id.tv_card_category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_note_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notes[position]
        holder.titleTextView.text = note.title
        holder.contentTextView.text = note.content
        holder.categoryTextView.text = note.category
        holder.cardView.setOnClickListener { onNoteClick(note) }
    }

    override fun getItemCount(): Int {
        return notes.size
    }
}