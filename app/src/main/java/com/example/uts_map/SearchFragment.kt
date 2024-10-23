package com.example.uts_map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.content.Context
import android.widget.ArrayAdapter

class SearchFragment : Fragment() {

    private lateinit var backButton: ImageButton
    private lateinit var searchInput: EditText
    private lateinit var searchHistory: ListView
    private lateinit var recentSearchHeader: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        // Initialize UI components
        backButton = view.findViewById(R.id.backButton)
        searchInput = view.findViewById(R.id.searchInput)
        searchHistory = view.findViewById(R.id.searchHistory)
        recentSearchHeader = view.findViewById(R.id.recentSearchHeader)

        // Show the keyboard when the fragment is opened
        searchInput.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT)

        // Handle back button click
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Load recent search history (mock data for now)
        val recentSearches = listOf("Product Idea", "Monthly Buying List")
        setupSearchHistory(recentSearches)

        return view
    }

    // Function to set up search history
    private fun setupSearchHistory(searches: List<String>) {
        // Check if the search history is empty or not
        if (searches.isEmpty()) {
            recentSearchHeader.visibility = View.GONE
        } else {
            recentSearchHeader.visibility = View.VISIBLE
        }

        // Create a simple adapter for the ListView
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, searches)
        searchHistory.adapter = adapter

        // Handle clicks on the search history items
        searchHistory.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = searches[position]
            searchInput.setText(selectedItem) // Put the selected item in the search input
        }
    }

    // Close the keyboard when fragment is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchInput.windowToken, 0)
    }
}
