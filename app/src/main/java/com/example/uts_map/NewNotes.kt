package com.example.uts_map

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class NewNotes : Fragment(R.layout.fragment_new_notes) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup listener for card clicks
        view.findViewById<View>(R.id.card_interesting_idea).setOnClickListener {
            navigateToNotesActivity("Interesting Idea")
        }

        view.findViewById<View>(R.id.card_goals).setOnClickListener {
            navigateToNotesActivity("Goals")
        }

        view.findViewById<View>(R.id.card_guidance).setOnClickListener {
            navigateToNotesActivity("Guidance")
        }

        view.findViewById<View>(R.id.card_buy_something).setOnClickListener {
            navigateToNotesActivity("Buy Something")
        }

        view.findViewById<View>(R.id.card_routine_tasks).setOnClickListener {
            navigateToNotesActivity("Routine Tasks")
        }
    }

    // Function to navigate to NotesActivity
    private fun navigateToNotesActivity(category: String) {
        val intent = Intent(requireContext(), NotesActivity::class.java)
        intent.putExtra("CATEGORY", category) // Send category data
        startActivity(intent)
    }
}