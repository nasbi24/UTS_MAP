package com.example.uts_map

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

fun fetchAndScheduleReminders(context: Context) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser?.email ?: return
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    db.collection("notes")
        .whereEqualTo("user", user)
        .get()
        .addOnSuccessListener { notesResult ->
            val noteIds = notesResult.documents.map { it.id }
            if (noteIds.isNotEmpty()) {
                db.collection("reminders")
                    .whereIn("note_id", noteIds)
                    .get()
                    .addOnSuccessListener { remindersResult ->
                        for (document in remindersResult) {
                            val reminderId = document.id
                            val title = document.getString("title") ?: ""
                            val description = document.getString("description") ?: ""
                            val dateField = document.get("date")
                            val timeString = document.getString("time") ?: ""

                            val dateString = dateField
                            val dateTimeString = "$dateString $timeString"
                            try {
                                val date = dateFormat.parse(dateTimeString)?.time ?: 0L
                                ScheduleReminder(context, reminderId, title, description, date)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("FetchReminders", "Error fetching reminders", e)
                    }
            }
        }
        .addOnFailureListener { e ->
            Log.e("FetchReminders", "Error fetching notes", e)
        }
}