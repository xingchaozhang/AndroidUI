package com.xingchaozhang.androidui.ui

/**
 * @date : 2023/10/11 15:45
 * @author ： zhangxingchao
 * @description : This page is use to control demo list.
 */
object DemoPageConfig {

    val commonList = mutableListOf<Pair<String, String>>()
    val brandAList = mutableListOf<Pair<String, String>>()
    val brandBList = mutableListOf<Pair<String, String>>()
    val brandCList = mutableListOf<Pair<String, String>>()

    const val CURRENT_PACKAGE = "current_package"
    const val PACKAGE_DEFAULT = "com.xingchaozhang.androidui.ui."
    const val PACKAGE_BRAND_A = "com.xingchaozhang.androidui.ui.fragments."
    const val PACKAGE_BRAND_B = "com.xingchaozhang.androidui.ui.fragments."
    const val PACKAGE_BRAND_C = "com.xingchaozhang.androidui.ui.fragments."

    init {
        setComponents()
    }

    private fun setComponents() {
        commonList.apply {
            add("LocalizationInputEditText (LocalizationInputEditTextFragment)" to "LocalizationInputEditTextFragment")
            add("Input (InputFragment)" to "InputFragment")
        }
    }
}