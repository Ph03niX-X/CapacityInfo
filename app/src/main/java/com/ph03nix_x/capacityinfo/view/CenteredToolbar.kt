package com.ph03nix_x.capacityinfo.view

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar

import com.ph03nix_x.capacityinfo.R

class CenteredToolbar : Toolbar {

    private var tvTitle: TextView? = null
    private var tvSubtitle: TextView? = null

    private var linear: LinearLayout? = null

    constructor(context: Context) : super(context) {
        setupTextViews()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setupTextViews()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setupTextViews()
    }

    override fun getTitle(): CharSequence {
        return tvTitle!!.text.toString()
    }

    override fun setTitle(@StringRes resId: Int) {
        val s = resources.getString(resId)
        title = s
    }

    override fun setTitle(title: CharSequence) {
        tvTitle!!.text = title
    }

    override fun getSubtitle(): CharSequence {
        return tvSubtitle!!.text.toString()
    }

    override fun setSubtitle(resId: Int) {
        val s = resources.getString(resId)
        subtitle = s
    }

    override fun setSubtitle(subtitle: CharSequence) {
        tvSubtitle!!.visibility = View.VISIBLE
        tvSubtitle!!.text = subtitle
    }

    private fun setupTextViews() {
        tvSubtitle = TextView(context)
        tvTitle = TextView(context)

        tvTitle!!.ellipsize = TextUtils.TruncateAt.END
        tvTitle!!.setTextAppearance(
            context,
            R.style.TextAppearance_AppCompat_Widget_ActionBar_Title
        )

        linear = LinearLayout(context)
        linear!!.gravity = Gravity.CENTER
        linear!!.orientation = LinearLayout.VERTICAL
        linear!!.addView(tvTitle)
        linear!!.addView(tvSubtitle)

        tvSubtitle!!.setSingleLine()
        tvSubtitle!!.ellipsize = TextUtils.TruncateAt.END
        tvSubtitle!!.setTextAppearance(
            context,
            R.style.TextAppearance_AppCompat_Widget_ActionBar_Subtitle
        )

        tvSubtitle!!.visibility = View.GONE

        val lp = Toolbar.LayoutParams(
            Toolbar.LayoutParams.WRAP_CONTENT,
            Toolbar.LayoutParams.WRAP_CONTENT
        )
        lp.gravity = Gravity.CENTER
        linear!!.layoutParams = lp

        addView(linear)
    }

}
