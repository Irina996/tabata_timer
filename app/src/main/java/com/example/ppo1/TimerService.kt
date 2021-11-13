package com.example.ppo1

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import android.widget.TextView
import androidx.core.view.isVisible
import com.example.ppo1.util.PrefUtil
import kotlinx.android.synthetic.main.activity_timer.*

class TimerService : Service() {
    private var iniSetNumber: Int = 0
    private var iniWorkSeconds: Int = 0
    private var iniRestSeconds: Int = 0
    private var iniWarmUpSeconds: Int = 0
    private var iniCoolDownSeconds: Int = 0
    private var currentSetNumber: Int = 0
    private var currentStep = TimerActivity.TimerStep.WarmUp
    private var currentTime: Int = 0  // seconds remaining
    private var isPaused: Boolean = false
    private var timer: CountDownTimer? = null

    private lateinit var mpRest: MediaPlayer
    private lateinit var mpWork: MediaPlayer

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mpRest = MediaPlayer.create(this, R.raw.beep)
        mpWork = MediaPlayer.create(this, R.raw.boop)

        getTimerData()

        initTimer()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()

        cancelTimer()
        setTimerData()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun setTimerData() {
        PrefUtil.setIniSetNumber(iniSetNumber, this)
        PrefUtil.setCurrentSetNumber(currentSetNumber, this)
        PrefUtil.setIniWorkSeconds(iniWorkSeconds, this)
        PrefUtil.setIniRestSeconds(iniRestSeconds, this)
        PrefUtil.setIniWarmUpSeconds(iniWarmUpSeconds, this)
        PrefUtil.setIniCoolDownSeconds(iniCoolDownSeconds, this)
        PrefUtil.setCurrentStepNumber(currentStep, this)
        PrefUtil.setCurrentTime(currentTime, this)
        PrefUtil.setTimerState(isPaused, this)
    }

    private fun getTimerData() {
        iniSetNumber = PrefUtil.getIniSetNumber(this)
        currentSetNumber = PrefUtil.getCurrentSetNumber(this)
        iniWorkSeconds = PrefUtil.getIniWorkSeconds(this)
        iniRestSeconds = PrefUtil.getIniRestSeconds(this)
        iniWarmUpSeconds = PrefUtil.getIniWarmUpSeconds(this)
        iniCoolDownSeconds = PrefUtil.getIniCoolDownSeconds(this)
        currentStep = PrefUtil.getCurrentStepNumber(this)
        currentTime = PrefUtil.getCurrentTime(this)
        isPaused = PrefUtil.getTimerState(this)
    }

    private fun initTimer() {
        getTimerData()

        val alarmSetTime = PrefUtil.getAlarmSetTime(this)
        if (alarmSetTime > 0)
            currentTime -= TimerActivity.nowSeconds.toInt() - alarmSetTime.toInt()

        when (currentStep) {
            TimerActivity.TimerStep.WarmUp -> iniGetReady(currentTime)
            TimerActivity.TimerStep.Work -> iniWorkout(currentTime)
            TimerActivity.TimerStep.Rest -> iniRest(currentTime)
            TimerActivity.TimerStep.CoolDown -> iniCoolDown(currentTime)
            TimerActivity.TimerStep.Done -> iniDone()
        }
        if (isPaused) {
            cancelTimer()
        }
    }

    private fun startTimer(sec: Int) {
        currentTime = sec

        timer = object : CountDownTimer((sec * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                onTimerTick()
            }

            override fun onFinish() {
                if (currentSetNumber != iniSetNumber + 1) {
                    if (currentStep == TimerActivity.TimerStep.WarmUp || currentStep == TimerActivity.TimerStep.Rest) {
                        iniWorkout()
                    } else if (currentStep == TimerActivity.TimerStep.Work) {
                        if (currentSetNumber == iniSetNumber)
                            iniCoolDown()
                        else
                            iniRest()
                    } else if (currentStep == TimerActivity.TimerStep.CoolDown) {
                        iniDone()
                    }
                } else {
                    iniDone()
                }
            }
        }
        (timer as CountDownTimer).start()
    }

    private fun onTimerTick() {

        if (currentStep == TimerActivity.TimerStep.Work)
            mpWork.start()
        else
            mpRest.start()

        var seconds = currentTime % 60
        var minutes = (currentTime - seconds) / 60
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
                val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE)
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
        currentTime = minutes * 60 + seconds
    }

    private fun cancelTimer() {
        timer?.cancel()
    }

    private fun iniGetReady(secondsRemaining: Int = -1) {
        currentStep = TimerActivity.TimerStep.WarmUp // 0
        if (secondsRemaining == -1)
            startTimer(iniWarmUpSeconds)
        else
            startTimer(secondsRemaining)
        currentSetNumber += 1
    }

    private fun iniWorkout(secondsRemaining: Int = -1) {
        currentStep = TimerActivity.TimerStep.Work
        if (secondsRemaining == -1)
            startTimer(iniWorkSeconds)
        else
            startTimer(secondsRemaining)
    }

    private fun iniRest(secondsRemaining: Int = -1) {
        currentStep = TimerActivity.TimerStep.Rest
        if (secondsRemaining == -1)
            startTimer(iniRestSeconds)
        else
            startTimer(secondsRemaining)
        currentSetNumber += 1
    }

    private fun iniCoolDown(secondsRemaining: Int = -1) {
        currentStep = TimerActivity.TimerStep.CoolDown
        if (secondsRemaining == -1)
            startTimer(iniCoolDownSeconds)
        else
            startTimer(secondsRemaining)
        currentSetNumber += 1
    }

    private fun iniDone() {
        currentStep = TimerActivity.TimerStep.Done // -1
    }
}