package com.example.uts_map

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SearchFragment : Fragment() {

    private lateinit var backButton: ImageButton
    private lateinit var searchInput: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var searchHistory: RecyclerView
    private lateinit var recentSearchHeader: TextView
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var firestore: FirebaseFirestore
    private val recentSearches = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        // Initialize UI components
        backButton = view.findViewById(R.id.backButton)
        searchInput = view.findViewById(R.id.searchInput)
        searchButton = view.findViewById(R.id.searchButton)
        searchHistory = view.findViewById(R.id.searchHistory)
        recentSearchHeader = view.findViewById(R.id.recentSearchHeader)
        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView)
        firestore = FirebaseFirestore.getInstance()

        // Show the keyboard when the fragment is opened
        searchInput.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT)

        // Handle back button click
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Handle search button click
        searchButton.setOnClickListener {
            val query = searchInput.text.toString()
            if (query.isNotEmpty()) {
                performSearch(query, )
                addRecentSearch(query)
            }
        }

        // Load recent search history
        loadRecentSearches()

        // Set up RecyclerView
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(context)
        searchHistory.layoutManager = LinearLayoutManager(context)

        return view
    }

    private fun performSearch(query: String) {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
        val lowercaseQuery = query.lowercase()
        firestore.collection("notes")
            .whereEqualTo("user", userEmail)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val results = documents.mapNotNull { document ->
                    val noteId = document.id
                    val title = document.getString("title") ?: return@mapNotNull null
                    val content = document.getString("content") ?: return@mapNotNull null
                    val category = document.getString("category") ?: return@mapNotNull null
                    val date = document.getTimestamp("date")?.toDate()?.toString() ?: return@mapNotNull null

                    if (title.lowercase().contains(lowercaseQuery)) {

                        Note(
                            id = noteId,
                            title = title,
                            content = content,
                            category = category,
                            date = date
                        )
                    }else{
                        null;
                    }
                }
                displaySearchResults(results)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to search notes", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displaySearchResults(results: List<Note>) {
        val adapter = SearchResultsAdapter(requireContext(), results) { note ->
            val bundle = Bundle().apply {
                putString("NOTE_ID", note.id)
                putString("NOTE_TITLE", note.title)
                putString("NOTE_CONTENT", note.content)
                putString("NOTE_CATEGORY", note.category)
                putString("NOTE_DATE", note.date)
            }
            findNavController().navigate(R.id.action_searchFragment_to_noteDetailFragment, bundle)
        }
        searchResultsRecyclerView.adapter = adapter
        searchHistory.visibility = View.GONE
        recentSearchHeader.visibility = View.GONE
    }

    private fun addRecentSearch(query: String) {
        if (recentSearches.contains(query)) {
            recentSearches.remove(query)
        }
        recentSearches.add(0, query)
        if (recentSearches.size > 10) {
            recentSearches.removeAt(10)
        }
        saveRecentSearches()
        setupSearchHistory(recentSearches)
    }

    private fun loadRecentSearches() {
        val sharedPreferences = requireContext().getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
        val searches = sharedPreferences.getStringSet("recent_searches", setOf())?.toMutableList() ?: mutableListOf()
        recentSearches.clear()
        recentSearches.addAll(searches)
        setupSearchHistory(recentSearches)
    }

    private fun saveRecentSearches() {
        val sharedPreferences = requireContext().getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putStringSet("recent_searches", recentSearches.toSet())
            apply()
        }
    }

    private fun setupSearchHistory(searches: List<String>) {
        if (searches.isEmpty()) {
            recentSearchHeader.visibility = View.GONE
        } else {
            recentSearchHeader.visibility = View.VISIBLE
        }

        val adapter = RecentSearchAdapter(requireContext(), searches, ::removeRecentSearch, ::performSearch)
        searchHistory.adapter = adapter
    }

    private fun removeRecentSearch(query: String) {
        recentSearches.remove(query)
        saveRecentSearches()
        setupSearchHistory(recentSearches)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchInput.windowToken, 0)
    }

    private fun createNoteCard(title: String?, content: String?, category: String?): CardView? {
        if (!isAdded) return null

        val inflater = LayoutInflater.from(requireContext())
        val cardView = inflater.inflate(R.layout.item_note_card, null) as CardView

        // Set margin untuk CardView
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 8, 16, 8)
        cardView.layoutParams = layoutParams

        // Mengatur isi CardView
        val titleTextView = cardView.findViewById<TextView>(R.id.tv_card_title)
        val contentTextView = cardView.findViewById<TextView>(R.id.tv_card_content)
        val categoryTextView = cardView.findViewById<TextView>(R.id.tv_card_category)

        titleTextView.text = title
        contentTextView.text = content
        categoryTextView.text = category

        return cardView
    }
}