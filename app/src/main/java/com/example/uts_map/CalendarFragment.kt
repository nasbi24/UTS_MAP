package com.example.uts_map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener

class CalendarFragment : Fragment() {

    private lateinit var calendarView: MaterialCalendarView
    private lateinit var recyclerViewAgenda: RecyclerView
    private val agendas: MutableList<String> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)
        calendarView = view.findViewById(R.id.calendarView)
        recyclerViewAgenda = view.findViewById(R.id.recyclerViewAgenda)

        setupCalendar()
        setupAgendas()

        return view
    }

    private fun setupCalendar() {
        calendarView.setOnDateChangedListener(OnDateSelectedListener { widget, date, selected ->
            if (selected) {
                // Filter notes for the selected date
                val selectedDate = "${date.year}-${date.month + 1}-${date.day}"
                val filteredAgendas = agendas.filter { it.contains(selectedDate) }
                refreshAgenda(filteredAgendas)
            }
        })
    }

    private fun refreshAgenda(filteredAgendas: List<String>) {
        recyclerViewAgenda.layoutManager = LinearLayoutManager(context)
        recyclerViewAgenda.adapter = AgendaAdapter(filteredAgendas)
    }

    private fun setupAgendas() {
        recyclerViewAgenda.layoutManager = LinearLayoutManager(context)
        recyclerViewAgenda.adapter = AgendaAdapter(agendas)
    }
}