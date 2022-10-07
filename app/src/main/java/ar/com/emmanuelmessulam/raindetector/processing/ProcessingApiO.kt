package ar.com.emmanuelmessulam.raindetector.processing

import android.os.Build
import androidx.annotation.RequiresApi
import ar.com.emmanuelmessulam.raindetector.dataclasses.City
import ar.com.emmanuelmessulam.raindetector.dataclasses.NextRain
import ar.com.emmanuelmessulam.raindetector.smn.SmnApi
import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.flatMap
import java.time.LocalDate
import java.time.ZoneId
import java.util.*


@RequiresApi(Build.VERSION_CODES.O)
class ProcessingApiO(smnApi: SmnApi) : Processing(smnApi) {
    override suspend fun nextRain(city: City): Either<Exception, NextRain> {
        return smnApi.getRainProbabilities(city).flatMap { probabilities ->
            if(probabilities.isEmpty()) {
                return Either.Left(Exception("Empty probablity list"))
            }

            val nextRainIndex = probabilities.indexOfFirst { probability ->
                probability.high > 0
            }

            if (nextRainIndex == -1) {
                return Either.Right(NextRain(false, null, null))
            }

            return Either.Right(NextRain(true, probabilities[nextRainIndex], addDays(LocalDate.now(), nextRainIndex.toLong()).toDate()))
        }
    }

    private fun addDays(date: LocalDate, days: Long): LocalDate {
        return date.plusDays(days)
    }

    private fun LocalDate.toDate(): Date {
        return Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }
}