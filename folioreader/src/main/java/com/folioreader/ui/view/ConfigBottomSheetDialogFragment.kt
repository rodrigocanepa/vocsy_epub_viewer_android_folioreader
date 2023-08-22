package com.folioreader.ui.view

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.folioreader.Config
import com.folioreader.R
import com.folioreader.model.event.ReloadDataEvent
import com.folioreader.ui.activity.FolioActivity
import com.folioreader.ui.activity.FolioActivityCallback
import com.folioreader.ui.adapter.FontAdapter
import com.folioreader.ui.fragment.MediaControllerFragment
import com.folioreader.util.AppUtil
import com.folioreader.util.UiUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.view_config.*
import org.greenrobot.eventbus.EventBus

/**
 * Created by mobisys2 on 11/16/2016.
 */
class ConfigBottomSheetDialogFragment : BottomSheetDialogFragment() {

    companion object {
        const val FADE_DAY_NIGHT_MODE = 500

        @JvmField
        val LOG_TAG: String = ConfigBottomSheetDialogFragment::class.java.simpleName
    }

    private lateinit var config: Config
    private var isNightMode = false
    private lateinit var activityCallback: FolioActivityCallback


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.view_config, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (activity is FolioActivity)
            activityCallback = activity as FolioActivity

        view.viewTreeObserver.addOnGlobalLayoutListener {
            val dialog = dialog as BottomSheetDialog
            val bottomSheet =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?

            bottomSheet?.setBackgroundResource(R.drawable.rounded_corner)

            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.peekHeight = 0
        }

        config = AppUtil.getSavedConfig(activity)!!
        initViews()
    }

    override fun onDestroy() {
        super.onDestroy()
        view?.viewTreeObserver?.addOnGlobalLayoutListener(null)
    }

    private fun initViews() {
        inflateView()
        configFonts()
        view_config_font_size_seek_bar.progress = config.fontSize
        configSeekBar()
        selectFont(config.font, false)
        isNightMode = config.isNightMode
        if (isNightMode) {
            container.setBackgroundResource(R.drawable.rounded_corner_dark)
            viewVertical1.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
            view3.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
            view4.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
            view5.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
            viewVertical2.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
        } else {
            container.setBackgroundResource(R.drawable.rounded_corner)
            viewVertical1.setBackgroundColor(Color.parseColor("#FF442E83"))
            view3.setBackgroundColor(Color.parseColor("#FF442E83"))
            view4.setBackgroundColor(Color.parseColor("#FF442E83"))
            view5.setBackgroundColor(Color.parseColor("#FF442E83"))
            viewVertical2.setBackgroundColor(Color.parseColor("#FF442E83"))
        }

        if (isNightMode) {
            view_config_ib_day_mode.isSelected = false
            view_config_ib_night_mode.isSelected = true
            UiUtil.setColorIntToDrawable(
                Color.parseColor("#FFFFFFFF"),
                view_config_ib_night_mode.drawable
            )
            UiUtil.setColorResToDrawable(R.color.white, view_config_ib_day_mode.drawable)
        } else {
            view_config_ib_day_mode.isSelected = true
            view_config_ib_night_mode.isSelected = false
            UiUtil.setColorIntToDrawable(
                Color.parseColor("#FF442E83"),
                view_config_ib_day_mode!!.drawable
            )
            UiUtil.setColorResToDrawable(R.color.default_theme_accent_color, view_config_ib_night_mode.drawable)
        }
    }

    private fun inflateView() {

        if (config.allowedDirection != Config.AllowedDirection.VERTICAL_AND_HORIZONTAL) {
            view5.visibility = View.GONE
            buttonVertical.visibility = View.GONE
            buttonHorizontal.visibility = View.GONE
        }

        view_config_ib_day_mode.setOnClickListener {
            isNightMode = true
            toggleBlackTheme()
            view_config_ib_day_mode.isSelected = true
            view_config_ib_night_mode.isSelected = false
            setToolBarColor()
            setAudioPlayerBackground()
            UiUtil.setColorResToDrawable(R.color.app_gray, view_config_ib_night_mode.drawable)
            UiUtil.setColorIntToDrawable(config.currentThemeColor, view_config_ib_day_mode.drawable)
            dialog?.hide()
        }

        view_config_ib_night_mode.setOnClickListener {
            isNightMode = false
            toggleBlackTheme()
            view_config_ib_day_mode.isSelected = false
            view_config_ib_night_mode.isSelected = true
            UiUtil.setColorResToDrawable(R.color.app_gray, view_config_ib_day_mode.drawable)
            UiUtil.setColorIntToDrawable(
                config.currentThemeColor,
                view_config_ib_night_mode.drawable
            )
            setToolBarColor()
            setAudioPlayerBackground()
            dialog?.hide()
        }

        if (activityCallback.direction == Config.Direction.HORIZONTAL) {
            buttonHorizontal.isSelected = true
        } else if (activityCallback.direction == Config.Direction.VERTICAL) {
            buttonVertical.isSelected = true
        }

        buttonVertical.setOnClickListener {
            config = AppUtil.getSavedConfig(context)!!
            config.direction = Config.Direction.VERTICAL
            AppUtil.saveConfig(context, config)
            activityCallback.onDirectionChange(Config.Direction.VERTICAL)
            buttonHorizontal.isSelected = false
            buttonVertical.isSelected = true
            dialog?.hide()
        }

        buttonHorizontal.setOnClickListener {
            config = AppUtil.getSavedConfig(context)!!
            config.direction = Config.Direction.HORIZONTAL
            AppUtil.saveConfig(context, config)
            activityCallback.onDirectionChange(Config.Direction.HORIZONTAL)
            buttonHorizontal.isSelected = true
            buttonVertical.isSelected = false
            dialog?.hide()
        }
    }

    private fun configFonts() {

        val colorStateList = UiUtil.getColorList(
            Color.parseColor("#FF442E83"),
            ContextCompat.getColor(context!!, R.color.default_theme_accent_color_unselected)
        )

        if(config.isNightMode){
            UiUtil.setColorResToDrawable(R.color.white, buttonVertical.drawable)
            UiUtil.setColorResToDrawable(R.color.white, buttonHorizontal.drawable)

        } else{
            UiUtil.setColorResToDrawable(R.color.default_theme_accent_color, buttonHorizontal.drawable)
            UiUtil.setColorResToDrawable(R.color.default_theme_accent_color, buttonVertical.drawable)

        }
        //buttonVertical.setTextColor(colorStateList)
        //
        // buttonHorizontal.setTextColor(colorStateList)

        val adapter = FontAdapter(config, context!!)

        view_config_font_spinner.adapter = adapter

        view_config_font_spinner.background.setColorFilter(
            if (config.isNightMode) {
                R.color.night_default_font_color
            } else {
                R.color.day_default_font_color
            },
            PorterDuff.Mode.SRC_ATOP
        )

        val fontIndex = adapter.fontKeyList.indexOf(config.font)
        view_config_font_spinner.setSelection(if (fontIndex < 0) 0 else fontIndex)

        view_config_font_spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectFont(adapter.fontKeyList[position], true)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
    }

    private fun selectFont(selectedFont: String, isReloadNeeded: Boolean) {
        // parse font from name
        config.font = selectedFont

        if (isAdded && isReloadNeeded) {
            AppUtil.saveConfig(activity, config)
            EventBus.getDefault().post(ReloadDataEvent())
        }
    }

    private fun toggleBlackTheme() {

        val day = ContextCompat.getColor(context!!, R.color.white)
        val night = ContextCompat.getColor(context!!, R.color.night)

        val colorAnimation = ValueAnimator.ofObject(
            ArgbEvaluator(),
            if (isNightMode) night else day, if (isNightMode) day else night
        )
        colorAnimation.duration = FADE_DAY_NIGHT_MODE.toLong()

        colorAnimation.addUpdateListener { animator ->
            val value = animator.animatedValue as Int
            container.setBackgroundColor(value)
        }

        colorAnimation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}

            override fun onAnimationEnd(animator: Animator) {
                isNightMode = !isNightMode
                config.isNightMode = isNightMode
                AppUtil.saveConfig(activity, config)
                EventBus.getDefault().post(ReloadDataEvent())
            }

            override fun onAnimationCancel(animator: Animator) {}

            override fun onAnimationRepeat(animator: Animator) {}
        })

        colorAnimation.duration = FADE_DAY_NIGHT_MODE.toLong()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            val attrs = intArrayOf(android.R.attr.navigationBarColor)
            val typedArray = activity?.theme?.obtainStyledAttributes(attrs)
            val defaultNavigationBarColor = typedArray?.getColor(
                0,
                ContextCompat.getColor(context!!, R.color.white)
            )
            val black = ContextCompat.getColor(context!!, R.color.black)

            val navigationColorAnim = ValueAnimator.ofObject(
                ArgbEvaluator(),
                if (isNightMode) black else defaultNavigationBarColor,
                if (isNightMode) defaultNavigationBarColor else black
            )

            navigationColorAnim.addUpdateListener { valueAnimator ->
                val value = valueAnimator.animatedValue as Int
                activity?.window?.navigationBarColor = value
            }

            navigationColorAnim.duration = FADE_DAY_NIGHT_MODE.toLong()
            navigationColorAnim.start()
        }

        colorAnimation.start()
    }

    private fun configSeekBar() {
        val thumbDrawable = ContextCompat.getDrawable(activity!!, R.drawable.seekbar_thumb)
        if(config.isNightMode){
            UiUtil.setColorIntToDrawable(Color.parseColor("#FFFFFFFF"), thumbDrawable)
            UiUtil.setColorResToDrawable(
                R.color.white,
                view_config_font_size_seek_bar.progressDrawable
            )
        } else{
            UiUtil.setColorIntToDrawable(Color.parseColor("#FF442E83"), thumbDrawable)
            UiUtil.setColorResToDrawable(
                R.color.default_theme_accent_color,
                view_config_font_size_seek_bar.progressDrawable
            )
        }

        view_config_font_size_seek_bar.thumb = thumbDrawable

        view_config_font_size_seek_bar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                config.fontSize = progress
                AppUtil.saveConfig(activity, config)
                EventBus.getDefault().post(ReloadDataEvent())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun setToolBarColor() {
        if (isNightMode) {
            activityCallback.setDayMode()
        } else {
            activityCallback.setNightMode()
        }
    }

    private fun setAudioPlayerBackground() {

        var mediaControllerFragment: Fragment? =
            fragmentManager?.findFragmentByTag(MediaControllerFragment.LOG_TAG)
                ?: return
        mediaControllerFragment = mediaControllerFragment as MediaControllerFragment
        if (isNightMode) {
            mediaControllerFragment.setDayMode()
        } else {
            mediaControllerFragment.setNightMode()
        }
    }
}
