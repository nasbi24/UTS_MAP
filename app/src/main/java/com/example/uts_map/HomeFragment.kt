package com.example.uts_map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val fragmentContainerPinnedNotes = view.findViewById<LinearLayout>(R.id.fragment_container_pinned_notes)
        val fragmentContainerInterestingIdea = view.findViewById<LinearLayout>(R.id.fragment_container_interesting_idea)
        val fragmentContainerGoals = view.findViewById<LinearLayout>(R.id.fragment_container_goals)
        val fragmentContainerRoutineTask = view.findViewById<LinearLayout>(R.id.fragment_container_routine_task)

        // Add fragments incrementally
        for (i in 1..5) {
            addFragment(fragmentContainerPinnedNotes, PinnedNotes(), "pinned_notes_$i")
            addFragment(fragmentContainerInterestingIdea, InterestingIdea(), "interesting_idea_$i")
            addFragment(fragmentContainerGoals, GoalsFragment(), "goals_$i")
            addFragment(fragmentContainerRoutineTask, RoutineTask(), "routine_task_$i")
        }

        return view
    }

    private fun addFragment(container: LinearLayout, fragment: Fragment, tag: String) {
        val fragmentManager: FragmentManager = childFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(container.id, fragment, tag)
        fragmentTransaction.commit()
    }
}