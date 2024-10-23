package com.example.uts_map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class NoteDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_note_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = arguments?.getString("NOTE_TITLE")
        val content = arguments?.getString("NOTE_CONTENT")
        val category = arguments?.getString("NOTE_CATEGORY")
        val date = arguments?.getString("NOTE_DATE")

        view.findViewById<TextView>(R.id.tv_note_title).text = title
        view.findViewById<TextView>(R.id.tv_note_category).text = category
        view.findViewById<TextView>(R.id.tv_note_date).text = date
        view.findViewById<TextView>(R.id.tv_note_content).text = content

        view.findViewById<Button>(R.id.btn_back).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}