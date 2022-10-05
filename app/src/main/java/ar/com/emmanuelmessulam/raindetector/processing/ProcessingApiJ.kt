package ar.com.emmanuelmessulam.raindetector.processing

import ar.com.emmanuelmessulam.raindetector.dataclasses.City
import ar.com.emmanuelmessulam.raindetector.dataclasses.NextRain
import ar.com.emmanuelmessulam.raindetector.smn.SmnApi
import arrow.core.Either
import arrow.core.flatMap
import java.util.*

class ProcessingApiJ(smnApi: SmnApi) : Processing(smnApi) {
    override suspend fun nextRain(city: City): Either<Exception, NextRain> {
        return smnApi.getRainProbabilities(city).flatMap { probabilities ->
            if(probabilities.isEmpty()) {
                return Either.Left(Exception("Empty probablity list"))
            }

            val nextRainIndex = probabilities.indexOfFirst { probability ->
                probability.high > 0
            }

            if (nextRainIndex != -1 && probabilities[nextRainIndex].high == 0F) {
                return Either.Right(NextRain(false, null, null))
            }

            return Either.Right(NextRain(true, probabilities[nextRainIndex], addDays(Date(), nextRainIndex)))
        }
    }

    private fun addDays(date: Date, days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DATE, days)
        return calendar.time
    }

}