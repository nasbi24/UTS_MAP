package com.example.uts_map

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditNoteActivity : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var noteId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        titleEditText = findViewById(R.id.et_note_title)
        contentEditText = findViewById(R.id.et_note_content)
        categorySpinner = findViewById(R.id.spinner_note_category)
        val saveButton = findViewById<Button>(R.id.btn_save)
        val backButton = findViewById<TextView>(R.id.back)

        // Set up the category spinner
        val categories = arrayOf("Interesting Idea", "Goals", "Routine Tasks", "Guidance", "Buy Something")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        noteId = intent.getStringExtra("NOTE_ID") ?: return
        loadNoteDetails(noteId)

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val content = contentEditText.text.toString()
            val category = categorySpinner.selectedItem.toString()
            saveNoteToFirestore(title, content, category)
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadNoteDetails(noteId: String) {
        firestore.collection("notes").document(noteId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    titleEditText.setText(document.getString("title"))
                    contentEditText.setText(document.getString("content"))
                    val category = document.getString("category")
                    val categories = arrayOf("Interesting Idea", "Goals", "Routine Tasks", "Guidance", "Buy Something")
                    val index = categories.indexOf(category)
                    if (index >= 0) {
                        categorySpinner.setSelection(index)
                    }
                }
            }
    }

    private fun saveNoteToFirestore(title: String, content: String, category: String) {
        val user = auth.currentUser?.email ?: return
        val date = Timestamp.now()

        val note = hashMapOf(
            "content" to content,
            "date" to date,
            "title" to title,
            "category" to category,
            "user" to user
        )

        firestore.collection("notes")
            .document(noteId)
            .set(note)
            .addOnSuccessListener {
                Toast.makeText(this, "Note updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update note", Toast.LENGTH_SHORT).show()
            }
    }
}