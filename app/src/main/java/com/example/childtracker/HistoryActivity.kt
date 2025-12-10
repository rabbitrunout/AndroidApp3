package com.example.childtracker

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val text = findViewById<TextView>(R.id.historyText)

        val prefs = getSharedPreferences("history", MODE_PRIVATE)
        val history = prefs.getString("locations", "No history yet")

        text.text = history
    }
}
