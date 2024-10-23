package com.example.uts_map

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class NotesActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        // Inisialisasi EditText
        titleEditText = findViewById(R.id.et_note_title)
        contentEditText = findViewById(R.id.et_note_content)

        // Terima data dari Intent
        val noteTitle = intent.getStringExtra("NOTE_TITLE")
        val noteContent = intent.getStringExtra("NOTE_CONTENT")

        // Set judul dan isi yang diterima ke EditText
        titleEditText.setText(noteTitle)
        contentEditText.setText(noteContent)

        val saveButton = findViewById<TextView>(R.id.back)

        // Atur klik listener pada tombol save
        saveButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val content = contentEditText.text.toString()

            // Simpan data ke SharedPreferences
            val sharedPreferences = getSharedPreferences("NotesApp", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("NOTE_TITLE", title)
            editor.putString("NOTE_CONTENT", content)
            editor.apply()

            // Kembali ke HomeFragment setelah menyimpan
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("NAVIGATE_TO_HOME", true)
            startActivity(intent)

            finish() // Tutup NotesActivity
        }
    }
}