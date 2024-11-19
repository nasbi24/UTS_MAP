package com.example.uts_map

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditProfileFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var selectedImageUri: Uri
    private lateinit var currentPhotoPath: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val ivEditProfilePic = view.findViewById<ImageView>(R.id.ivEditProfilePic)
        val etEditName = view.findViewById<EditText>(R.id.etEditName)
        val etEditEmail = view.findViewById<EditText>(R.id.etEditEmail)
        val btnUploadPic = view.findViewById<TextView>(R.id.btnUploadPic)
        val btnCapturePic = view.findViewById<Button>(R.id.btnCapturePic)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val name = document.getString("name")
                        val email = document.getString("email")
                        val profilePicPath = document.getString("profilepic_path")

                        etEditName.setText(name)
                        etEditEmail.setText(email)

                        if (profilePicPath != null) {
                            Glide.with(this).load(profilePicPath).into(ivEditProfilePic)
                        } else {
                            ivEditProfilePic.setImageResource(R.drawable.ic_profileplaceholder)
                        }
                    }
                }
        }

        btnUploadPic.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        btnCapturePic.setOnClickListener {
            dispatchTakePictureIntent()
        }

        btnSave.setOnClickListener {
            val name = etEditName.text.toString()
            val email = etEditEmail.text.toString()

            val userUpdates = hashMapOf<String, Any>(
                "name" to name,
                "email" to email
            )

            if (::selectedImageUri.isInitialized) {
                val storageRef = storage.reference.child("profile_pics/${user?.uid}")
                storageRef.putFile(selectedImageUri).addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        userUpdates["profilepic_path"] = uri.toString()
                        updateUser(userUpdates)
                    }
                }
            } else {
                updateUser(userUpdates)
            }
        }

        return view
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                null
            }
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.uts_map.fileprovider",
                    it
                )
                selectedImageUri = photoURI
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, 2)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = requireActivity().getExternalFilesDir(null)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun updateUser(userUpdates: HashMap<String, Any>) {
        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).update(userUpdates)
                .addOnSuccessListener {
                    Toast.makeText(activity, "Profile updated", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data!!
            val ivEditProfilePic = view?.findViewById<ImageView>(R.id.ivEditProfilePic)
            ivEditProfilePic?.setImageURI(selectedImageUri)
        } else if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            val ivEditProfilePic = view?.findViewById<ImageView>(R.id.ivEditProfilePic)
            ivEditProfilePic?.setImageURI(selectedImageUri)
        }
    }
}