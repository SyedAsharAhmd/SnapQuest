package com.ashar.snapquest

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import android.widget.Toast

class SearchActivity : AppCompatActivity() {

    private lateinit var etSearch: EditText
    private lateinit var btnSearch: Button
    private lateinit var rvSearchResults: RecyclerView
    private val results = mutableListOf<String>()
    private lateinit var adapter: SearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        etSearch = findViewById(R.id.etSearch)
        btnSearch = findViewById(R.id.btnSearch)
        rvSearchResults = findViewById(R.id.rvSearchResults)

        adapter = SearchAdapter(results)
        rvSearchResults.layoutManager = LinearLayoutManager(this)
        rvSearchResults.adapter = adapter

        btnSearch.setOnClickListener {
            val query = etSearch.text.toString().trim().lowercase()
            if (query.isEmpty()) return@setOnClickListener
            searchUsers(query)
        }
    }

    private fun searchUsers(query: String) {
        val ref = FirebaseDatabase.getInstance().getReference("posts")
        ref.get().addOnSuccessListener { snapshot ->
            results.clear()
            val foundUsernames = mutableSetOf<String>()
            for (userSnapshot in snapshot.children) {
                for (dateSnapshot in userSnapshot.children) {
                    val username = dateSnapshot.child("username").getValue(String::class.java) ?: ""
                    if (username.lowercase().contains(query) && username.isNotEmpty()) {
                        foundUsernames.add(username)
                    }
                }
            }
            results.addAll(foundUsernames)
            adapter.notifyDataSetChanged()
            if (results.isEmpty()) {
                Toast.makeText(this, "No users found", Toast.LENGTH_SHORT).show()
            }
        }
    }
}