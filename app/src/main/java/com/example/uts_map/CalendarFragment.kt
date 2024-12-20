package com.example.uts_map

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class CalendarFragment : Fragment() {

    private lateinit var calendarView: MaterialCalendarView
    private lateinit var recyclerViewAgenda: RecyclerView
    private val agendas: MutableList<Note> = mutableListOf()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var agendaAdapter: AgendaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)
        calendarView = view.findViewById(R.id.calendarView)
        recyclerViewAgenda = view.findViewById(R.id.recyclerViewAgenda)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        setupCalendar()

        return view
    }

    private fun setupRecyclerView() {
        agendaAdapter = AgendaAdapter(agendas) { note ->
            val bundle = Bundle().apply {
                putString("NOTE_ID", note.id)
                putString("NOTE_TITLE", note.title)
                putString("NOTE_CONTENT", note.content)
                putString("NOTE_CATEGORY", note.category)
                putString("NOTE_DATE", note.date)
            }
            findNavController().navigate(R.id.noteDetailFragment, bundle)
        }
        recyclerViewAgenda.layoutManager = LinearLayoutManager(context)
        recyclerViewAgenda.adapter = agendaAdapter
    }

    private fun setupCalendar() {
        calendarView.setOnDateChangedListener(OnDateSelectedListener { widget, date, selected ->
            if (selected) {
                val localDate = java.time.LocalDate.of(date.year, date.month, date.day)
                val formattedDate = localDate.format(DateTimeFormatter.ISO_DATE)
                fetchNotesForDate(formattedDate)
            }
        })
        fetchAllNoteDates()
    }

    private fun fetchAllNoteDates() {
        val user = auth.currentUser?.email ?: return
        firestore.collection("notes")
            .whereEqualTo("user", user)
            .get()
            .addOnSuccessListener { documents ->
                val dates = documents.mapNotNull { document ->
                    val firebaseTimestamp = document.getTimestamp("date") ?: return@mapNotNull null
                    val parsedDate = firebaseTimestamp.toDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    CalendarDay.from(parsedDate.year, parsedDate.monthValue, parsedDate.dayOfMonth)
                }
                calendarView.addDecorator(HeatMapDecorator(dates))
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    private fun fetchNotesForDate(date: String) {
        val user = auth.currentUser?.email ?: return
        firestore.collection("notes")
            .whereEqualTo("user", user)
            .get()
            .addOnSuccessListener { documents ->
                val notes = documents.mapNotNull { document ->
                    val firebaseTimestamp = document.getTimestamp("date") ?: return@mapNotNull null
                    val parsedDate = firebaseTimestamp.toDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    val formattedFirebaseDate = parsedDate.format(DateTimeFormatter.ISO_DATE)

                    if (formattedFirebaseDate == date) {
                        Log.d("CalendarFragment", "Note title: ${document.getString("title")}")
                        Note(
                            id = document.id,
                            title = document.getString("title") ?: "",
                            content = document.getString("content") ?: "",
                            category = document.getString("category") ?: "",
                            date = parsedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
                        )
                    } else {
                        null
                    }
                }
                refreshAgenda(notes)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    private fun refreshAgenda(notes: List<Note>) {
        agendas.clear()
        agendas.addAll(notes)
        agendaAdapter.notifyDataSetChanged()
    }
}
