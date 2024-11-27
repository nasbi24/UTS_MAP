package com.example.uts_map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class CalendarFragment : Fragment() {

    private lateinit var calendarView: MaterialCalendarView
    private lateinit var recyclerViewAgenda: RecyclerView
    private val agendas: MutableList<String> = mutableListOf()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)
        calendarView = view.findViewById(R.id.calendarView)
        recyclerViewAgenda = view.findViewById(R.id.recyclerViewAgenda)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupCalendar()

        return view
    }

    private fun setupCalendar() {
        calendarView.setOnDateChangedListener(OnDateSelectedListener { widget, date, selected ->
            if (selected) {
                val selectedDate = LocalDate.of(date.year, date.month + 1, date.day)
                val formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                fetchNotesForDate(formattedDate)
            }
        })
    }

    private fun fetchNotesForDate(date: String) {
        val user = auth.currentUser?.email ?: return
        firestore.collection("notes")
            .whereEqualTo("user", user)
            .whereEqualTo("date", date)
            .get()
            .addOnSuccessListener { documents ->
                val notes = documents.map { document ->
                    Note(
                        id = document.id,
                        title = document.getString("title") ?: "",
                        content = document.getString("content") ?: "",
                        category = document.getString("category") ?: "",
                        date = document.getString("date") ?: ""
                    )
                }
                refreshAgenda(notes)
            }
    }

    private fun refreshAgenda(notes: List<Note>) {
        recyclerViewAgenda.layoutManager = LinearLayoutManager(context)
        recyclerViewAgenda.adapter = AgendaAdapter(notes) { note ->
            val bundle = Bundle().apply {
                putString("NOTE_ID", note.id)
                putString("NOTE_TITLE", note.title)
                putString("NOTE_CONTENT", note.content)
                putString("NOTE_CATEGORY", note.category)
                putString("NOTE_DATE", note.date)
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NoteDetailFragment::class.java, bundle)
                .addToBackStack(null)
                .commit()
        }
    }
}