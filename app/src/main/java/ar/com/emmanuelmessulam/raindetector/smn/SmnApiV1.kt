package ar.com.emmanuelmessulam.raindetector.smn

import android.util.Log
import androidx.annotation.WorkerThread
import ar.com.emmanuelmessulam.raindetector.dataclasses.City
import ar.com.emmanuelmessulam.raindetector.dataclasses.Probability
import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.flatMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.FileNotFoundException
import java.net.URL
import java.util.regex.Pattern
import java.util.regex.Pattern.MULTILINE
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class SmnApiV1 : SmnApi() {
    private val url = "https://ssl.smn.gob.ar/dpd/zipopendata.php?dato=pron5d"
    private val lineRegex = Pattern.compile("""\d{2}/[A-Z]{3}/\d{4} \d{2}Hs. *\d+.\d+ *\d+ \| *\d+ *(\d+.\d+)""")
    private val cityNamesRegex = Pattern.compile("""^ ([A-Z_]+)$""", MULTILINE)

    @WorkerThread
    override suspend fun getRainProbabilities(city: City): Either<Exception, List<Probability>> {
        val zipFile = downloadFile()
        val textFile = zipFile.flatMap { unzip(it) }
        val inputAsString: String

        when (val input = textFile.map { it.bufferedReader().use { reader -> reader.readText() }  }) {// defaults to UTF-8
            is Either.Left<Exception> -> return input
            is Either.Right<String> -> inputAsString = input.value
        }

        if(inputAsString.isEmpty()) {
            return Either.Left(Exception("Empty file! API is down!"))
        }

        val indexOfCityStart = inputAsString.indexOf(city.id)

        val cityDataWithExtras = inputAsString.substring(indexOfCityStart)

        val matcherNames = cityNamesRegex.matcher(cityDataWithExtras)

        if(!matcherNames.find(city.id.length)) {
            return Either.Left(Exception("Broken cityNamesRegex"))
        }
        val nextCityIndex = matcherNames.start(1)

        val cityData = cityDataWithExtras.substring(0, nextCityIndex)

        val matcher = lineRegex.matcher(cityData)

        val allLines = mutableListOf<String>()
        while(matcher.find()) {
            val group = matcher.group(1) ?: return Either.Left(Exception("Broken lineRegex"))
            allLines.add(group)
        }
        val precipitations = allLines.map(String::toFloat)

        val probabilities = precipitations.map {
            if (it >= 0.1) {
                Probability(0.1f, 1.0f)
            } else {
                Probability(0.0f, 0.0f)
            }
        }

        return Either.Right(probabilities.toList())
    }

    @WorkerThread
    override suspend fun getCities(): Either<Exception, List<City>> {
        val zipFile = downloadFile()
        val textFile = zipFile.flatMap { unzip(it) }
        val inputAsString: String

        when (val input = textFile.map { it.bufferedReader().use { reader -> reader.readText() }  }) {// defaults to UTF-8
            is Either.Left<Exception> -> return input
            is Either.Right<String> -> inputAsString = input.value
        }

        if(inputAsString.isEmpty()) {
            return Either.Left(Exception("Empty file! API is down!"))
        }

        val matcher = cityNamesRegex.matcher(inputAsString)

        val allLines = mutableListOf<String>()
        while(matcher.find()) {
            val group = matcher.group(1) ?: return Either.Left(Exception("Broken cityNamesRegex"))
            allLines.add(group)
        }

        if(allLines.isEmpty()) {
            return Either.Left(Exception("Empty list"))
        }

        val cities = allLines.map { rawName ->
            val name = rawName.removeUnderscores()
            val capitalized = name.lowercase().capitalizeWords()
            City(capitalized, rawName)
        }

        return Either.Right(cities)
    }

    @WorkerThread
    private suspend fun downloadFile(): Either<Exception, DataInputStream> {
        val input = withContext(Dispatchers.IO) {
            val u = URL(url)
            u.openConnection()
            DataInputStream(u.openStream())
        }
        return Either.Right(input)
    }

    @WorkerThread
    suspend fun unzip(inputStream: DataInputStream): Either<Exception, ByteArrayInputStream>
            = withContext(Dispatchers.IO) {
        val r: Either<Exception, ByteArrayInputStream>
        ZipInputStream(inputStream).use { zin ->
            val zipEntry: ZipEntry? = zin.nextEntry
            r = if (zipEntry == null) {
                Either.Left(FileNotFoundException())
            } else {
                Either.Right(ByteArrayInputStream(zin.readBytes()))
            }
            zin.closeEntry()
        }
        return@withContext r
    }

    private fun String.removeUnderscores(): String = replace('_', ' ')

    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { w -> w.replaceFirstChar { c ->  c.uppercaseChar() } }

}