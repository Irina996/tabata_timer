package com.example.ppo1

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.ppo1.util.NotificationUtil
import com.example.ppo1.util.PrefUtil

class TimerExpiredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        /*// This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        NotificationUtil.showTimerExpired(context)

        PrefUtil.setTimerState(TimerActivity.TimerState.Stopped, context)
        PrefUtil.setAlarmSetTime(0, context)*/
    }
}