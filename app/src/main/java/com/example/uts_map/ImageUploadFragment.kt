package com.example.uts_map

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class ImageUploadFragment : Fragment() {
    private lateinit var storage: FirebaseStorage
    private lateinit var imageView: ImageView
    private lateinit var descriptionEditText: EditText
    private var imageUri: Uri? = null
    private lateinit var noteId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_image_upload, container, false)
        storage = FirebaseStorage.getInstance()

        imageView = view.findViewById(R.id.image_preview)
        descriptionEditText = view.findViewById(R.id.et_image_description)
        val uploadButton = view.findViewById<Button>(R.id.btn_upload_image)

        uploadButton.setOnClickListener {
            uploadImage()
        }

        imageView.setOnClickListener {
            openImagePicker()
        }

        noteId = arguments?.getString("NOTE_ID") ?: ""

        return view
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            imageView.setImageURI(imageUri)
        }
    }

    private fun uploadImage() {
        val uri = imageUri ?: return
        val description = descriptionEditText.text.toString()
        val storageRef = storage.reference.child("images/${UUID.randomUUID()}")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    saveImageInfo(downloadUri.toString(), description)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveImageInfo(imagePath: String, description: String) {
        val image = hashMapOf(
            "description" to description,
            "image_path" to imagePath,
            "note_id" to noteId
        )

        FirebaseFirestore.getInstance().collection("images")
            .add(image)
            .addOnSuccessListener {
                Toast.makeText(context, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                (activity as NotesActivity).addImageToLayout(imagePath, description)
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to save image info", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 1
    }
}