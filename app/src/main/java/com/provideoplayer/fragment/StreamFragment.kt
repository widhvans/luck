package com.provideoplayer.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.provideoplayer.PlayerActivity
import com.provideoplayer.R
import com.provideoplayer.databinding.FragmentStreamBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONArray
import org.json.JSONObject

class StreamFragment : Fragment() {
    
    private var _binding: FragmentStreamBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var historyAdapter: StreamHistoryAdapter
    private val streamHistory = mutableListOf<StreamHistoryItem>()
    
    data class StreamHistoryItem(
        val url: String,
        val timestamp: Long
    )
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStreamBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupHistoryRecyclerView()
        loadHistory()
    }
    
    private fun setupUI() {
        // Play button
        binding.btnPlay.setOnClickListener {
            val url = binding.urlInput.text?.toString()?.trim() ?: ""
            if (url.isEmpty()) {
                binding.urlInputLayout.error = "Please enter a URL"
                return@setOnClickListener
            }
            
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                binding.urlInputLayout.error = "Invalid URL format"
                return@setOnClickListener
            }
            
            binding.urlInputLayout.error = null
            hideKeyboard()
            playStream(url)
        }
        
        // URL input ime action
        binding.urlInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.btnPlay.performClick()
                true
            } else {
                false
            }
        }
        
        // Clear history button
        binding.btnClearHistory.setOnClickListener {
            if (streamHistory.isEmpty()) {
                Toast.makeText(requireContext(), "History is already empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Clear History")
                .setMessage("Are you sure you want to clear all stream history?")
                .setPositiveButton("Clear") { _, _ ->
                    clearHistory()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
    
    private fun setupHistoryRecyclerView() {
        historyAdapter = StreamHistoryAdapter(
            onItemClick = { item ->
                playStream(item.url)
            },
            onDeleteClick = { item ->
                deleteHistoryItem(item)
            }
        )
        
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
    }
    
    private fun loadHistory() {
        val prefs = requireContext().getSharedPreferences("pro_video_player_prefs", Context.MODE_PRIVATE)
        val historyJson = prefs.getString("stream_history", "[]") ?: "[]"
        
        try {
            val jsonArray = JSONArray(historyJson)
            streamHistory.clear()
            
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                streamHistory.add(StreamHistoryItem(
                    url = obj.getString("url"),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                ))
            }
            
            // Sort by timestamp descending (most recent first)
            streamHistory.sortByDescending { it.timestamp }
            
            updateHistoryUI()
        } catch (e: Exception) {
            streamHistory.clear()
            updateHistoryUI()
        }
    }
    
    private fun saveHistory() {
        val prefs = requireContext().getSharedPreferences("pro_video_player_prefs", Context.MODE_PRIVATE)
        
        val jsonArray = JSONArray()
        streamHistory.forEach { item ->
            val obj = JSONObject()
            obj.put("url", item.url)
            obj.put("timestamp", item.timestamp)
            jsonArray.put(obj)
        }
        
        prefs.edit()
            .putString("stream_history", jsonArray.toString())
            .apply()
    }
    
    private fun addToHistory(url: String) {
        // Remove if already exists
        streamHistory.removeAll { it.url == url }
        
        // Add to beginning
        streamHistory.add(0, StreamHistoryItem(url, System.currentTimeMillis()))
        
        // Keep only last 100 items
        while (streamHistory.size > 100) {
            streamHistory.removeLast()
        }
        
        saveHistory()
        updateHistoryUI()
    }
    
    private fun deleteHistoryItem(item: StreamHistoryItem) {
        streamHistory.remove(item)
        saveHistory()
        updateHistoryUI()
    }
    
    private fun clearHistory() {
        streamHistory.clear()
        saveHistory()
        updateHistoryUI()
        Toast.makeText(requireContext(), "History cleared", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateHistoryUI() {
        if (streamHistory.isEmpty()) {
            binding.historyRecyclerView.visibility = View.GONE
            binding.emptyHistoryView.visibility = View.VISIBLE
        } else {
            binding.emptyHistoryView.visibility = View.GONE
            binding.historyRecyclerView.visibility = View.VISIBLE
            historyAdapter.submitList(streamHistory.toList())
        }
    }
    
    private fun playStream(url: String) {
        // Add to history
        addToHistory(url)
        
        // Get title from URL
        val title = try {
            val uri = android.net.Uri.parse(url)
            uri.lastPathSegment?.ifEmpty { "Network Stream" } ?: "Network Stream"
        } catch (e: Exception) {
            "Network Stream"
        }
        
        // Start player
        val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(PlayerActivity.EXTRA_VIDEO_URI, url)
            putExtra(PlayerActivity.EXTRA_VIDEO_TITLE, title)
            putExtra(PlayerActivity.EXTRA_IS_NETWORK_STREAM, true)
            putStringArrayListExtra(PlayerActivity.EXTRA_PLAYLIST, arrayListOf(url))
            putStringArrayListExtra(PlayerActivity.EXTRA_PLAYLIST_TITLES, arrayListOf(title))
        }
        startActivity(intent)
    }
    
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.urlInput.windowToken, 0)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    // Stream History Adapter
    inner class StreamHistoryAdapter(
        private val onItemClick: (StreamHistoryItem) -> Unit,
        private val onDeleteClick: (StreamHistoryItem) -> Unit
    ) : RecyclerView.Adapter<StreamHistoryAdapter.ViewHolder>() {
        
        private var items = listOf<StreamHistoryItem>()
        
        fun submitList(newItems: List<StreamHistoryItem>) {
            items = newItems
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_stream_history, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }
        
        override fun getItemCount() = items.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val urlText = itemView.findViewById<android.widget.TextView>(R.id.streamUrl)
            private val deleteBtn = itemView.findViewById<View>(R.id.btnDelete)
            
            fun bind(item: StreamHistoryItem) {
                urlText.text = item.url
                
                itemView.setOnClickListener { onItemClick(item) }
                deleteBtn.setOnClickListener { onDeleteClick(item) }
            }
        }
    }
}
