package com.example.uts_map

import android.os.Bundle
import android.util.Log
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
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NoteDetailFragment::class.java, bundle)
                .addToBackStack(null)
                .commit()
        }
        recyclerViewAgenda.layoutManager = LinearLayoutManager(context)
        recyclerViewAgenda.adapter = agendaAdapter
    }

    private fun setupCalendar() {
        calendarView.setOnDateChangedListener(OnDateSelectedListener { widget, date, selected ->
            if (selected) {
                val calendar = Calendar.getInstance()
                calendar.set(date.year, date.month - 1, date.day) // Subtract 1 from the month
                val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                fetchNotesForDate(formattedDate)
            }
        })
    }

    private fun fetchNotesForDate(date: String) {
        val user = auth.currentUser?.email ?: return
        firestore.collection("notes")
            .whereEqualTo("user", user)
            .get()
            .addOnSuccessListener { documents ->
                val notes = documents.mapNotNull { document ->
                    val firebaseTimestamp = document.getTimestamp("date") ?: return@mapNotNull null
                    val parsedDate = firebaseTimestamp.toDate()
                    val formattedFirebaseDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(parsedDate)
                    Log.d("CalendarFragment", "Selected date: $date")
                    Log.d("CalendarFragment", "Firebase date: $formattedFirebaseDate")
                    if (formattedFirebaseDate == date) {
                        val title = document.getString("title") ?: ""
                        Log.d("CalendarFragment", "Note title: $title")
                        Note(
                            id = document.id,
                            title = document.getString("title") ?: "",
                            content = document.getString("content") ?: "",
                            category = document.getString("category") ?: "",
                            date = SimpleDateFormat("MMMM d, yyyy 'at' h:mm:ss a z", Locale.getDefault()).format(parsedDate)
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