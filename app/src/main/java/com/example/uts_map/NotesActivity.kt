package com.example.uts_map

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class NotesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        // Terima data kategori dari Intent
        val category = intent.getStringExtra("CATEGORY")

        // Tampilkan toast atau perbarui UI berdasarkan kategori
        Toast.makeText(this, "Category: $category", Toast.LENGTH_SHORT).show()

        // Anda bisa memperbarui UI di sini sesuai dengan kategori yang diterima
    }
}