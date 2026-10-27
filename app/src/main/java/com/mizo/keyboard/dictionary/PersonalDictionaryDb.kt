package com.mizo.keyboard.dictionary

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PersonalDictionaryDb(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "mizo_personal_dict.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_WORDS = "words"
        private const val COL_ID = "id"
        private const val COL_WORD = "word"
        private const val COL_FREQUENCY = "freq"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_WORDS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_WORD TEXT UNIQUE NOT NULL,
                $COL_FREQUENCY INTEGER DEFAULT 1
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_WORDS")
        onCreate(db)
    }

    fun insertWord(word: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_WORD, word.trim())
            put(COL_FREQUENCY, 1)
        }
        db.insertWithOnConflict(TABLE_WORDS, null, values, SQLiteDatabase.CONFLICT_IGNORE)
    }

    fun deleteWord(word: String) {
        val db = writableDatabase
        db.delete(TABLE_WORDS, "$COL_WORD = ?", arrayOf(word.trim()))
    }

    fun getAllWords(): List<String> {
        val list = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COL_WORD FROM $TABLE_WORDS ORDER BY $COL_WORD ASC", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun searchWords(prefix: String): List<String> {
        val list = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_WORD FROM $TABLE_WORDS WHERE $COL_WORD LIKE ? LIMIT 5",
            arrayOf("$prefix%")
        )
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun hasWord(word: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_ID FROM $TABLE_WORDS WHERE LOWER($COL_WORD) = LOWER(?) LIMIT 1",
            arrayOf(word.trim())
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun clearAll() {
        val db = writableDatabase
        db.delete(TABLE_WORDS, null, null)
    }
}
