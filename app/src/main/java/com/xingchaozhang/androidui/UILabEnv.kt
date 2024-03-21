package com.xingchaozhang.androidui

import android.app.Application
import android.content.Context
import androidx.annotation.StyleRes

/**
 * UI environment config.
 */
object UIEnv {

    private lateinit var application: Application

    @StyleRes
    private var themeRes: Int = R.style.UITheme

    /**
     * Initialize environment.
     */
    @JvmStatic
    @JvmOverloads
    fun init(application: Application, @StyleRes theme: Int = R.style.UITheme) {
        UIEnv.application = application
        application.setTheme(application.applicationInfo.theme)
    }

    /**
     * Set application theme dynamic.
     */
    fun setTheme(@StyleRes theme: Int) {
        application.setTheme(theme)
        themeRes = theme
    }

    /**
     * Application context.
     */
    @JvmStatic
    fun getApp(): Application = application

    /**
     * Context theme wrapper.
     */
    @JvmStatic
    @Deprecated("Use getApp() instead." +
            "If you want to use different appearance from default application, " +
            "you could call ThemeContextCache.getThemeContext() to get other brand context.")
    fun getThemeContext(): Context {
        return application
    }

    @JvmStatic
    @StyleRes
    fun getThemeId(): Int {
        return themeRes
    }
    /**
     * Set lottie whether to use software render mode.
     */
    var lottieUseSoftware = false
}