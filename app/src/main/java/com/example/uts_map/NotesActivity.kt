package com.example.uts_map

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotesActivity : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        titleEditText = findViewById(R.id.et_note_title)
        contentEditText = findViewById(R.id.et_note_content)
        val saveButton = findViewById<Button>(R.id.btn_save)

        val category = intent.getStringExtra("CATEGORY") ?: return

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val content = contentEditText.text.toString()
            saveNoteToFirestore(title, content, category)
        }
    }

    private fun saveNoteToFirestore(title: String, content: String, category: String) {
        val user = auth.currentUser?.email ?: return
        val date = Timestamp.now()

        firestore.collection("notes")
            .orderBy("id", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                var newId = 1
                if (!documents.isEmpty) {
                    val lastId = documents.documents[0].getLong("id") ?: 0
                    newId = lastId.toInt() + 1
                }

                val note = hashMapOf(
                    "category" to category,
                    "content" to content,
                    "date" to date,
                    "id" to newId,
                    "title" to title,
                    "user" to user
                )

                firestore.collection("notes")
                    .add(note)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Note saved successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show()
                    }
            }
    }
}