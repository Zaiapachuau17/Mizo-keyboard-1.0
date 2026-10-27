package com.mizo.keyboard.settings

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.mizo.keyboard.databinding.ActivityPersonalDictionaryBinding
import com.mizo.keyboard.dictionary.PersonalDictionaryDb

class PersonalDictionaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalDictionaryBinding
    private lateinit var db: PersonalDictionaryDb
    private lateinit var adapter: ArrayAdapter<String>
    private val wordsList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalDictionaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = PersonalDictionaryDb(this)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, wordsList)
        binding.listViewWords.adapter = adapter

        loadWords()

        binding.btnAddWord.setOnClickListener {
            showAddWordDialog()
        }

        binding.listViewWords.setOnItemLongClickListener { _, _, position, _ ->
            val word = wordsList[position]
            AlertDialog.Builder(this)
                .setTitle("Delete '$word'?")
                .setMessage("Remove this word from your personal dictionary?")
                .setPositiveButton("Delete") { _, _ ->
                    db.deleteWord(word)
                    loadWords()
                }
                .setNegativeButton("Cancel", null)
                .show()
            true
        }
    }

    private fun loadWords() {
        wordsList.clear()
        wordsList.addAll(db.getAllWords())
        adapter.notifyDataSetChanged()
    }

    private fun showAddWordDialog() {
        val input = EditText(this).apply {
            hint = "E.g. ṭhianṭha, aizawl"
        }
        AlertDialog.Builder(this)
            .setTitle("Add New Mizo Word")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val newWord = input.text.toString().trim()
                if (newWord.isNotEmpty()) {
                    db.insertWord(newWord)
                    loadWords()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
