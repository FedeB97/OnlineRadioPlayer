package com.onlineradioplayer.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat as MediaNotification
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem

class RadioService : Service() {

    companion object {
        const val ACTION_PLAY = "com.onlineradioplayer.app.ACTION_PLAY"
        const val ACTION_PAUSE = "com.onlineradioplayer.app.ACTION_PAUSE"
        const val ACTION_STOP = "com.onlineradioplayer.app.ACTION_STOP"
        const val ACTION_NEXT = "com.onlineradioplayer.app.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.onlineradioplayer.app.ACTION_PREVIOUS"
    }

    private lateinit var player: ExoPlayer
    private var currentIndex = 0
    private var radioList: List<Radio> = emptyList()

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        createChannel()
    }

    private fun createChannel() {
        val channelId = "radio_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(channelId, "Radio Player", NotificationManager.IMPORTANCE_LOW)
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(ch)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        // If an URL + index is provided, set list and play that index
        val url = intent?.getStringExtra("url")
        val idx = intent?.getIntExtra("index", -1) ?: -1
        val listJson = intent?.getStringExtra("listJson")
        if (listJson != null && listJson.isNotEmpty()) {
            try {
                radioList = com.google.gson.Gson().fromJson(listJson, Array<Radio>::class.java).toList()
            } catch (e: Exception) { /* ignore */ }
        }
        if (url != null && idx >= 0) {
            playRadio(idx)
            return START_STICKY
        }

        when (action) {
            ACTION_PLAY -> player.play()
            ACTION_PAUSE -> player.pause()
            ACTION_STOP -> {
                stopForeground(true)
                stopSelf()
            }
            ACTION_NEXT -> playRadio(currentIndex + 1)
            ACTION_PREVIOUS -> playRadio(currentIndex - 1)
        }
        return START_STICKY
    }

    private fun playRadio(index: Int) {
        if (radioList.isEmpty()) return
        currentIndex = (index % radioList.size + radioList.size) % radioList.size
        val r = radioList[currentIndex]
        player.setMediaItem(MediaItem.fromUri(r.url))
        player.prepare()
        player.play()
        showNotification(r)
    }

    private fun showNotification(radio: Radio) {
        val channelId = "radio_channel"
        val prevIntent = Intent(this, RadioService::class.java).setAction(ACTION_PREVIOUS)
        val prevPending = PendingIntent.getService(this, 0, prevIntent, PendingIntent.FLAG_IMMUTABLE)

        val playIntent = Intent(this, RadioService::class.java).setAction(ACTION_PLAY)
        val playPending = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_IMMUTABLE)

        val pauseIntent = Intent(this, RadioService::class.java).setAction(ACTION_PAUSE)
        val pausePending = PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_IMMUTABLE)

        val nextIntent = Intent(this, RadioService::class.java).setAction(ACTION_NEXT)
        val nextPending = PendingIntent.getService(this, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE)

        val stopIntent = Intent(this, RadioService::class.java).setAction(ACTION_STOP)
        val stopPending = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(radio.name)
            .setContentText(radio.url)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .addAction(android.R.drawable.ic_media_previous, getString(R.string.previous), prevPending)

        if (player.isPlaying) {
            builder.addAction(android.R.drawable.ic_media_pause, getString(R.string.pause), pausePending)
        } else {
            builder.addAction(android.R.drawable.ic_media_play, getString(R.string.play), playPending)
        }

        builder.addAction(android.R.drawable.ic_media_next, getString(R.string.next), nextPending)
        builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.stop), stopPending)
        builder.setStyle(MediaNotification().setShowActionsInCompactView(0,1,2))
        builder.setOngoing(true)

        startForeground(1, builder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
