package com.example.uts_map

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

class HomeFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Inisialisasi SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("NotesApp", Context.MODE_PRIVATE)

        // Ambil data catatan dari SharedPreferences
        val noteTitle = sharedPreferences.getString("NOTE_TITLE", "No Title")
        val noteContent = sharedPreferences.getString("NOTE_CONTENT", "")

        // Buat dan tampilkan card dengan data yang diambil
        val containerLayout = view.findViewById<LinearLayout>(R.id.fragment_container_interesting_idea)
        val cardView = createPinnedNoteCard(noteTitle, noteContent, "Interesting Idea")

        // Tambahkan card ke container
        containerLayout.addView(cardView)

        // Tambahkan listener klik pada card
        cardView.setOnClickListener {
            val intent = Intent(requireContext(), NotesActivity::class.java)
            intent.putExtra("NOTE_TITLE", noteTitle)
            intent.putExtra("NOTE_CONTENT", noteContent)
            startActivity(intent)
        }

        return view
    }

    // Fungsi untuk membuat card view dengan layout item_pinned_note_card.xml
    private fun createPinnedNoteCard(title: String?, content: String?, category: String?): View {
        val cardView = layoutInflater.inflate(R.layout.item_note_card, null) as CardView

        val titleTextView = cardView.findViewById<TextView>(R.id.tv_card_title)
        val contentTextView = cardView.findViewById<TextView>(R.id.tv_card_content)
        val categoryTextView = cardView.findViewById<TextView>(R.id.tv_card_category)

        // Set data ke elemen card
        titleTextView.text = "Pinned Notes" // Judul tetap "Pinned Notes"
        contentTextView.text = content ?: "No Content"
        categoryTextView.text = category ?: "No Category"

        return cardView
    }
}