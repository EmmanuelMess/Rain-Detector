package ar.com.emmanuelmessulam.raindetector

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import ar.com.emmanuelmessulam.raindetector.processing.ProcessingApiJ
import ar.com.emmanuelmessulam.raindetector.processing.ProcessingApiO
import arrow.core.Either
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        run()
    }

    private fun run() = CoroutineScope(Dispatchers.Main).launch {
        val smn: SmnApi = SmnApiV1()
        val process = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ProcessingApiO(smn)
        } else {
            ProcessingApiJ(smn)
        }

        val view: TextView = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            requireViewById(R.id.rainTextView)
        } else {
            findViewById(R.id.rainTextView)
        }

        val result = process.nextRain()

        view.text = getText(result)
        view.visibility = View.VISIBLE

        notification(result)
    }

    private fun notification(result: Either<Exception, NextRain>) {
        val id = 0
        val channel = "channel"

        val notificationManager = getSystemService<NotificationManager>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channel, "Rain", NotificationManager.IMPORTANCE_LOW)
            notificationManager?.createNotificationChannel(notificationChannel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channel).apply {
            setAutoCancel(true)
            setSmallIcon(R.drawable.ic_twotone_cloud_24)
            setTicker("Rain")
            setContentTitle(getString(R.string.app_name))
            setContentText(getText(result))
            setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND)
            setContentInfo("Info")
        }.build()

        notificationManager?.notify(id, notification)
    }

    private fun getText(result: Either<Exception, NextRain>): String {
        when (result) {
            is Either.Left<Exception> -> {
                return result.value.localizedMessage?.toString() ?: return result.value.message.toString()
            }
            is Either.Right<NextRain> -> {
                if (result.value.isRain) {
                    require(result.value.date != null && result.value.probability != null)

                    val format = SimpleDateFormat("EEEE", Locale.getDefault())
                    val weekDay = format.format(result.value.date)

                    return getString(
                        R.string.rain,
                        weekDay,
                        result.value.probability
                    )
                } else {
                    return getString(R.string.no_rain)
                }
            }
        }
    }
}