package com.onlineradioplayer.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    private lateinit var radios: MutableList<Radio>
    private lateinit var adapter: RadioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nameInput = findViewById<TextInputEditText>(R.id.nameInput)
        val urlInput = findViewById<TextInputEditText>(R.id.urlInput)
        val addBtn = findViewById<MaterialButton>(R.id.addRadioBtn)
        val recycler = findViewById<RecyclerView>(R.id.radioRecycler)

        radios = RadioStorage.loadRadios(this)

        // If empty, preload sample radios
        if (radios.isEmpty()) {
            radios.add(Radio("Radio Example 1", "http://streams.ilovemusic.de/iloveradio1.mp3"))
            radios.add(Radio("Radio Example 2", "http://streams.ilovemusic.de/iloveradio2.mp3"))
            radios.add(Radio("Radio Example 3", "http://stream.whus.org:8000/whusfm"))
            RadioStorage.saveRadios(this, radios)
        }

        adapter = RadioAdapter(radios,
            onPlayClick = { radio, index ->
                val intent = Intent(this, RadioService::class.java)
                intent.putExtra("url", radio.url)
                // also pass list and index
                intent.putExtra("index", index)
                startService(intent)
            },
            onDeleteClick = { radio ->
                radios.remove(radio)
                RadioStorage.saveRadios(this, radios)
                adapter.notifyDataSetChanged()
            })

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        addBtn.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val url = urlInput.text.toString().trim()
            if (name.isNotEmpty() && url.isNotEmpty()) {
                val r = Radio(name, url)
                radios.add(r)
                RadioStorage.saveRadios(this, radios)
                adapter.notifyItemInserted(radios.size - 1)
                nameInput.text?.clear()
                urlInput.text?.clear()
            }
        }

        // Mini player controls
        val mini = findViewById<LinearLayout>(R.id.miniPlayer)
        val miniTitle = findViewById<TextView>(R.id.miniPlayerTitle)
        val btnPrev = findViewById<ImageButton>(R.id.btnPrevious)
        val btnPlay = findViewById<ImageButton>(R.id.btnPlayPause)
        val btnNext = findViewById<ImageButton>(R.id.btnNext)
        val btnStop = findViewById<ImageButton>(R.id.btnStop)

        // Update UI when service sends intents is not implemented (simple approach)
        // We'll provide manual controls to send actions to the service
        btnPrev.setOnClickListener {
            startService(Intent(this, RadioService::class.java).setAction(RadioService.ACTION_PREVIOUS))
        }
        btnPlay.setOnClickListener {
            startService(Intent(this, RadioService::class.java).setAction(RadioService.ACTION_PLAY))
        }
        btnNext.setOnClickListener {
            startService(Intent(this, RadioService::class.java).setAction(RadioService.ACTION_NEXT))
        }
        btnStop.setOnClickListener {
            startService(Intent(this, RadioService::class.java).setAction(RadioService.ACTION_STOP))
            mini.visibility = View.GONE
        }

        // Open settings
        findViewById<ImageButton?>(R.id.openSettings)?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
