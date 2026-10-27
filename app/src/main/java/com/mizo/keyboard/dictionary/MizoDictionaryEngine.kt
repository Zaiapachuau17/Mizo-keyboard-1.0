package com.mizo.keyboard.dictionary

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.InputStreamReader

data class DictWord(
    @SerializedName("w") val word: String,
    @SerializedName("f") val freq: Int,
    @SerializedName("d") val def: String? = null
)

data class DictPhrase(
    @SerializedName("p") val phrase: String,
    @SerializedName("d") val def: String? = null
)

data class MizoDictBundle(
    @SerializedName("words") val words: List<DictWord>,
    @SerializedName("phrases") val phrases: List<DictPhrase>?,
    @SerializedName("nextWords") val nextWords: Map<String, List<String>>?
)

/**
 * High-performance embedded Mizo Dictionary Engine.
 * Operates 100% offline using the bundled dictionary compiled into assets/mizo_dictionary.json.
 */
class MizoDictionaryEngine(private val context: Context) {

    private val embeddedWords = mutableListOf<DictWord>()
    private val embeddedPhrases = mutableListOf<String>()
    private val nextWordMap = mutableMapOf<String, List<String>>()
    private val wordSet = mutableSetOf<String>()
    private val personalDb = PersonalDictionaryDb(context)

    // Common Mizo phonetic typos mapped to correct Unicode characters
    private val typoMap = mapOf(
        "tha" to "ṭha",
        "thian" to "ṭhian",
        "thiante" to "ṭhiante",
        "thianpa" to "ṭhianpa",
        "thiannu" to "ṭhiannu",
        "tawng" to "ṭawng",
        "tawngtai" to "ṭawngṭai",
        "tawngtaina" to "ṭawngṭaina",
        "tan" to "ṭan",
        "tanpui" to "ṭanpui",
        "tanpuina" to "ṭanpuina",
        "thenawm" to "ṭhenawm",
        "thang" to "ṭhang",
        "thangthar" to "ṭhangthar",
        "tum" to "ṭum",
        "thenkhat" to "ṭhenkhat",
        "tap" to "ṭap",
        "thutthleng" to "ṭhutthleng",
        "tullo" to "ṭullo",
        "thatna" to "ṭhatna",
        "thazawk" to "ṭhazawk",
        "nhal" to "ṅhal",
        "nhat" to "ṅhat",
        "nhawng" to "ṅhawng",
        "kalam" to "ka lawm e",
        "chibay" to "chibai"
    )

    init {
        loadEmbeddedDictionary()
    }

    private fun loadEmbeddedDictionary() {
        try {
            val inputStream = context.assets.open("mizo_dictionary.json")
            val reader = InputStreamReader(inputStream)
            val bundle: MizoDictBundle = Gson().fromJson(reader, MizoDictBundle::class.java)
            embeddedWords.addAll(bundle.words)
            bundle.words.forEach { wordSet.add(it.word.lowercase()) }
            bundle.phrases?.forEach {
                embeddedPhrases.add(it.phrase)
                wordSet.add(it.phrase.lowercase())
            }
            bundle.nextWords?.let { nextWordMap.putAll(it) }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Get suggestions for the current composing prefix.
     */
    fun getSuggestions(prefix: String, maxCount: Int = 3): List<String> {
        val clean = prefix.trim().lowercase()
        if (clean.isEmpty()) return emptyList()

        val results = LinkedHashSet<String>()

        // 1. Check exact typo corrections (e.g. tha -> ṭha)
        typoMap[clean]?.let { results.add(it) }

        // 2. Personal dictionary exact or prefix matches
        val personalMatches = personalDb.searchWords(clean)
        results.addAll(personalMatches)

        // 3. Embedded dictionary prefix search sorted by frequency
        val prefixMatches = embeddedWords
            .filter { it.word.lowercase().startsWith(clean) }
            .sortedByDescending { it.freq }
            .map { it.word }

        results.addAll(prefixMatches)

        // 4. Check multi-word phrase matches
        val phraseMatches = embeddedPhrases
            .filter { it.lowercase().startsWith(clean) }

        results.addAll(phraseMatches)

        // 5. Return top candidates up to maxCount
        return results.take(maxCount)
    }

    /**
     * Get next-word predictions based on previous typed text.
     */
    fun getNextWordPredictions(previousWord: String): List<String> {
        val clean = previousWord.trim().lowercase()
        return nextWordMap[clean] ?: emptyList()
    }

    /**
     * Check if a word is already in the dictionary.
     */
    fun isKnownWord(word: String): Boolean {
        val clean = word.trim().lowercase()
        return wordSet.contains(clean) || personalDb.hasWord(clean)
    }

    /**
     * Learn a new word into the personal dictionary.
     */
    fun addPersonalWord(word: String) {
        val clean = word.trim()
        if (clean.isNotEmpty()) {
            personalDb.insertWord(clean)
            wordSet.add(clean.lowercase())
        }
    }
}
