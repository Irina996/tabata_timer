package com.example.ppo1

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.*

class TimerService : Service() {
    private var iniSetNumber: Int = 0
    private var iniWorkSeconds: Int = 0
    private var iniRestSeconds: Int = 0
    private var iniWarmUpSeconds: Int = 0
    private var iniCoolDownSeconds: Int = 0
    private var currentSetNumber: Int = 0
    private var currentStep: Int = 0
    private var currentTime: Int = 0  // seconds remaining
    private var timer: CountDownTimer? = null

    private lateinit var mpRest: MediaPlayer
    private lateinit var mpWork: MediaPlayer

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mpRest = MediaPlayer.create(this, R.raw.beep)
        mpWork = MediaPlayer.create(this, R.raw.boop)

        if (intent != null) {
            getValues(intent)
        }

        initTimer(currentTime)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun getValues(intent: Intent) {
        iniSetNumber = intent.getIntExtra(AppConstants.INTENT_SET_NUMBER, 0)
        currentSetNumber = intent.getIntExtra(AppConstants.CURRENT_SET_NUMBER, 0)
        currentStep = intent.getIntExtra(AppConstants.CURRENT_STEP_NUMBER, 0)
        currentTime = intent.getIntExtra(AppConstants.CURRENT_TIME, 0)
        iniWorkSeconds = intent.getIntExtra(AppConstants.INTENT_WORK_INTERVAL, 0)
        iniRestSeconds = intent.getIntExtra(AppConstants.INTENT_REST_INTERVAL, 0)
        iniWarmUpSeconds = 5
        iniCoolDownSeconds = 10
    }

    private fun startTimer(sec: Int) {
        timer = object : CountDownTimer((sec * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                onTimerTick(currentTime)
            }

            override fun onFinish() {
                if (currentSetNumber != iniSetNumber + 1) {
                    if (currentStep == 0 || currentStep == 2) {
                        iniWorkout()
                    } else if (currentStep == 1) {
                        iniRest()
                    }
                } else {
                    iniDone()
                }
            }
        }
        (timer as CountDownTimer).start()
    }

    private fun onTimerTick(time: Int) {

        if (currentStep == 1)
            mpRest.start()
        else
            mpWork.start()

        currentTime = time
        var seconds = time % 60
        var minutes = (time - seconds) / 60
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
        currentTime = minutes * 60 + seconds
    }

    private fun initTimer(secondsRemaining: Int) {
        if (currentStep == 0) {
            iniWorkout(secondsRemaining)
        }
        else if (currentStep == 1) {
            iniRest(secondsRemaining)
        }
        else if (currentStep == 2) {
            if (currentSetNumber == iniSetNumber + 1)
                iniDone()
            else
                iniWorkout(secondsRemaining)
        }
    }

    private fun iniGetReady(secondsRemaining: Int = -1) {
        currentStep = 0
        if (secondsRemaining == -1)
            startTimer(iniWarmUpSeconds)
        else
            startTimer(secondsRemaining)
        currentSetNumber += 1
    }

    private fun iniWorkout(secondsRemaining: Int = -1) {
        currentStep = 1
        if (secondsRemaining == -1)
            startTimer(iniWorkSeconds)
        else
            startTimer(secondsRemaining)
    }

    private fun iniRest(secondsRemaining: Int = -1) {
        currentStep = 2
        if (secondsRemaining == -1)
            startTimer(iniRestSeconds)
        else
            startTimer(secondsRemaining)
        currentSetNumber += 1
    }

    private fun iniCoolDown(secondsRemaining: Int = -1) {
        currentStep = 3
        if (secondsRemaining == 0)
            startTimer(iniCoolDownSeconds)
        else
            startTimer(secondsRemaining)
        currentSetNumber += 1
    }

    private fun iniDone() {
        currentStep = -1
    }

    private fun sendDataToActivity() {
        val intent = Intent()
        intent.action = "GET_TIMER_TICK"
        intent.putExtra(AppConstants.INTENT_SET_NUMBER, iniSetNumber)
        intent.putExtra(AppConstants.INTENT_WORK_INTERVAL, iniWorkSeconds)
        intent.putExtra(AppConstants.INTENT_REST_INTERVAL, iniRestSeconds)
        intent.putExtra(AppConstants.CURRENT_SET_NUMBER, currentSetNumber)
        intent.putExtra(AppConstants.CURRENT_STEP_NUMBER, currentStep)
        intent.putExtra(AppConstants.CURRENT_TIME, currentTime)
        sendBroadcast(intent)

        /*Intent intent = new Intent("GPSLocationUpdates");
    // You can also include some extra data.
    intent.putExtra("Status", msg);
    Bundle b = new Bundle();
    b.putParcelable("Location", l);
    intent.putExtra("Location", b);
    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);*/
        /*val sendLevel = Intent()
        sendLevel.action = "GET_SIGNAL_STRENGTH"
        sendLevel.putExtra("LEVEL_DATA", "Strength_Value")
        sendBroadcast(sendLevel)*/
    }
}