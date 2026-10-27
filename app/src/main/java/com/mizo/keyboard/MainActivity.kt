package com.mizo.keyboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.mizo.keyboard.databinding.ActivityMainBinding
import com.mizo.keyboard.settings.SettingsActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupStepButtons()
    }

    override fun onResume() {
        super.onResume()
        updateSetupStatus()
    }

    private fun setupStepButtons() {
        // Step 1: Open system Input Method Settings to enable Mizo Keyboard
        binding.btnEnableKeyboard.setOnClickListener {
            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
            startActivity(intent)
        }

        // Step 2: Show input method picker to switch active keyboard
        binding.btnSelectKeyboard.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }

        // Step 4: Open Customization Settings
        binding.btnOpenSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun updateSetupStatus() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledMethods = imm.enabledInputMethodList
        val myPackage = packageName

        val isEnabled = enabledMethods.any { it.packageName == myPackage }
        binding.statusStep1.text = if (isEnabled) "✓ Enabled" else "Not yet enabled"

        val currentIme = Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        val isSelected = currentIme != null && currentIme.contains(myPackage)
        binding.statusStep2.text = if (isSelected) "✓ Active" else "Not currently selected"
    }
}
