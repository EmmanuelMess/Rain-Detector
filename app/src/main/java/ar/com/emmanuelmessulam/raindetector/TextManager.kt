package ar.com.emmanuelmessulam.raindetector

import android.content.Context
import ar.com.emmanuelmessulam.raindetector.dataclasses.NextRain
import arrow.core.Either
import java.text.SimpleDateFormat
import java.util.*

object TextManager {
    @JvmStatic
    fun getText(context: Context, result: Either<Exception, NextRain>): String {
        when (result) {
            is Either.Left<Exception> -> {
                return result.value.localizedMessage?.toString() ?: return result.value.message.toString()
            }
            is Either.Right<NextRain> -> {
                if (result.value.isRain) {
                    require(result.value.date != null && result.value.probability != null)

                    val format = SimpleDateFormat("EEEE", Locale.getDefault())
                    val weekDay = format.format(result.value.date)

                    return context.getString(
                        R.string.rain,
                        weekDay,
                        result.value.probability
                    )
                } else {
                    return context.getString(R.string.no_rain)
                }
            }
        }
    }
}