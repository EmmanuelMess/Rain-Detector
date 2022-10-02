package ar.com.emmanuelmessulam.raindetector

import android.util.Log
import androidx.annotation.WorkerThread
import arrow.core.Either
import arrow.core.continuations.either
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URL
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class SmnApiV1 : SmnApi() {
    private val url = "https://ssl.smn.gob.ar/dpd/zipopendata.php?dato=pron5d"
    private val regex = Pattern.compile("""\d{2}/[A-Z]{3}/\d{4} \d{2}Hs. *\d+.\d+ *\d+ \| *\d+ *(\d+.\d+)""")

    @WorkerThread
    override suspend fun getRainProbabilities(): Either<Exception, List<Probability>> {
        val result = either {
            val zipFile = downloadFile().bind()
            val textFile = unzip(zipFile).bind()
            val inputAsString =
                textFile.bufferedReader().use { it.readText() }  // defaults to UTF-8
            val rosario = inputAsString.substring(
                inputAsString.indexOf("ROSARIO_AERO"),
                inputAsString.indexOf("SALTA_AERO")
            )

            val matcher = regex.matcher(rosario)
            val allLines = mutableListOf<String?>()
            while(matcher.find()) {
                allLines.add(matcher.group(1))
            }
            val precipitations = allLines.map {
                it?.toFloat() ?: throw Exception("Broken regex")
            }
            val probabilities = precipitations.map {
                if (it >= 0.1) Probability(0.1f, 1.0f) else Probability(0.0f, 0.0f)
            }

            return@either probabilities.toList()
        }

        return result
    }

    @WorkerThread
    private suspend fun downloadFile(): Either<Exception, DataInputStream> = either {
        val input = withContext(Dispatchers.IO) {
            val u = URL(url)
            u.openConnection()
            DataInputStream(u.openStream())
        }
        return@either input
    }

    @WorkerThread
    suspend fun unzip(inputStream: DataInputStream): Either<Exception, ByteArrayInputStream> =
        either {
            val byteArrayInputStream: ByteArrayInputStream = withContext(Dispatchers.IO) {
                val r: ByteArrayInputStream
                ZipInputStream(inputStream).use { zin ->
                    val zipEntry: ZipEntry? = zin.nextEntry
                    zipEntry ?: throw FileNotFoundException()
                    r = ByteArrayInputStream(zin.readBytes())
                    zin.closeEntry()
                }
                return@withContext r
            }

            return@either byteArrayInputStream
        }

}