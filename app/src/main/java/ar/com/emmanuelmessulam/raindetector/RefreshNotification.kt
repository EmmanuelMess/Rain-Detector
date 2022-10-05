package ar.com.emmanuelmessulam.raindetector

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.work.*
import ar.com.emmanuelmessulam.raindetector.dataclasses.NextRain
import ar.com.emmanuelmessulam.raindetector.persisters.CityPersister
import ar.com.emmanuelmessulam.raindetector.processing.Processing
import ar.com.emmanuelmessulam.raindetector.processing.ProcessingApiJ
import ar.com.emmanuelmessulam.raindetector.processing.ProcessingApiO
import ar.com.emmanuelmessulam.raindetector.smn.SmnApi
import ar.com.emmanuelmessulam.raindetector.smn.SmnApiV1
import arrow.core.Either
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class RefreshNotification(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    val smn: SmnApi = SmnApiV1()
    val process: Processing = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ProcessingApiO(smn)
    } else {
        ProcessingApiJ(smn)
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.Main) {
        Log.i(RefreshNotification::class.java.simpleName, "Starting worker!")

        val city = CityPersister.getCity(appContext) ?: return@withContext Result.success()

        when (val result = process.nextRain(city)) {
            is Either.Left -> return@withContext Result.retry()
            is Either.Right -> {
                RefreshNotification.notification(appContext, result)
            }
        }

        return@withContext Result.success()
    }

    companion object {
        private val WORK_NAME = "notification updater"

        suspend fun loadAndNotify(context: Context, processing: Processing): Either<Exception, Unit> {
            periodicUpdates(context)

            val city = CityPersister.getCity(context) ?: return Either.Right(Unit)

            when (val result = processing.nextRain(city)) {
                is Either.Left -> return result
                is Either.Right -> {
                    notification(context, result)
                }
            }

            return Either.Right(Unit)
        }

        private fun periodicUpdates(context: Context) {
            val constraints: Constraints = Constraints.Builder().apply {
                setRequiredNetworkType(NetworkType.CONNECTED)
                setRequiresBatteryNotLow(true)
            }.build()

            val notificationUpdater =
                PeriodicWorkRequest.Builder(
                    RefreshNotification::class.java,
                    8,
                    TimeUnit.HOURS,
                    1,
                    TimeUnit.HOURS
                )
                    .setConstraints(constraints)
                    .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    notificationUpdater
                )
        }

        private fun notification(context: Context, result: Either<Exception, NextRain>) {
            val id = 0
            val channel = "channel"

            val notificationManager = context.getSystemService<NotificationManager>()
            if(notificationManager == null) {
                Log.e(RefreshNotification::class.java.simpleName, "Notification manager is null!")
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel =
                    NotificationChannel(channel, "Rain", NotificationManager.IMPORTANCE_LOW)
                notificationManager.createNotificationChannel(notificationChannel)
            }

            val notification = NotificationCompat.Builder(context, channel).apply {
                setAutoCancel(true)
                setSmallIcon(R.drawable.ic_twotone_cloud_24)
                setTicker("Rain")
                setContentTitle(context.getString(R.string.app_name))
                setContentText(TextManager.getText(context, result))
                setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND)
                setContentInfo("Info")
            }.build()

            notificationManager.notify(id, notification)

            Log.i(RefreshNotification::class.java.simpleName, "Notification shown!")
        }
    }
}