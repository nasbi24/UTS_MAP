package com.example.uts_map

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class NoteDetailFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var isPinned = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_note_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val title = arguments?.getString("NOTE_TITLE")
        val content = arguments?.getString("NOTE_CONTENT")
        val date = arguments?.getString("NOTE_DATE")
        val noteId = arguments?.getString("NOTE_ID")

        view.findViewById<TextView>(R.id.tv_note_title).text = title
        view.findViewById<TextView>(R.id.tv_note_date).text = date
        view.findViewById<TextView>(R.id.tv_note_content).text = content

        val imagesLayout = view.findViewById<LinearLayout>(R.id.ll_images)
        loadNoteImages(noteId, imagesLayout)

        val pinButton = view.findViewById<ImageButton>(R.id.btn_pin)
        val deleteButton = view.findViewById<ImageButton>(R.id.btn_delete)
        val editButton = view.findViewById<ImageButton>(R.id.btn_edit)

        checkIfPinned(noteId, pinButton)

        pinButton.setOnClickListener {
            if (isPinned) {
                unpinNote(noteId, pinButton)
            } else {
                pinNote(noteId, pinButton)
            }
        }

        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        editButton.setOnClickListener {
            val intent = Intent(requireContext(), EditNoteActivity::class.java).apply {
                putExtra("NOTE_ID", noteId)
            }
            startActivity(intent)
        }
        
    }

    private fun checkIfPinned(noteId: String?, pinButton: ImageButton) {
        val user = auth.currentUser?.email ?: return
        firestore.collection("pinned_notes")
            .whereEqualTo("note_id", noteId)
            .whereEqualTo("user", user)
            .get()
            .addOnSuccessListener { documents ->
                isPinned = !documents.isEmpty
                updatePinIcon(pinButton)
            }
    }

    private fun pinNote(noteId: String?, pinButton: ImageButton) {
        val user = auth.currentUser?.email ?: return
        val pinnedNote = hashMapOf(
            "note_id" to noteId,
            "user" to user
        )
        firestore.collection("pinned_notes")
            .add(pinnedNote)
            .addOnSuccessListener {
                isPinned = true
                updatePinIcon(pinButton)
                Toast.makeText(requireContext(), "Note pinned", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to pin note", Toast.LENGTH_SHORT).show()
            }
    }

    private fun unpinNote(noteId: String?, pinButton: ImageButton) {
        val user = auth.currentUser?.email ?: return
        firestore.collection("pinned_notes")
            .whereEqualTo("note_id", noteId)
            .whereEqualTo("user", user)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    firestore.collection("pinned_notes").document(document.id).delete()
                        .addOnSuccessListener {
                            isPinned = false
                            updatePinIcon(pinButton)
                            Toast.makeText(requireContext(), "Note unpinned", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Failed to unpin note", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    private fun updatePinIcon(pinButton: ImageButton) {
        if (isPinned) {
            pinButton.setImageResource(R.drawable.ic_pinned_note)
        } else {
            pinButton.setImageResource(R.drawable.ic_pin_note)
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("Yes") { dialog, _ ->
                deleteNote()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun deleteNote() {
        val noteId = arguments?.getString("NOTE_ID") ?: return
        val db = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()

        db.collection("notes").document(noteId)
            .delete()
            .addOnSuccessListener {
                db.collection("images").whereEqualTo("note_id", noteId)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        for (document in querySnapshot.documents) {
                            val imagePath = document.getString("path") ?: continue
                            storage.getReference(imagePath).delete()
                                .addOnSuccessListener {
                                    document.reference.delete()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(requireContext(), "Failed to delete image: $imagePath", Toast.LENGTH_SHORT).show()
                                }
                        }
                        Toast.makeText(requireContext(), "Note and related images deleted", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed to query related images", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to delete note", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadNoteImages(noteId: String?, imagesLayout: LinearLayout) {
        val db = FirebaseFirestore.getInstance()
        db.collection("images").whereEqualTo("note_id", noteId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    Log.d("NoteDetailFragment", "No images found for noteId: $noteId")
                } else {
                    for (document in querySnapshot.documents) {
                        val imagePath = document.getString("image_path")
                        val imageDescription = document.getString("description") ?: "No description"
                        if (imagePath != null) {
                            Log.d("NoteDetailFragment", "Loading image: $imagePath")
                            val imageView = ImageView(requireContext()).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    width = resources.displayMetrics.widthPixels / 2
                                    setMargins(0, 8, 0, 8)
                                }
                                adjustViewBounds = true
                                scaleType = ImageView.ScaleType.FIT_CENTER
                            }
                            Glide.with(this).load(imagePath).into(imageView)
                            imagesLayout.addView(imageView)

                            val descriptionView = TextView(requireContext()).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    setMargins(0, 4, 0, 8)
                                }
                                text = imageDescription
                            }
                            imagesLayout.addView(descriptionView)
                        } else {
                            Log.d("NoteDetailFragment", "Image path is null for document: ${document.id}")
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("NoteDetailFragment", "Failed to load images", e)
                Toast.makeText(requireContext(), "Failed to load images", Toast.LENGTH_SHORT).show()
            }
    }
}