package com.example.ppo1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.media.AudioManager
import android.media.SoundPool
import android.view.View
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val maxStreams = 1
    var sp: SoundPool? = null
    var soundId = 0
    var streamIdSound = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*SoundPool soundPool = new SoundPool.Builder().build();
 assertThat(soundPool).isNotNull();
 SoundPool.OnLoadCompleteListener listener = mock(SoundPool.OnLoadCompleteListener.class);
 soundPool.setOnLoadCompleteListener(listener);*/
        /*
        sp = SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 0)
        soundId = sp!!.load(this, R.raw.first_sound, 1)
        // sp!!.play(soundId, 1F, 1F, 0, 5, 1F)*/
        sp = SoundPool.Builder().build()
        sp!!.load(this, R.raw.first_sound, 1)

        playBtn.setOnClickListener {
            val intent = Intent(this, TimerActivity::class.java)
            startActivity(intent)
        }
        settingsBtn.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}