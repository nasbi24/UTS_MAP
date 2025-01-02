package com.example.uts_map

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditNoteActivity : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var noteId: String
    private lateinit var fragmentContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        titleEditText = findViewById(R.id.et_note_title)
        contentEditText = findViewById(R.id.et_note_content)
        categorySpinner = findViewById(R.id.spinner_note_category)
        fragmentContainer = findViewById(R.id.fragment_container)
        val saveButton = findViewById<ImageButton>(R.id.btn_save)
        val backButton = findViewById<ImageButton>(R.id.back)
        val addImageButton = findViewById<ImageButton>(R.id.btn_add_image)
        val addReminderButton = findViewById<ImageButton>(R.id.btn_add_reminder)

        // Set up the category spinner
        val categories = arrayOf("Interesting Idea", "Goals", "Routine Tasks", "Guidance", "Buy Something")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        noteId = intent.getStringExtra("NOTE_ID") ?: return
        loadNoteDetails(noteId)
        loadImages()
        loadReminders()

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val content = contentEditText.text.toString()
            val category = categorySpinner.selectedItem.toString()
            saveNoteToFirestore(title, content, category)
        }

        backButton.setOnClickListener {
            finish()
        }

        addImageButton.setOnClickListener {
            slideUpFragment()
        }

        addReminderButton.setOnClickListener {
            slideUpReminderFragment()
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
        val fragment = ImageEditUploadFragment().apply {
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
        Log.d("EditNoteActivity", "Adding image to layout: $imagePath")
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

    private fun slideUpReminderFragment() {
        val height = fragmentContainer.height
        val slideUp = ObjectAnimator.ofFloat(fragmentContainer, "translationY", height.toFloat(), 0f)
        slideUp.duration = 300
        slideUp.start()

        slideUp.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                showReminderUploadFragment()
            }
        })
    }

    private fun showReminderUploadFragment() {
        val fragment = EditNoteReminderUploadFragment().apply {
            arguments = Bundle().apply {
                putString("NOTE_ID", noteId)
            }
        }
        supportFragmentManager.commit {
            replace(R.id.fragment_container, fragment)
            addToBackStack(null)
        }
    }

    fun addReminderToLayout(date: String, title: String, description: String) {
        val reminderContainer = findViewById<LinearLayout>(R.id.reminder_container)

        val textView = TextView(this).apply {
            text = "$date - $title: $description"
        }

        val deleteButton = Button(this).apply {
            text = "Delete"
            setOnClickListener {
                deleteReminder(date, title, description)
            }
        }

        val reminderLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(textView)
            addView(deleteButton)
        }

        reminderContainer.addView(reminderLayout)
    }

    private fun deleteReminder(date: String, title: String, description: String) {
        val remindersCollection = firestore.collection("reminders")
        remindersCollection
            .whereEqualTo("date", date)
            .whereEqualTo("title", title)
            .whereEqualTo("description", description)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    remindersCollection.document(document.id).delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Reminder deleted successfully", Toast.LENGTH_SHORT).show()
                            // Optionally, remove the reminder from the layout
                            removeReminderFromLayout(date, title, description)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to delete reminder", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to find reminder", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeReminderFromLayout(date: String, title: String, description: String) {
        val reminderContainer = findViewById<LinearLayout>(R.id.reminder_container)
        for (i in 0 until reminderContainer.childCount) {
            val reminderLayout = reminderContainer.getChildAt(i) as LinearLayout
            val textView = reminderLayout.getChildAt(0) as TextView
            if (textView.text == "$date - $title: $description") {
                reminderContainer.removeViewAt(i)
                break
            }
        }
    }

    private fun loadReminders() {
        val reminderContainer = findViewById<LinearLayout>(R.id.reminder_container)
        reminderContainer.removeAllViews()

        firestore.collection("reminders")
            .whereEqualTo("note_id", noteId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val date = document.getString("date") ?: continue
                    val title = document.getString("title") ?: continue
                    val description = document.getString("description") ?: continue
                    addReminderToLayout(date, title, description)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load reminders", Toast.LENGTH_SHORT).show()
            }
    }


}