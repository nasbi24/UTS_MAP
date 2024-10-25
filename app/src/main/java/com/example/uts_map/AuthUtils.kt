package com.example.uts_map

import com.google.firebase.auth.FirebaseAuth

object AuthUtils {
    fun isUserLoggedIn(): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser != null
    }
}