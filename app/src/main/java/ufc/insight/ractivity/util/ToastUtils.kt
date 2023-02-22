package ufc.insight.ractivity.util

import android.content.Context
import android.widget.Toast

class ToastUtils {
    companion object {
        fun showToast(applicationContext: Context, text: String) {
            Toast.makeText(
                applicationContext,
                text,
                Toast.LENGTH_LONG
            ).show()
        }


    }
}