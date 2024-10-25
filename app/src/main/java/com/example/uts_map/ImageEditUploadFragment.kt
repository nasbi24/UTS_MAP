package com.example.uts_map

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class ImageEditUploadFragment : Fragment() {
    private lateinit var storage: FirebaseStorage
    private lateinit var imageView: ImageView
    private lateinit var descriptionEditText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var contentLayout: LinearLayout
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
        progressBar = view.findViewById(R.id.progress_bar)
        contentLayout = view.findViewById(R.id.content_layout)
        val uploadButton = view.findViewById<Button>(R.id.btn_upload_image)
        val cancelButton = view.findViewById<Button>(R.id.btn_cancel)

        uploadButton.setOnClickListener {
            uploadImage()
        }

        cancelButton.setOnClickListener {
            parentFragmentManager.popBackStack()
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

        showProgressBar()
        disableInputs()

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    saveImageInfo(downloadUri.toString(), description)
                }
            }
            .addOnFailureListener {
                hideProgressBar()
                enableInputs()
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
                hideProgressBar()
                enableInputs()
                Toast.makeText(context, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                (activity as EditNoteActivity).addImageToLayout(imagePath, description)
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener {
                hideProgressBar()
                enableInputs()
                Toast.makeText(context, "Failed to save image info", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
        contentLayout.isEnabled = false
        contentLayout.alpha = 0.5f
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.GONE
        contentLayout.isEnabled = true
        contentLayout.alpha = 1.0f
    }

    private fun disableInputs() {
        imageView.isEnabled = false
        descriptionEditText.isEnabled = false
        view?.findViewById<Button>(R.id.btn_upload_image)?.isEnabled = false
        view?.findViewById<Button>(R.id.btn_cancel)?.isEnabled = false
    }

    private fun enableInputs() {
        imageView.isEnabled = true
        descriptionEditText.isEnabled = true
        view?.findViewById<Button>(R.id.btn_upload_image)?.isEnabled = true
        view?.findViewById<Button>(R.id.btn_cancel)?.isEnabled = true
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 1
    }
}