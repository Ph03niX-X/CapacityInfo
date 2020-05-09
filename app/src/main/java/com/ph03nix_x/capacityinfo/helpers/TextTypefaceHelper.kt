package com.ph03nix_x.capacityinfo.helpers

import android.content.Context
import android.graphics.Typeface
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.TypefaceCompat
import com.ph03nix_x.capacityinfo.R

object TextTypefaceHelper {

    fun setTextAppearance(textView: AppCompatTextView, textStylePref: String?,
                          textFontPref: String?): Typeface {

        val fontFamily = setTextFont(textView.context, textFontPref)

        return setTextStyle(textView, textStylePref, fontFamily)
    }

    private fun setTextStyle(textView: AppCompatTextView, textStylePref: String?,
                             fontFamily: Typeface?): Typeface {

        return when(textStylePref?.toInt()) {

            0 -> TypefaceCompat.create(textView.context, fontFamily, Typeface.NORMAL)

            1 -> TypefaceCompat.create(textView.context, fontFamily, Typeface.BOLD)

            2 -> TypefaceCompat.create(textView.context, fontFamily, Typeface.ITALIC)

            else -> TypefaceCompat.create(textView.context, fontFamily, Typeface.NORMAL)
        }
    }

    private fun setTextFont(context: Context, textFontPref: String?): Typeface? {

        return when(textFontPref?.toInt()) {

            0 -> Typeface.DEFAULT

            1 -> ResourcesCompat.getFont(context, R.font.roboto)

            2 -> Typeface.SERIF

            3 -> Typeface.SANS_SERIF

            4 -> Typeface.MONOSPACE

            5 -> ResourcesCompat.getFont(context, R.font.open_sans)

            6 -> ResourcesCompat.getFont(context, R.font.google_sans)

            7 -> ResourcesCompat.getFont(context, R.font.san_francisco)

            8 -> ResourcesCompat.getFont(context, R.font.times_new_roman)

            9 -> ResourcesCompat.getFont(context, R.font.ubuntu)

            else -> ResourcesCompat.getFont(context, R.font.google_sans)
        }
    }
}