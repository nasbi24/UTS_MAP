package com.example.uts_map

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage

class HomeFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        view.findViewById<TextView>(R.id.view_all_pinned_notes).setOnClickListener {
            navigateToViewAll("pinned_notes")
        }

        view.findViewById<TextView>(R.id.view_all_interesting_idea).setOnClickListener {
            navigateToViewAll("interesting_idea")
        }

        view.findViewById<TextView>(R.id.view_all_goals).setOnClickListener {
            navigateToViewAll("goals")
        }

        view.findViewById<TextView>(R.id.view_all_routine_task).setOnClickListener {
            navigateToViewAll("routine_task")
        }

        view.findViewById<TextView>(R.id.view_all_guidance).setOnClickListener {
            navigateToViewAll("guidance")
        }

        view.findViewById<TextView>(R.id.view_all_buy_something).setOnClickListener {
            navigateToViewAll("buy_something")
        }

        return view
    }

    private fun navigateToViewAll(type: String) {
        val bundle = Bundle().apply {
            putString("type", type)
        }
        findNavController().navigate(R.id.action_homeFragment_to_viewAllFragment, bundle)
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

        // Clear existing views in each category container
        val categories = arrayOf(
            R.id.fragment_container_interesting_idea,
            R.id.fragment_container_goals,
            R.id.fragment_container_routine_task,
            R.id.fragment_container_guidance,
            R.id.fragment_container_buy_something,
            R.id.fragment_container_pinned_notes
        )
        for (categoryId in categories) {
            view.findViewById<LinearLayout>(categoryId).removeAllViews()
        }

        // Fetch and display pinned notes
        firestore.collection("pinned_notes")
            .whereEqualTo("user", user)
            .get()
            .addOnSuccessListener { pinnedDocuments ->
                for (pinnedDocument in pinnedDocuments) {
                    val noteId = pinnedDocument.getString("note_id") ?: continue
                    firestore.collection("notes").document(noteId).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val title = document.getString("title")
                                val content = document.getString("content")
                                val category = document.getString("category")
                                val date = document.getTimestamp("date")?.toDate()?.toString()

                                val cardView = createNoteCard(title, content, category)
                                if (cardView != null) {
                                    val containerLayout = view.findViewById<LinearLayout>(R.id.fragment_container_pinned_notes)
                                    containerLayout.addView(cardView)

                                    cardView.setOnClickListener {
                                        val bundle = Bundle().apply {
                                            putString("NOTE_ID", noteId)
                                            putString("NOTE_TITLE", title)
                                            putString("NOTE_CONTENT", content)
                                            putString("NOTE_CATEGORY", category)
                                            putString("NOTE_DATE", date)
                                        }
                                        findNavController().navigate(R.id.action_homeFragment_to_noteDetailFragment, bundle)
                                    }

                                    cardView.setOnLongClickListener {
                                        showPopupMenu(it, noteId)
                                        true
                                    }
                                }
                            }
                        }
                }
            }

        // Fetch and display other notes
        firestore.collection("notes")
            .whereEqualTo("user", user)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val noteId = document.id
                    val title = document.getString("title")
                    val content = document.getString("content")
                    val category = document.getString("category")
                    val date = document.getTimestamp("date")?.toDate()?.toString()

                    val cardView = createNoteCard(title, content, category)
                    if (cardView != null) {
                        val containerLayout = getCategoryContainer(view, category)
                        containerLayout?.addView(cardView)

                        cardView.setOnClickListener {
                            val bundle = Bundle().apply {
                                putString("NOTE_ID", noteId)
                                putString("NOTE_TITLE", title)
                                putString("NOTE_CONTENT", content)
                                putString("NOTE_CATEGORY", category)
                                putString("NOTE_DATE", date)
                            }
                            findNavController().navigate(R.id.action_homeFragment_to_noteDetailFragment, bundle)
                        }

                        cardView.setOnLongClickListener {
                            showPopupMenu(it, noteId)
                            true
                        }
                    }
                }
            }
    }

    private fun showPopupMenu(view: View, noteId: String) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.note_options_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    val intent = Intent(requireContext(), EditNoteActivity::class.java).apply {
                        putExtra("NOTE_ID", noteId)
                    }
                    startActivity(intent)
                    true
                }
                R.id.action_delete -> {
                    showDeleteConfirmationDialog(noteId)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun showDeleteConfirmationDialog(noteId: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("Yes") { dialog, _ ->
                deleteNote(noteId)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun deleteNote(noteId: String) {
        val db = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()

        // Delete the note document from Firestore
        db.collection("notes").document(noteId)
            .delete()
            .addOnSuccessListener {
                // Query the images collection to find all images related to the note
                db.collection("images").whereEqualTo("note_id", noteId)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        for (document in querySnapshot.documents) {
                            val imagePath = document.getString("path") ?: continue
                            // Delete each image from Firebase Storage
                            storage.getReference(imagePath).delete()
                                .addOnSuccessListener {
                                    // Optionally, delete the image document from Firestore
                                    document.reference.delete()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(requireContext(), "Failed to delete image: $imagePath", Toast.LENGTH_SHORT).show()
                                }
                        }
                        Toast.makeText(requireContext(), "Note and related images deleted", Toast.LENGTH_SHORT).show()
                        fetchAndDisplayNotes(requireView())
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed to query related images", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to delete note", Toast.LENGTH_SHORT).show()
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

    private fun getCategoryColor(category: String?): Int {
        return when (category) {
            "Interesting Idea" -> ContextCompat.getColor(requireContext(), R.color.interesting_idea_color)
            "Goals" -> ContextCompat.getColor(requireContext(), R.color.goals_color)
            "Routine Tasks" -> ContextCompat.getColor(requireContext(), R.color.routine_tasks_color)
            "Guidance" -> ContextCompat.getColor(requireContext(), R.color.guidance_color)
            "Buy Something" -> ContextCompat.getColor(requireContext(), R.color.buy_something_color)
            else -> ContextCompat.getColor(requireContext(), R.color.default_category_color)
        }
    }

    private fun getCategoryFontColor(category: String?): Int {
        return when (category) {
            "Interesting Idea" -> ContextCompat.getColor(requireContext(), R.color.white)
            "Goals" -> ContextCompat.getColor(requireContext(), R.color.black)
            "Routine Tasks" -> ContextCompat.getColor(requireContext(), R.color.black)
            "Guidance" -> ContextCompat.getColor(requireContext(), R.color.white)
            "Buy Something" -> ContextCompat.getColor(requireContext(), R.color.white)
            else -> ContextCompat.getColor(requireContext(), R.color.default_category_color)
        }
    }

    private fun createNoteCard(title: String?, content: String?, category: String?): CardView? {
        if (!isAdded) return null

        val inflater = LayoutInflater.from(requireContext())
        val cardView = inflater.inflate(R.layout.item_note_card, null) as CardView

        // Set margin untuk CardView
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 8, 16, 8)
        cardView.layoutParams = layoutParams

        // Mengatur isi CardView
        val titleTextView = cardView.findViewById<TextView>(R.id.tv_card_title)
        val contentTextView = cardView.findViewById<TextView>(R.id.tv_card_content)
        val categoryTextView = cardView.findViewById<TextView>(R.id.tv_card_category)

        titleTextView.text = title
        contentTextView.text = content
        categoryTextView.text = category

        // Set background color based on category
        categoryTextView.setBackgroundColor(getCategoryColor(category))
        categoryTextView.setTextColor(getCategoryFontColor(category))


        return cardView
    }
}