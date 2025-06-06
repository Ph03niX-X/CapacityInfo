package com.ph03nix_x.capacityinfo.views

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import com.google.android.material.appbar.MaterialToolbar
import com.ph03nix_x.capacityinfo.R

class CenteredToolbar : MaterialToolbar {

    private var tvTitle: AppCompatTextView? = null
    private var tvSubtitle: AppCompatTextView? = null
    private var linear: LinearLayoutCompat? = null

    constructor(context: Context) : super(context) { setupTextViews() }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) { setupTextViews() }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs,
        defStyleAttr) { setupTextViews() }

    override fun getTitle(): CharSequence = tvTitle?.text.toString()

    override fun setTitle(@StringRes resId: Int) { title = resources.getString(resId) }

    override fun setTitle(title: CharSequence) { tvTitle?.text = title }

    override fun getSubtitle() = tvSubtitle?.text.toString()

    override fun setSubtitle(resId: Int) { subtitle = resources.getString(resId) }

    override fun setSubtitle(subtitle: CharSequence) {
        tvSubtitle?.apply {
            isVisible = true
            text = subtitle
        }
    }

    private fun setupTextViews() {
        tvSubtitle = AppCompatTextView(context)
        tvTitle = AppCompatTextView(context)
        tvTitle?.apply {
            ellipsize = TextUtils.TruncateAt.END
            setTextAppearance(R.style.TitleTheme)
            gravity = Gravity.CENTER
        }
        linear = LinearLayoutCompat(context)
        linear?.apply {
            gravity = Gravity.CENTER
            orientation = LinearLayoutCompat.VERTICAL
            addView(tvTitle)
            addView(tvSubtitle) 
        }
        val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        lp.gravity = Gravity.CENTER
        linear?.layoutParams = lp
        tvSubtitle?.apply {
            setSingleLine()
            ellipsize = TextUtils.TruncateAt.END
            setTextAppearance(R.style.SubtitleTheme)
            isVisible = false
        }
        addView(linear)
    }
}