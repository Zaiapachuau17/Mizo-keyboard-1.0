package com.mizo.keyboard

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Vibrator
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.LinearLayout
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.mizo.keyboard.dictionary.MizoDictionaryEngine

/**
 * Main Mizo Input Method Service (IME).
 * Manages soft keyboard rendering, Mizo orthography (Ṭ/ṭ, Ṅ/ṅ), candidate bar suggestions,
 * and text commits.
 */
class MizoInputMethodService : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardView: KeyboardView
    private lateinit var qwertyKeyboard: Keyboard
    private lateinit var symbolsKeyboard: Keyboard
    private lateinit var dictionaryEngine: MizoDictionaryEngine
    private lateinit var candidateStrip: LinearLayout
    private lateinit var vibrator: Vibrator

    private val composingText = StringBuilder()
    private var isCaps = false
    private var isSymbols = false
    private var hapticEnabled = true

    override fun onCreate() {
        super.onCreate()
        dictionaryEngine = MizoDictionaryEngine(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun onCreateInputView(): View {
        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as KeyboardView
        qwertyKeyboard = Keyboard(this, R.xml.keyboard_mizo_qwerty)
        symbolsKeyboard = Keyboard(this, R.xml.keyboard_symbols)

        keyboardView.keyboard = qwertyKeyboard
        keyboardView.setOnKeyboardActionListener(this)
        keyboardView.isPreviewEnabled = true

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        hapticEnabled = prefs.getBoolean("pref_haptic", true)

        return keyboardView
    }

    override fun onCreateCandidatesView(): View {
        candidateStrip = layoutInflater.inflate(R.layout.candidate_strip, null) as LinearLayout
        return candidateStrip
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        composingText.clear()
        updateCandidateSuggestions()
        setCandidatesViewShown(true)
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val ic: InputConnection = currentInputConnection ?: return
        triggerHaptic()

        when (primaryCode) {
            Keyboard.KEYCODE_DELETE -> handleDelete(ic)
            Keyboard.KEYCODE_SHIFT -> handleShift()
            Keyboard.KEYCODE_DONE -> ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            -2 -> handleKeyboardSwitch() // Switch between QWERTY and Symbols
            32 -> handleSpace(ic) // Space bar with auto-commit
            7789 -> handleChar('ṭ', ic) // Dedicated Mizo ṭ (U+1E6D)
            7749 -> handleChar('ṅ', ic) // Dedicated Mizo ṅ (U+1E45)
            else -> {
                var code = primaryCode.toChar()
                if (Character.isLetter(code) && isCaps) {
                    code = Character.toUpperCase(code)
                }
                handleChar(code, ic)
            }
        }
    }

    private fun handleChar(code: Char, ic: InputConnection) {
        composingText.append(code)
        ic.setComposingText(composingText.toString(), 1)
        updateCandidateSuggestions()
    }

    private fun handleDelete(ic: InputConnection) {
        if (composingText.isNotEmpty()) {
            composingText.deleteCharAt(composingText.length - 1)
            if (composingText.isNotEmpty()) {
                ic.setComposingText(composingText.toString(), 1)
            } else {
                ic.commitText("", 1)
            }
            updateCandidateSuggestions()
        } else {
            ic.deleteSurroundingText(1, 0)
        }
    }

    private fun handleSpace(ic: InputConnection) {
        if (composingText.isNotEmpty()) {
            commitTypedWord(composingText.toString(), ic)
        } else {
            ic.commitText(" ", 1)
        }
    }

    private fun handleShift() {
        isCaps = !isCaps
        qwertyKeyboard.isShifted = isCaps
        keyboardView.invalidateAllKeys()
    }

    private fun handleKeyboardSwitch() {
        isSymbols = !isSymbols
        keyboardView.keyboard = if (isSymbols) symbolsKeyboard else qwertyKeyboard
        keyboardView.invalidateAllKeys()
    }

    private fun commitTypedWord(word: String, ic: InputConnection) {
        ic.commitText(word + " ", 1)
        composingText.clear()
        // Provide next-word suggestions
        val nextWords = dictionaryEngine.getNextWordPredictions(word)
        renderCandidates(nextWords)
    }

    private fun updateCandidateSuggestions() {
        val query = composingText.toString()
        if (query.isEmpty()) {
            renderCandidates(emptyList())
            return
        }
        val suggestions = dictionaryEngine.getSuggestions(query, maxCount = 4)
        renderCandidates(suggestions)
    }

    private fun renderCandidates(words: List<String>) {
        if (!::candidateStrip.isInitialized) return
        candidateStrip.removeAllViews()

        for (word in words) {
            val tv = TextView(this).apply {
                text = word
                textSize = 16f
                setPadding(32, 16, 32, 16)
                setTextColor(getColor(R.color.key_text_dark))
                setOnClickListener {
                    val ic = currentInputConnection ?: return@setOnClickListener
                    commitTypedWord(word, ic)
                }
            }
            candidateStrip.addView(tv)
        }

        // If word is unknown, add "+ Add Word" button
        val query = composingText.toString()
        if (query.length > 2 && !dictionaryEngine.isKnownWord(query)) {
            val addBtn = TextView(this).apply {
                text = "+ Add '$query'"
                textSize = 14f
                setPadding(24, 16, 24, 16)
                setTextColor(getColor(R.color.candidate_active_dark))
                setOnClickListener {
                    dictionaryEngine.addPersonalWord(query)
                    val ic = currentInputConnection ?: return@setOnClickListener
                    commitTypedWord(query, ic)
                }
            }
            candidateStrip.addView(addBtn)
        }
    }

    private fun triggerHaptic() {
        if (hapticEnabled && ::vibrator.isInitialized) {
            try {
                vibrator.vibrate(20)
            } catch (ignored: Exception) {}
        }
    }

    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
    override fun onText(text: CharSequence?) {
        val ic = currentInputConnection ?: return
        ic.commitText(text, 1)
    }
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
}
