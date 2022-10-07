package ar.com.emmanuelmessulam.raindetector.smn

import androidx.annotation.WorkerThread
import ar.com.emmanuelmessulam.raindetector.dataclasses.City
import ar.com.emmanuelmessulam.raindetector.dataclasses.Probability
import arrow.core.Either

abstract class SmnApi {
    @WorkerThread
    abstract suspend fun getRainProbabilities(city: City): Either<Exception, List<Probability>>

    @WorkerThread
    abstract suspend fun getCities(): Either<Exception, List<City>>
}