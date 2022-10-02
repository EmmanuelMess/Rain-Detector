package ar.com.emmanuelmessulam.raindetector

import androidx.annotation.WorkerThread
import arrow.core.Either

abstract class SmnApi {
    @WorkerThread
    abstract suspend fun getRainProbabilities(): Either<Exception, List<Probability>>
}