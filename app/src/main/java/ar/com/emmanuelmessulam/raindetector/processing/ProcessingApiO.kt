package ar.com.emmanuelmessulam.raindetector.processing

import android.os.Build
import androidx.annotation.RequiresApi
import ar.com.emmanuelmessulam.raindetector.NextRain
import ar.com.emmanuelmessulam.raindetector.SmnApi
import arrow.core.Either
import arrow.core.continuations.either
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.*


@RequiresApi(Build.VERSION_CODES.O)
class ProcessingApiO(smnApi: SmnApi) : Processing(smnApi) {
    override suspend fun nextRain(): Either<Exception, NextRain> = either {
        val probabilities = smnApi.getRainProbabilities().bind()
        if(probabilities.isEmpty()) {
            Either.Left(Exception("Empty probablity list")).bind<NextRain>()
        }

        val nextRainIndex = probabilities.indexOfFirst { probability ->
            probability.high > 0
        }

        if (probabilities[nextRainIndex].high == 0F) {
            return@either NextRain(false, null, null)
        }

        return@either NextRain(true, probabilities[nextRainIndex], addDays(LocalDate.now(), nextRainIndex.toLong()).toDate())
    }

    private fun addDays(date: LocalDate, days: Long): LocalDate {
        return date.plusDays(days)
    }

    private fun LocalDate.toDate(): Date {
        return Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }
}