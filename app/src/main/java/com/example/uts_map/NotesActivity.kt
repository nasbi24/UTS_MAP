package com.example.uts_map

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.bumptech.glide.Glide
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.target.Target

@GlideModule
class MyAppGlideModule : AppGlideModule()

class NotesActivity : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var noteId: String
    private lateinit var category: String
    private lateinit var fragmentContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        titleEditText = findViewById(R.id.et_note_title)
        contentEditText = findViewById(R.id.et_note_content)
        val saveButton = findViewById<Button>(R.id.btn_save)
        val addImageButton = findViewById<Button>(R.id.btn_add_image)
        val backButton = findViewById<TextView>(R.id.back)
        fragmentContainer = findViewById(R.id.fragment_container)

        category = intent.getStringExtra("CATEGORY") ?: return
        createNewNote()

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val content = contentEditText.text.toString()
            saveNoteToFirestore(title, content, category)
        }

        addImageButton.setOnClickListener {
            slideUpFragment()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun createNewNote() {
        val user = auth.currentUser?.email ?: return
        val noteTitle = "New $category"
        val note = hashMapOf(
            "user" to user,
            "date" to Timestamp.now(),
            "category" to category,
            "title" to noteTitle,
            "content" to ""
        )

        firestore.collection("notes")
            .add(note)
            .addOnSuccessListener { documentReference ->
                noteId = documentReference.id
                titleEditText.setText(noteTitle)
            }
    }

    private fun saveNoteToFirestore(title: String, content: String, category: String) {
        val user = auth.currentUser?.email ?: return
        val date = Timestamp.now()

        val note = hashMapOf(
            "category" to category,
            "content" to content,
            "date" to date,
            "title" to title,
            "user" to user
        )

        firestore.collection("notes")
            .document(noteId)
            .set(note)
            .addOnSuccessListener {
                Toast.makeText(this, "Note saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show()
            }
    }

    private fun slideUpFragment() {
        val height = fragmentContainer.height
        val slideUp = ObjectAnimator.ofFloat(fragmentContainer, "translationY", height.toFloat(), 0f)
        slideUp.duration = 300
        slideUp.start()

        slideUp.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                showImageUploadFragment()
            }
        })
    }

    private fun slideDownFragment() {
        val height = fragmentContainer.height
        val slideDown = ObjectAnimator.ofFloat(fragmentContainer, "translationY", 0f, height.toFloat())
        slideDown.duration = 300
        slideDown.start()
    }

    private fun showImageUploadFragment() {
        val fragment = ImageUploadFragment().apply {
            arguments = Bundle().apply {
                putString("NOTE_ID", noteId)
            }
        }
        supportFragmentManager.commit {
            replace(R.id.fragment_container, fragment)
            addToBackStack(null)
        }
    }

    fun addImageToLayout(imagePath: String, description: String) {
        Log.d("NotesActivity", "Adding image to layout: $imagePath")
        val imageContainer = findViewById<LinearLayout>(R.id.image_container)

        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400 // Set a fixed height for the image
            ).apply {
                weight = 1f
                width = 400
            }
            adjustViewBounds = true
        }
        Glide.with(this)
            .load(imagePath)
            .override(Target.SIZE_ORIGINAL) // Load the original image size
            .into(imageView)

        val textView = TextView(this).apply {
            text = description
        }

        val deleteButton = Button(this).apply {
            text = "Delete"
            setOnClickListener {
                deleteImage(imagePath)
            }
        }

        val imageLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(imageView)
            addView(textView)
            addView(deleteButton)
        }

        imageContainer.addView(imageLayout)
    }

    private fun deleteImage(imagePath: String) {
        // Delete from Firebase Storage
        val storageRef = storage.getReferenceFromUrl(imagePath)
        storageRef.delete().addOnSuccessListener {
            // Delete from Firestore
            firestore.collection("images")
                .whereEqualTo("image_path", imagePath)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        firestore.collection("images").document(document.id).delete()
                    }
                    Toast.makeText(this, "Image deleted successfully", Toast.LENGTH_SHORT).show()
                    // Refresh the images view
                    loadImages()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to delete image info", Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to delete image from storage", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadImages() {
        val imageContainer = findViewById<LinearLayout>(R.id.image_container)
        imageContainer.removeAllViews()

        firestore.collection("images")
            .whereEqualTo("note_id", noteId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val imagePath = document.getString("image_path") ?: continue
                    val description = document.getString("description") ?: ""
                    addImageToLayout(imagePath, description)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load images", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeImageFromLayout(imagePath: String) {
        val imageContainer = findViewById<LinearLayout>(R.id.image_container)
        for (i in 0 until imageContainer.childCount) {
            val imageLayout = imageContainer.getChildAt(i) as LinearLayout
            val imageView = imageLayout.getChildAt(0) as ImageView
            if (Glide.with(this).load(imagePath).equals(imageView.drawable)) {
                imageContainer.removeViewAt(i)
                break
            }
        }
    }
}