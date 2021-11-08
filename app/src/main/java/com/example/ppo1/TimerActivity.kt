package com.example.ppo1

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_timer.*
import kotlinx.android.synthetic.main.content_timer.*
import com.example.ppo1.util.NotificationUtil
import com.example.ppo1.util.PrefUtil
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

    private lateinit var timer: CountDownTimer
    private var timerLengthSeconds = 0L
    private var timerState = TimerState.Stopped
    private var secondsRemaining = 0L

    private var startT = false
    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        context = this

        if (timerState != TimerState.Running) {
            /*startTimer()
            timerState = TimerState.Running*/
            startT = true
        }
        startBtn.setOnClickListener{
            startTimer()
            timerState = TimerState.Running
        }
    }

    override fun onResume(){
        super.onResume()

        initTimer()

        removeAlarm(this)
        NotificationUtil.hideTimerNotification(this)

        if(startT){
            startTimer()
            timerState = TimerState.Running
            startT = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onPause(){
        super.onPause()

        if (timerState == TimerState.Running) {
            timer.cancel()
            val wakeUpTime = setAlarm(this, nowSeconds, secondsRemaining)
            NotificationUtil.showTimerRunning(this, wakeUpTime)
        }
        else if (timerState == TimerState.Paused) {
            NotificationUtil.showTimerPaused(this)
        }

        PrefUtil.setPreviousTimerLengthSeconds(timerLengthSeconds, this)
        PrefUtil.setSecondsRemaining(secondsRemaining, this)
        PrefUtil.setTimerState(timerState, this)
    }

    private fun initTimer() {
        timerState =PrefUtil.getTimerState(this)

        if (timerState == TimerState.Stopped)
            setNewTimerLength()
        else
            setPreviousTimerLength()

        secondsRemaining = if (timerState == TimerState.Running || timerState == TimerState.Paused)
            PrefUtil.getSecondsRemaining(this)
        else
            timerLengthSeconds

        val alarmSetTime = PrefUtil.getAlarmSetTime(this)
        if (alarmSetTime > 0)
            secondsRemaining -= nowSeconds - alarmSetTime

        if (secondsRemaining <= 0)
            onTimerFinished()
        else if (timerState == TimerState.Running)
            startTimer()

        // updateButtons
        updateCountDownUI()
    }

    private fun onTimerFinished(){
        timerState = TimerState.Stopped

        setNewTimerLength()

        progress_countdown.progress = 0

        PrefUtil.setSecondsRemaining(timerLengthSeconds, this)
        secondsRemaining = timerLengthSeconds

        // updateButtons (ep2 20:20)
        updateCountDownUI()
    }

    private fun startTimer(){
        timerState = TimerState.Running
        timer = object: CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onFinish() = onTimerFinished()

            override fun onTick(milisecUntilFinished: Long) {

                secondsRemaining = milisecUntilFinished / 1000
                updateCountDownUI()

                if (secondsRemaining in 0..3) {
                    val vib  = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vibratorManager =  getSystemService(Context.VIBRATOR_MANAGER_SERVICE)
                                as VibratorManager
                        vibratorManager.defaultVibrator
                    } else {
                        getSystemService(VIBRATOR_SERVICE) as Vibrator
                    }

                    val canVibrate: Boolean = vib.hasVibrator()
                    val milliseconds = if (secondsRemaining == 0L) 500L
                    else 100L

                    if (canVibrate) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            // API 26
                            vib.vibrate(
                                VibrationEffect.createOneShot(
                                    milliseconds,
                                    VibrationEffect.DEFAULT_AMPLITUDE
                                )
                            )
                        } else {
                            // This method was deprecated in API level 26
                            vib.vibrate(milliseconds)
                        }
                    }
                }
            }
        }.start()
    }

    private fun setNewTimerLength(){
        val lengthInMinutes = PrefUtil.getTimerLength(this)
        timerLengthSeconds = (lengthInMinutes * 60L)
        progress_countdown.max = timerLengthSeconds.toInt()
    }

    private fun setPreviousTimerLength(){
        timerLengthSeconds = PrefUtil.getPreviousTimerLengthSeconds(this)
        progress_countdown.max = timerLengthSeconds.toInt()
    }

    private fun updateCountDownUI(){
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinuteUntilFinished.toString()
        val timeStr = if (secondsStr.length == 2) "$minutesUntilFinished:$secondsStr"
        else "$minutesUntilFinished:0$secondsInMinuteUntilFinished"
        timeTextView.text = timeStr
        progress_countdown.progress = (timerLengthSeconds - secondsRemaining).toInt()
    }
}