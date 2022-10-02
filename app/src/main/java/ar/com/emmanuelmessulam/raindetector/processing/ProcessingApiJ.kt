package ar.com.emmanuelmessulam.raindetector.processing

import ar.com.emmanuelmessulam.raindetector.NextRain
import ar.com.emmanuelmessulam.raindetector.SmnApi
import arrow.core.Either
import arrow.core.continuations.either
import java.util.*

class ProcessingApiJ(smnApi: SmnApi) : Processing(smnApi) {
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

        return@either NextRain(true, probabilities[nextRainIndex], addDays(Date(), nextRainIndex))
    }

    private fun addDays(date: Date, days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DATE, days)
        return calendar.time
    }

}