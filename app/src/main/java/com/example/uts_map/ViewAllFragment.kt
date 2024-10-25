package com.example.uts_map

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ViewAllFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_all, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase instances
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Fetch and display notes based on the category
        val category = arguments?.getString("type")
        fetchAndDisplayNotes(view, category)
    }

    private fun fetchAndDisplayNotes(view: View, category: String?) {
        val user = auth.currentUser?.email ?: return

        // Clear existing views in the container
        val containerLayout = view.findViewById<LinearLayout>(R.id.fragment_large_thumbnail)
        containerLayout.removeAllViews()

        // Fetch and display notes based on the category
        firestore.collection("notes")
            .whereEqualTo("user", user)
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("ViewAllFragment", "No notes found for category: $category")
                } else {
                    for (document in documents) {
                        val noteId = document.id
                        val title = document.getString("title")
                        val content = document.getString("content")
                        val date = document.getTimestamp("date")?.toDate()?.toString()

                        val cardView = createNoteCard(title, content, category)
                        if (cardView != null) {
                            containerLayout.addView(cardView)
                            Log.d("ViewAllFragment", "Added note: $title")

                            cardView.setOnClickListener {
                                val bundle = Bundle().apply {
                                    putString("NOTE_ID", noteId)
                                    putString("NOTE_TITLE", title)
                                    putString("NOTE_CONTENT", content)
                                    putString("NOTE_CATEGORY", category)
                                    putString("NOTE_DATE", date)
                                }
                                findNavController().navigate(R.id.action_viewAllFragment_to_noteDetailFragment, bundle)
                            }

                            cardView.setOnLongClickListener {
                                showPopupMenu(it, noteId)
                                true
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to fetch notes", Toast.LENGTH_SHORT).show()
                Log.e("ViewAllFragment", "Error fetching notes", e)
            }
    }

    private fun showPopupMenu(view: View, noteId: String) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.note_options_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
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
                        fetchAndDisplayNotes(requireView(), arguments?.getString("type"))
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed to query related images", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to delete note", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createNoteCard(title: String?, content: String?, category: String?): CardView? {
        if (!isAdded) return null

        val inflater = LayoutInflater.from(requireContext())
        val cardView = inflater.inflate(R.layout.item_note_card, null) as CardView

        // Set margin for CardView
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 8, 16, 8)
        cardView.layoutParams = layoutParams

        // Set content for CardView
        val titleTextView = cardView.findViewById<TextView>(R.id.tv_card_title)
        val contentTextView = cardView.findViewById<TextView>(R.id.tv_card_content)
        val categoryTextView = cardView.findViewById<TextView>(R.id.tv_card_category)

        titleTextView.text = title
        contentTextView.text = content
        categoryTextView.text = category

        return cardView
    }
}