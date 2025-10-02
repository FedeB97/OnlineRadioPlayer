package com.onlineradioplayer.app

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val group = findViewById<RadioGroup>(R.id.langGroup)
        val english = findViewById<RadioButton>(R.id.langEnglish)
        val spanish = findViewById<RadioButton>(R.id.langSpanish)

        // load current
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val lang = prefs.getString("lang", "en")
        if (lang == "es") spanish.isChecked = true else english.isChecked = true

        group.setOnCheckedChangeListener { _, checkedId ->
            val chosen = if (checkedId == R.id.langSpanish) "es" else "en"
            prefs.edit().putString("lang", chosen).apply()
            updateLocale(chosen)
            // restart main activity to apply immediately
            startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            finish()
        }
    }

    private fun updateLocale(code: String) {
        val locale = Locale(code)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
