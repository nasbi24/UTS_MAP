package com.example.uts_map

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class ReminderUploadFragment : Fragment() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var dateEditText: EditText
    private lateinit var timeEditText: EditText
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var contentLayout: LinearLayout
    private lateinit var noteId: String
    private var selectedTime: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reminder_upload, container, false)
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        dateEditText = view.findViewById(R.id.et_reminder_date)
        timeEditText = view.findViewById(R.id.et_reminder_time)
        titleEditText = view.findViewById(R.id.et_reminder_title)
        descriptionEditText = view.findViewById(R.id.et_reminder_description)
        progressBar = view.findViewById(R.id.progress_bar)
        contentLayout = view.findViewById(R.id.content_layout)
        val uploadButton = view.findViewById<Button>(R.id.btn_upload_reminder)
        val cancelButton = view.findViewById<Button>(R.id.btn_cancel)

        dateEditText.setOnClickListener { openDatePicker() }
        timeEditText.setOnClickListener { openTimePicker() }

        uploadButton.setOnClickListener { uploadReminder() }
        cancelButton.setOnClickListener { parentFragmentManager.popBackStack() }

        noteId = arguments?.getString("NOTE_ID") ?: ""

        return view
    }

    private fun openDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            dateEditText.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun openTimePicker() {
        val currentTime = Calendar.getInstance()
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)

        TimePickerDialog(context, { _, selectedHour, selectedMinute ->
            selectedTime.set(Calendar.HOUR_OF_DAY, selectedHour)
            selectedTime.set(Calendar.MINUTE, selectedMinute)
            timeEditText.setText(String.format("%02d:%02d", selectedHour, selectedMinute))
        }, hour, minute, true).show()
    }

    private fun uploadReminder() {
        val date = dateEditText.text.toString()
        val time = timeEditText.text.toString()
        val title = titleEditText.text.toString()
        val description = descriptionEditText.text.toString()

        if (date.isEmpty() || time.isEmpty() || title.isEmpty() || description.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val reminder = hashMapOf(
            "date" to date,
            "time" to time,
            "title" to title,
            "description" to description,
            "note_id" to noteId
        )

        showProgressBar()
        disableInputs()

        firestore.collection("reminders")
            .add(reminder)
            .addOnSuccessListener {
                hideProgressBar()
                enableInputs()
                Toast.makeText(context, "Reminder uploaded successfully", Toast.LENGTH_SHORT).show()
                (activity as NotesActivity).addReminderToLayout(date, title, description)
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener {
                hideProgressBar()
                enableInputs()
                Toast.makeText(context, "Failed to upload reminder", Toast.LENGTH_SHORT).show()
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
        dateEditText.isEnabled = false
        timeEditText.isEnabled = false
        titleEditText.isEnabled = false
        descriptionEditText.isEnabled = false
        view?.findViewById<Button>(R.id.btn_upload_reminder)?.isEnabled = false
        view?.findViewById<Button>(R.id.btn_cancel)?.isEnabled = false
    }

    private fun enableInputs() {
        dateEditText.isEnabled = true
        timeEditText.isEnabled = true
        titleEditText.isEnabled = true
        descriptionEditText.isEnabled = true
        view?.findViewById<Button>(R.id.btn_upload_reminder)?.isEnabled = true
        view?.findViewById<Button>(R.id.btn_cancel)?.isEnabled = true
    }
}