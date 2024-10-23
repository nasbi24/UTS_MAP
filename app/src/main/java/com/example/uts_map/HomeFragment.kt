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
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase instances
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("NotesApp", Context.MODE_PRIVATE)

        // Fetch and display notes
        fetchAndDisplayNotes(view)
    }

    private fun fetchAndDisplayNotes(view: View) {
        val user = auth.currentUser?.email ?: return

        firestore.collection("notes")
            .whereEqualTo("user", user)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val title = document.getString("title")
                    val content = document.getString("content")
                    val category = document.getString("category")
                    val date = document.getTimestamp("date")?.toDate()?.toString()

                    val cardView = createPinnedNoteCard(title, content, category)
                    if (cardView != null) {
                        val containerLayout = getCategoryContainer(view, category)
                        containerLayout?.addView(cardView)

                        cardView.setOnClickListener {
                            val bundle = Bundle().apply {
                                putString("NOTE_TITLE", title)
                                putString("NOTE_CONTENT", content)
                                putString("NOTE_CATEGORY", category)
                                putString("NOTE_DATE", date)
                            }
                            findNavController().navigate(R.id.action_homeFragment_to_noteDetailFragment, bundle)
                        }
                    }
                }
            }
    }

    private fun getCategoryContainer(view: View, category: String?): LinearLayout? {
        return when (category) {
            "Interesting Idea" -> view.findViewById(R.id.fragment_container_interesting_idea)
            "Goals" -> view.findViewById(R.id.fragment_container_goals)
            "Routine Tasks" -> view.findViewById(R.id.fragment_container_routine_task)
            "Guidance" -> view.findViewById(R.id.fragment_container_guidance)
            "Buy Something" -> view.findViewById(R.id.fragment_container_buy_something)
            else -> null
        }
    }

    private fun createPinnedNoteCard(title: String?, content: String?, category: String?): CardView? {
        if (!isAdded) return null

        val inflater = LayoutInflater.from(requireContext())
        val cardView = inflater.inflate(R.layout.item_note_card, null) as CardView

        val titleTextView = cardView.findViewById<TextView>(R.id.tv_card_title)
        val contentTextView = cardView.findViewById<TextView>(R.id.tv_card_content)
        val categoryTextView = cardView.findViewById<TextView>(R.id.tv_card_category)

        titleTextView.text = title
        contentTextView.text = content
        categoryTextView.text = category

        return cardView
    }
}