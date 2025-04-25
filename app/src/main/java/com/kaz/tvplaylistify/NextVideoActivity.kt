package com.kaz.tvplaylistify

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity

class NextVideoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_next_video)

        val videoId = intent.getStringExtra("VIDEO_ID")

        Log.d("NextVideoActivity", "Activity creada, preparando video $videoId")

        if (videoId != null) {
            val youtubeIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://www.youtube.com/watch?v=$videoId")
                setPackage("com.google.android.youtube.tv")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }

            Log.d("NextVideoActivity", "Lanzando video $videoId en YouTube")
            startActivity(youtubeIntent)
        }

        finish()
    }
}
