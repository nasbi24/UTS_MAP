package com.example.uts_map

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView

class SearchResultsAdapter(
    private val context: Context,
    private val notes: List<Note>
) : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.tv_card_title)
        val contentTextView: TextView = view.findViewById(R.id.tv_card_content)
        val categoryTextView: TextView = view.findViewById(R.id.tv_card_category)
        val cardView: CardView = view.findViewById(R.id.card_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_note_card, parent, false)
        val layoutParams = view.layoutParams as RecyclerView.LayoutParams
        layoutParams.width = RecyclerView.LayoutParams.MATCH_PARENT
        layoutParams.height = 500
        layoutParams.setMargins(16, 8, 16, 8)
        view.layoutParams = layoutParams
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notes[position]
        holder.titleTextView.text = note.title
        holder.contentTextView.text = note.content
        holder.categoryTextView.text = note.category

        holder.cardView.setOnClickListener {
            val bundle = Bundle().apply {
                putString("NOTE_ID", note.id)
                putString("NOTE_TITLE", note.title)
                putString("NOTE_CONTENT", note.content)
                putString("NOTE_CATEGORY", note.category)
            }
            val navController = (context as FragmentActivity).findNavController(R.id.nav_host_fragment)
            navController.navigate(R.id.noteDetailFragment, bundle)
        }
    }

    override fun getItemCount(): Int {
        return notes.size
    }
}