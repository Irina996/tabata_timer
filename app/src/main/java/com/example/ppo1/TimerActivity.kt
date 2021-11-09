package com.example.ppo1

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.ppo1.AppConstants.Companion.FORMAT
import com.example.ppo1.util.*
import kotlinx.android.synthetic.main.activity_timer.*
import java.util.*


class TimerActivity : AppCompatActivity() {

    companion object {
        fun setAlarm(context: Context, nowSeconds: Long, secondsRemaining: Long): Long {
            val wakeUpTime = (nowSeconds + secondsRemaining) * 1000
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntent)
            PrefUtil.setAlarmSetTime(nowSeconds, context)
            return wakeUpTime
        }

        fun removeAlarm(context: Context) {
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            PrefUtil.setAlarmSetTime(0, context)
        }

        val nowSeconds: Long
            get() = Calendar.getInstance().timeInMillis / 1000
    }

    enum class TimerState {
        Stopped, Paused, Running
    }

    //private lateinit var timer: CountDownTimer
    private var timerLengthSeconds = 0L
    private var timerState = TimerState.Stopped
    private var secondsRemaining = 0L

    private var startT = false
    private lateinit var context: Context

    private var setNumberIni: Int = 0
    private var workSecondsIni: Int = 0
    private var restSecondsIni: Int = 0
    private var currentSetNumber: Int = 0
    private var currentStep: Int = 0
    private var currentTime: Int = 0
    private var timer: CountDownTimer? = null
    private var isPaused: Boolean = false

    private lateinit var mpRest: MediaPlayer
    private lateinit var mpWork: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        mpRest = MediaPlayer.create(this, R.raw.beep)
        mpWork = MediaPlayer.create(this, R.raw.boop)

        context = this

        iniActionButtons()
        getValues()
        iniPhasesList()
        iniGetReady()

    }

    private fun iniActionButtons() {
        stopB.setOnClickListener {
            cancelTimer()
            this.finish()
        }
        replayB.setOnClickListener {
            cancelTimer()
            currentSetNumber = setNumberIni
            iniGetReady()
            isPaused = false
        }
        playPauseB.setOnClickListener {
            if (!isPaused) {
                cancelTimer()
                playPauseB.setImageResource(R.drawable.ic_play_arrow_24px)
                isPaused = true
            } else {
                if (currentStep != -1) {
                    currentTime = convertMinutesToSeconds(
                        getTimeFromStr(timeTV.text.toString()).first,
                        getTimeFromStr(timeTV.text.toString()).second
                    )
                    playPauseB.setImageResource(R.drawable.ic_pause_24px)
                    startTimer(currentTime)
                    isPaused = false
                }
            }
        }
    }

    private fun iniGetReady() {
        currentStep = 0
        playPauseB.setImageResource(R.drawable.ic_pause_24px)
        stepTV.isVisible = true
        stepCountTV.isVisible = true
        constraintLayout.setBackgroundResource(R.drawable.green_gradient)
        val temp = "${resources.getString(R.string.upper_set)} $currentSetNumber"
        stepCountTV.text = temp
        stepTV.text = resources.getString(R.string.upper_get_ready)
        timeTV.text = resources.getString(R.string.ini_time)
        startTimer(6)
    }

    private fun iniWorkout() {
        currentStep = 1
        constraintLayout.setBackgroundResource(R.drawable.blue_gradient)
        var temp = "${resources.getString(R.string.upper_set)} $currentSetNumber"
        stepCountTV.text = temp
        stepTV.text = resources.getString(R.string.upper_work_it)
        temp = "${String.format(
            FORMAT,
            convertSecondsToMinutes(workSecondsIni).first
        )}:${String.format(FORMAT, convertSecondsToMinutes(workSecondsIni).second + 1)}"
        timeTV.text = temp
        startTimer(workSecondsIni + 1)
    }

    private fun iniRest() {
        currentStep = 2
        constraintLayout.setBackgroundResource(R.drawable.pink_gradient)
        stepTV.text = resources.getString(R.string.upper_rest_now)
        val temp = "${String.format(
            FORMAT,
            convertSecondsToMinutes(restSecondsIni).first
        )}:${String.format(FORMAT, convertSecondsToMinutes(restSecondsIni).second + 1)}"
        timeTV.text = temp
        startTimer(restSecondsIni + 1)
        currentSetNumber -= 1
    }

    private fun iniDone() {
        currentStep = -1
        stepTV.isVisible = false
        stepCountTV.isVisible = false
        timeTV.text = resources.getString(R.string.upper_done)
        playPauseB.setImageResource(R.drawable.ic_play_arrow_24px)
    }

    private fun getValues() {
        setNumberIni = intent.getIntExtra(AppConstants.INTENT_SET_NUMBER, 0)
        currentSetNumber = setNumberIni
        workSecondsIni = intent.getIntExtra(AppConstants.INTENT_WORK_INTERVAL, 0)
        restSecondsIni = intent.getIntExtra(AppConstants.INTENT_REST_INTERVAL, 0)
    }

    private fun iniPhasesList() {
        val phases: Array<String> = Array(setNumberIni * 2 + 2){""}
        phases[0] = "1. ${getString(R.string.upper_get_ready)}: 5"
        var i = 1
        while (i < setNumberIni * 2 + 1) {
            if (i % 2 != 0)
                phases[i] = "${i+1}. ${getString(R.string.upper_work_it)}: $workSecondsIni"
            else
                phases[i] = "${i+1}. ${getString(R.string.upper_rest_now)}: $restSecondsIni"
            i++
        }
        phases[i] = "${i+1}. ${getString(R.string.upper_done)}"

        phasesTitle.movementMethod = ScrollingMovementMethod.getInstance()
        for (phase in phases) {
            val textView = TextView(this)
            textView.movementMethod = ScrollingMovementMethod.getInstance()
            textView.text = phase
            textView.textSize = 30F
            textView.verticalScrollbarPosition
            phasesListLayout.addView(textView)
        }
    }

    private fun decreaseTV(textViewTime: TextView) {

        if (currentStep == 1)
            mpRest.start()
        else
            mpWork.start()

        var currentTime = textViewTime.text.toString()
        var seconds = getTimeFromStr(currentTime).second
        var minutes = getTimeFromStr(currentTime).first
        if (minutes != 0) {
            if (seconds == 0) {
                seconds = 59
                minutes -= 1
            } else {
                seconds -= 1
            }
        } else {
            if (seconds in 1..4) {
                seconds -= 1
                var vibrationTime = 100L
                if (seconds == 0) {
                    seconds = 0
                    minutes = 0
                    vibrationTime = 500L
                }
                val vib  = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager =  getSystemService(Context.VIBRATOR_MANAGER_SERVICE)
                            as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    getSystemService(VIBRATOR_SERVICE) as Vibrator
                }

                val canVibrate: Boolean = vib.hasVibrator()

                if (canVibrate) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // API 26
                        vib.vibrate(
                            VibrationEffect.createOneShot(
                                vibrationTime,
                                VibrationEffect.DEFAULT_AMPLITUDE
                            )
                        )
                    } else {
                        // This method was deprecated in API level 26
                        vib.vibrate(vibrationTime)
                    }
                }
            } else {
                seconds -= 1
            }
        }
        currentTime = String.format(FORMAT, minutes) + ":" + String.format(FORMAT, seconds)
        textViewTime.text = currentTime
    }

    private fun startTimer(sec: Int) {
        timer = object : CountDownTimer((sec * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                decreaseTV(timeTV)
            }

            override fun onFinish() {
                if (currentSetNumber != 0) {
                    if (currentStep == 0 || currentStep == 2) {
                        iniWorkout()
                    } else if (currentStep == 1) {
                        iniRest()
                    } else {
                        finish()
                    }
                } else {
                    iniDone()
                }
            }
        }
        (timer as CountDownTimer).start()
    }

    private fun cancelTimer() {
        timer?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelTimer()
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