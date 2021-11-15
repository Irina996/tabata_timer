package com.example.ppo1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import kotlinx.android.synthetic.main.activity_timer.*

class EntryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)
    }

    override fun onResume() {
        super.onResume()

        val timer = object : CountDownTimer((7 * 1000).toLong(), 1000) {
            override fun onTick(p0: Long) {
                val seconds = p0 / 1000
            }

            override fun onFinish() {
                openApp()
            }
        }
        (timer as CountDownTimer).start()
    }

    private fun openApp() {
        val intent = Intent(this, MainActivity::class.java)
        this.startActivity(intent)
    }
}