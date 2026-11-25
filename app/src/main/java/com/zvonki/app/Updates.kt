package com.zvonki.app

import android.app.Activity
import android.content.Context
import android.os.Build
import android.widget.Toast
import java.net.HttpURLConnection
import java.net.URL

// Проверка новой версии: номер последней сборки лежит в version.txt на GitHub
object Updates {
    private const val VERSION_URL = "https://github.com/lvvav/-/releases/download/latest/version.txt"

    fun currentCode(ctx: Context): Long {
        val info = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
        return if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else @Suppress("DEPRECATION") info.versionCode.toLong()
    }

    fun versionName(ctx: Context): String =
        ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "?"

    fun check(act: Activity, manual: Boolean) {
        Thread {
            try {
                val c = URL(VERSION_URL).openConnection() as HttpURLConnection
                c.connectTimeout = 10000
                c.readTimeout = 10000
                val latest = c.inputStream.bufferedReader().use { it.readText().trim().toLongOrNull() ?: 0L }
                c.disconnect()
                act.runOnUiThread {
                    if (latest > currentCode(act)) {
                        Glass.show(act, "Вышло обновление", listOf(
                            Glass.Row(R.drawable.ic_download, "Обновить", "Скачать и установить новую версию") { AppUpdate.start(act) },
                            Glass.Row(R.drawable.ic_refresh, "Позже", null) {}
                        ))
                    } else if (manual) {
                        Toast.makeText(act, "У тебя последняя версия", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                if (manual) act.runOnUiThread {
                    Toast.makeText(act, "Не удалось проверить обновление", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}
