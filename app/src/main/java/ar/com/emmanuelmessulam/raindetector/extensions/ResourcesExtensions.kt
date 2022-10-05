package ar.com.emmanuelmessulam.raindetector

import android.content.res.Resources
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.Nullable

@ColorInt
fun Resources.color(@ColorRes id: Int, @Nullable theme: Resources.Theme): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        getColor(R.color.teal_200, theme)
    } else {
        getColor(R.color.teal_200)
    }
}