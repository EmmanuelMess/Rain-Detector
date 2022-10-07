package ar.com.emmanuelmessulam.raindetector.processing

import ar.com.emmanuelmessulam.raindetector.dataclasses.City
import ar.com.emmanuelmessulam.raindetector.dataclasses.NextRain
import ar.com.emmanuelmessulam.raindetector.smn.SmnApi
import arrow.core.Either

abstract class Processing(val smnApi: SmnApi) {
    abstract suspend fun nextRain(city: City): Either<Exception, NextRain>
}