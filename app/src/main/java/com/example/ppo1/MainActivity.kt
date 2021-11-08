package com.example.ppo1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.example.ppo1.util.convertMinutesToSeconds
import com.example.ppo1.util.getTimeFromStr
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startB.setOnClickListener {
            val setNumber: Int = setNumberTV.text.toString().toInt()
            val workSeconds: Int = convertMinutesToSeconds(
                getTimeFromStr(workIntervalTV.text.toString()).first,
                getTimeFromStr(workIntervalTV.text.toString()).second
            )
            val restSeconds: Int = convertMinutesToSeconds(
                getTimeFromStr(restIntervalTV.text.toString()).first,
                getTimeFromStr(restIntervalTV.text.toString()).second
            )
            val intent = Intent(this, TimerActivity::class.java)
            intent.putExtra(AppConstants.INTENT_SET_NUMBER, setNumber)
            intent.putExtra(AppConstants.INTENT_WORK_INTERVAL, workSeconds)
            intent.putExtra(AppConstants.INTENT_REST_INTERVAL, restSeconds)
            this.startActivity(intent)
        }
        iniButtonListener()
    }

    private fun iniButtonListener() {
        setNumberMinusB.setOnClickListener {
            plusOrMinus1(setNumberTV, false)
        }
        setNumberPlusB.setOnClickListener {
            plusOrMinus1(setNumberTV)
        }
        workIntervalPlusB.setOnClickListener {
            plus10(workIntervalTV)
        }
        workIntervalMinusB.setOnClickListener {
            minus10(workIntervalTV)
        }
        restIntervalPlusB.setOnClickListener {
            plus10(restIntervalTV)
        }
        restIntervalMinusB.setOnClickListener {
            minus10(restIntervalTV)
        }

        setButtonLongClick(restIntervalPlusB, restIntervalTV)
        setButtonLongClick(workIntervalPlusB, workIntervalTV)
        setButtonLongClick(restIntervalMinusB, restIntervalTV, add = false)
        setButtonLongClick(workIntervalMinusB, workIntervalTV, add = false)
        setButtonLongClick(setNumberMinusB, setNumberTV, add = false, time = false)
        setButtonLongClick(setNumberPlusB, setNumberTV, add = true, time = false)
    }

    private fun plus10(textViewTime: TextView) {
        var currentTime = textViewTime.text.toString()
        var seconds = getTimeFromStr(currentTime).second
        var minutes = getTimeFromStr(currentTime).first
        if (seconds < 50) {
            seconds += 10
        } else if (seconds == 50) {
            seconds = 0
            minutes += 1
        }
        currentTime = String.format(AppConstants.FORMAT, minutes) + ":" + String.format(AppConstants.FORMAT, seconds)
        textViewTime.text = currentTime
    }

    private fun minus10(textViewTime: TextView) {
        var currentTime = textViewTime.text.toString()
        var seconds = getTimeFromStr(currentTime).second
        var minutes = getTimeFromStr(currentTime).first
        if (seconds > 0) {
            seconds -= 10
        } else if (seconds == 0) {
            if (minutes != 0) {
                seconds = 50
                minutes -= 1
            } else {
                seconds = 0
                minutes = 0
            }
        }
        currentTime = String.format(AppConstants.FORMAT, minutes) + ":" + String.format(AppConstants.FORMAT, seconds)
        textViewTime.text = currentTime
    }

    private fun plusOrMinus1(textViewSet: TextView, add: Boolean = true) {
        var currentSetNumber = textViewSet.text.toString().toInt()
        if (add) {
            currentSetNumber += 1
        } else if (currentSetNumber != 0 && !add) {
            currentSetNumber -= 1
        } else if (currentSetNumber == 0 && !add) {
            currentSetNumber = 0
        }
        textViewSet.text = currentSetNumber.toString()
    }

    private fun setButtonLongClick(
        button: ImageButton,
        textView: TextView,
        add: Boolean = true,
        time: Boolean = true
    ) {
        button.setOnLongClickListener(object : View.OnLongClickListener {
            private val mHandler: Handler = Handler()
            private val incrementRunnable: Runnable = object : Runnable {
                override fun run() {
                    mHandler.removeCallbacks(this)
                    if (button.isPressed) {
                        if (time) {
                            if (add) {
                                plus10(textView)
                            } else {
                                minus10(textView)
                            }
                        } else {
                            plusOrMinus1(textView, add)
                        }
                        mHandler.postDelayed(this, 100)
                    }
                }
            }

            override fun onLongClick(view: View): Boolean {
                mHandler.postDelayed(incrementRunnable, 0)
                return true
            }
        })
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }
}