package com.ph03nix_x.capacityinfo.helpers

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.TypefaceCompat
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface
import com.ph03nix_x.capacityinfo.utilities.Constants

object TextAppearanceHelper : PremiumInterface {

    fun setTextAppearance(context: Context, textViewArrayList: ArrayList<AppCompatTextView>,
                          textStylePref: String?,
                          textFontPref: String?, textSizePref: String?) {
        val isPremium = PremiumInterface.isPremium
        textViewArrayList.forEach {
            setTextSize(context, it, textSizePref)
            val fontFamily = setTextFont(it.context,
                if(isInstalledFromGooglePlay(context) && isPremium) textFontPref else "6")
            it.typeface = setTextStyle(it, textStylePref, fontFamily)
        }
    }

    @Suppress("DEPRECATION")
    private fun isInstalledFromGooglePlay(context: Context) =
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            Constants.GOOGLE_PLAY_PACKAGE_NAME == context.packageManager.getInstallSourceInfo(
                context.packageName).installingPackageName
        else Constants.GOOGLE_PLAY_PACKAGE_NAME == context.packageManager
            .getInstallerPackageName(context.packageName)

    fun setTextAppearance(context: Context, textView: AppCompatTextView, textStylePref: String?,
                          textFontPref: String?, textSizePref: String?) {
        setTextSize(context, textView, textSizePref)
        val isPremium = PremiumInterface.isPremium
        val fontFamily = setTextFont(textView.context,
            if(isInstalledFromGooglePlay(context) && isPremium) textFontPref else "6")
        textView.typeface = setTextStyle(textView, textStylePref, fontFamily)
    }

    private fun setTextFont(context: Context, textFontPref: String?): Typeface? {
        return when(textFontPref?.toInt()) {
            0 -> Typeface.DEFAULT
            1 -> ResourcesCompat.getFont(context, R.font.roboto)
            2 -> Typeface.SERIF
            3 -> Typeface.SANS_SERIF
            4 -> Typeface.MONOSPACE
            5 -> ResourcesCompat.getFont(context, R.font.inter)
            6 -> ResourcesCompat.getFont(context, R.font.google_sans)
            7 -> ResourcesCompat.getFont(context, R.font.times_new_roman)
            8 -> ResourcesCompat.getFont(context, R.font.ubuntu)
            9 -> ResourcesCompat.getFont(context, R.font.lora)
            10 -> ResourcesCompat.getFont(context, R.font.oswald)
            11 -> ResourcesCompat.getFont(context, R.font.pt_sans)
            12 -> ResourcesCompat.getFont(context, R.font.pt_serif)
            13 -> ResourcesCompat.getFont(context, R.font.open_sans)
            14 -> ResourcesCompat.getFont(context, R.font.noto_sans)
            15 -> ResourcesCompat.getFont(context, R.font.nunito_sans)
            16 -> ResourcesCompat.getFont(context, R.font.work_sans)
            17 -> ResourcesCompat.getFont(context, R.font.merriweather_sans)
            18 -> ResourcesCompat.getFont(context, R.font.sf_pro)
            19 -> ResourcesCompat.getFont(context, R.font.lobster)
            20 -> ResourcesCompat.getFont(context, R.font.moon_dance)
            21 -> ResourcesCompat.getFont(context, R.font.rubik)
            22 -> ResourcesCompat.getFont(context, R.font.playfair_display)
            23 -> ResourcesCompat.getFont(context, R.font.rowdies)
            24 -> ResourcesCompat.getFont(context, R.font.raleway)
            25 -> ResourcesCompat.getFont(context, R.font.montserrat)
            26 -> ResourcesCompat.getFont(context, R.font.sono)
            27 -> ResourcesCompat.getFont(context, R.font.rubik_iso)
            28 -> ResourcesCompat.getFont(context, R.font.roboto_condensed)
            29 -> ResourcesCompat.getFont(context, R.font.poppins)
            30 -> ResourcesCompat.getFont(context, R.font.kanit)
            31 -> ResourcesCompat.getFont(context, R.font.playfair)
            32 -> ResourcesCompat.getFont(context, R.font.mukta)
            33 -> ResourcesCompat.getFont(context, R.font.mooli)
            34 -> ResourcesCompat.getFont(context, R.font.inclusive_sans)
            35 -> ResourcesCompat.getFont(context, R.font.borel)
            36 -> ResourcesCompat.getFont(context, R.font.handjet)
            37 -> ResourcesCompat.getFont(context, R.font.ysabeau_sc)
            38 -> ResourcesCompat.getFont(context, R.font.ysabeau_office)
            39 -> ResourcesCompat.getFont(context, R.font.ysabeau_infant)
            40 -> ResourcesCompat.getFont(context, R.font.dm_sans)
            41 -> ResourcesCompat.getFont(context, R.font.poetsen_one)
            42 -> ResourcesCompat.getFont(context, R.font.sedan_sc)
            43 -> ResourcesCompat.getFont(context, R.font.lora)
            44 -> ResourcesCompat.getFont(context, R.font.mulish)
            45 -> ResourcesCompat.getFont(context, R.font.agdasima)
            46 -> ResourcesCompat.getFont(context, R.font.arima)
            47 -> ResourcesCompat.getFont(context, R.font.prociono)
            48 -> ResourcesCompat.getFont(context, R.font.rethink_sans)
            else -> null
        }
    }


    private fun setTextStyle(textView: AppCompatTextView, textStylePref: String?,
                             fontFamily: Typeface?): Typeface? {
        return when(textStylePref?.toInt()) {
            0 -> TypefaceCompat.create(textView.context, fontFamily, Typeface.NORMAL)
            1 -> TypefaceCompat.create(textView.context, fontFamily, Typeface.BOLD)
            2 -> TypefaceCompat.create(textView.context, fontFamily, Typeface.ITALIC)
            else -> null
        }
    }

    private fun setTextSize(context: Context, textView: AppCompatTextView, textSizePref: String?) {
        when(textSizePref?.toInt()) {
            0 -> textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                context.resources.getDimension(R.dimen.very_small_text_size))
            1 -> textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                context.resources.getDimension(R.dimen.small_text_size))
            2 -> textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                context.resources.getDimension(R.dimen.medium_text_size))
            3 -> textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                context.resources.getDimension(R.dimen.large_text_size))
            4 -> textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                context.resources.getDimension(R.dimen.very_large_text_size))
        }
    }
}