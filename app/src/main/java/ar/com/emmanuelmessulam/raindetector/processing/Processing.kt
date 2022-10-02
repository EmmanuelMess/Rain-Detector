package ar.com.emmanuelmessulam.raindetector.processing

import ar.com.emmanuelmessulam.raindetector.NextRain
import ar.com.emmanuelmessulam.raindetector.SmnApi
import arrow.core.Either
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.*

abstract class Processing(val smnApi: SmnApi) {
    abstract suspend fun nextRain(): Either<Exception, NextRain>
}