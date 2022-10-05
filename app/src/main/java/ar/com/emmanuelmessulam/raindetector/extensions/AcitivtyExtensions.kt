package ar.com.emmanuelmessulam.raindetector

import android.app.Activity
import android.os.Build
import android.view.View
import androidx.annotation.IdRes

fun <T: View> Activity.view(@IdRes id: Int): T {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        requireViewById(id)
    } else {
        findViewById(id)
    }
}