package com.example.ppo1

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings.Global.getInt
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.example.ppo1.util.PrefUtil
import com.example.ppo1.util.convertMinutesToSeconds
import com.example.ppo1.util.convertSecondsToMinutes
import com.example.ppo1.util.getTimeFromStr
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_timer.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNewB.setOnClickListener {
            val intent = Intent(this, WorkoutActivity::class.java)
            intent.putExtra("FILE_NAME", "")
            this.startActivity(intent)
            Log.d("CREATE NEW", "CREATE NEW")
        }

        openActBtn.setOnClickListener {
            val intent = Intent(this, EntryActivity::class.java)
            this.startActivity(intent)
        }
        // iniButtons()
    }

    override fun onResume() {
        super.onResume()

        iniButtons()
    }

    private fun iniButtons() {
        workoutListLayout.removeAllViews()

        val listOfNames = PrefUtil.getFileNames(this)

        var temp = listOfNames
        while (temp != "") {
            val fileName = temp.substringBefore(',')
            temp = temp.replace(fileName, "")
            if (temp != "" && temp[0] == ',') {
                temp = temp.substringAfter(", ")
            }
            val title = fileName.substringAfter("ppo1.").substringBeforeLast(')')

            val button = Button(this)
            button.movementMethod = ScrollingMovementMethod.getInstance()
            button.text = title
            button.textSize = resources.getDimension(R.dimen.workout_name)
            button.verticalScrollbarPosition
            workoutListLayout.addView(button)
            button.setOnClickListener {
                val fileName =  "(com.example.ppo1.${button.text})"
                val intent = Intent(this, WorkoutActivity::class.java)
                intent.putExtra("FILE_NAME", fileName)
                this.startActivity(intent)
            }
        }
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