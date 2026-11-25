package com.zvonki.app

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

// Панель во время звонка: таймер, динамик, микрофон. Завершить звонок можно кнопкой на телефоне
class InCallActivity : Activity() {

    private val handler = Handler(Looper.getMainLooper())
    private val startedAt = System.currentTimeMillis()
    private lateinit var timer: TextView
    private lateinit var speakerBtn: TextView
    private lateinit var muteBtn: TextView
    private val am by lazy { getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    private val tick = object : Runnable {
        override fun run() {
            val s = (System.currentTimeMillis() - startedAt) / 1000
            timer.text = "%02d:%02d".format(s / 60, s % 60)
            handler.postDelayed(this, 1000)
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun rounded(color: Int, r: Int) = GradientDrawable().apply {
        cornerRadius = dp(r).toFloat()
        setColor(color)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val number = intent.getStringExtra("number") ?: ""

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(60), dp(24), dp(32))
            background = ColorDrawable(0xFF1A130E.toInt())
        }

        root.addView(TextView(this).apply {
            text = "Разговор"
            setTextColor(0xB3FFFFFF.toInt())
            textSize = 16f
            gravity = Gravity.CENTER
        })
        root.addView(TextView(this).apply {
            text = number
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 26f
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(8))
        })
        timer = TextView(this).apply {
            text = "00:00"
            setTextColor(0xFFF2B77A.toInt())
            textSize = 20f
            gravity = Gravity.CENTER
        }
        root.addView(timer)

        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        speakerBtn = button("Динамик") {
            am.isSpeakerphoneOn = !am.isSpeakerphoneOn
            refresh()
        }
        muteBtn = button("Микрофон") {
            am.isMicrophoneMute = !am.isMicrophoneMute
            refresh()
        }
        row.addView(speakerBtn, LinearLayout.LayoutParams(0, dp(96), 1f).apply { setMargins(dp(6), dp(40), dp(6), 0) })
        row.addView(muteBtn, LinearLayout.LayoutParams(0, dp(96), 1f).apply { setMargins(dp(6), dp(40), dp(6), 0) })
        root.addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val hang = button("Завершить звонок") { endCall() }
        hang.background = rounded(0xFFB3261E.toInt(), 20)
        root.addView(hang, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)).apply { setMargins(0, dp(40), 0, 0) })

        val hide = button("Свернуть панель") { finish() }
        root.addView(hide, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)).apply { setMargins(0, dp(12), 0, 0) })

        setContentView(root)
        refresh()
        handler.post(tick)
    }

    private fun button(label: String, onTap: () -> Unit) = TextView(this).apply {
        text = label
        gravity = Gravity.CENTER
        setTextColor(0xFFFFFFFF.toInt())
        textSize = 15f
        background = rounded(0x662A1E16, 20)
        setOnClickListener { onTap() }
    }

    // Завершение звонка: нужно разрешение «отвечать на звонки», Android спросит его один раз
    private fun endCall() {
        if (checkSelfPermission(android.Manifest.permission.ANSWER_PHONE_CALLS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.ANSWER_PHONE_CALLS), 202)
            return
        }
        try {
            val tm = getSystemService(Context.TELECOM_SERVICE) as android.telecom.TelecomManager
            @Suppress("DEPRECATION")
            val ended = tm.endCall()
            if (ended) finish() else Toast.makeText(this, "Звонок не завершился, заверши кнопкой на телефоне", Toast.LENGTH_LONG).show()
        } catch (e: SecurityException) {
            Toast.makeText(this, "Нет разрешения, заверши кнопкой на телефоне", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 202 && grantResults.firstOrNull() == android.content.pm.PackageManager.PERMISSION_GRANTED) endCall()
    }

    private fun refresh() {
        speakerBtn.background = rounded(if (am.isSpeakerphoneOn) 0xFFF2B77A.toInt() else 0x662A1E16, 20)
        speakerBtn.setTextColor(if (am.isSpeakerphoneOn) 0xFF1A120C.toInt() else 0xFFFFFFFF.toInt())
        muteBtn.background = rounded(if (am.isMicrophoneMute) 0xFFF2B77A.toInt() else 0x662A1E16, 20)
        muteBtn.setTextColor(if (am.isMicrophoneMute) 0xFF1A120C.toInt() else 0xFFFFFFFF.toInt())
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        super.onDestroy()
    }
}
