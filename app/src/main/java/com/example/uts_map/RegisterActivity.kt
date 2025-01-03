package com.example.uts_map

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var btnRegister: Button
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etRetypePassword: EditText
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Check if user is already logged in
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        btnRegister = findViewById(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etRetypePassword = findViewById(R.id.etRetypePassword)

        btnRegister.isEnabled = false

        etFullName.addTextChangedListener(registerTextWatcher)
        etEmail.addTextChangedListener(registerTextWatcher)
        etPassword.addTextChangedListener(registerTextWatcher)
        etRetypePassword.addTextChangedListener(registerTextWatcher)

        btnRegister.setOnClickListener {
            val fullName = etFullName.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val retypePassword = etRetypePassword.text.toString()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != retypePassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = hashMapOf(
                            "email" to email,
                            "name" to fullName,
                            "profilepic_path" to null
                        )
                        db.collection("users").document(auth.currentUser!!.uid)
                            .set(user)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to create user document", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Google Sign-In button
        findViewById<Button>(R.id.btnGoogleRegister).setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = hashMapOf(
                        "email" to account.email,
                        "name" to account.displayName,
                        "profilepic_path" to account.photoUrl.toString()
                    )
                    db.collection("users").document(auth.currentUser!!.uid)
                        .set(user)
                        .addOnSuccessListener {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to create user document", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private val registerTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val fullNameInput = etFullName.text.toString().trim()
            val emailInput = etEmail.text.toString().trim()
            val passwordInput = etPassword.text.toString().trim()
            val retypePasswordInput = etRetypePassword.text.toString().trim()

            val isInputValid = fullNameInput.isNotEmpty() && emailInput.isNotEmpty() &&
                    passwordInput.isNotEmpty() && retypePasswordInput.isNotEmpty() &&
                    Patterns.EMAIL_ADDRESS.matcher(emailInput).matches() &&
                    passwordInput == retypePasswordInput

            // Mengatur enabled dan backgroundTint tombol Register
            btnRegister.isEnabled = isInputValid
            if (isInputValid) {
                // Menghapus background tint ketika semua input valid
                btnRegister.backgroundTintList = null
            } else {
                // Memberikan warna tint ketika input belum lengkap/valid
                btnRegister.backgroundTintList =
                    ContextCompat.getColorStateList(this@RegisterActivity, R.color.gray)
            }
        }

        override fun afterTextChanged(s: Editable?) {}
    }
}