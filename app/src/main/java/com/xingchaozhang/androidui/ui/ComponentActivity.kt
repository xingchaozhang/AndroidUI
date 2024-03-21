package com.xingchaozhang.androidui.ui

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.xingchaozhang.androidui.R
import com.xingchaozhang.androidui.ui.DemoPageConfig.CURRENT_PACKAGE
import com.xingchaozhang.androidui.ui.DemoPageConfig.PACKAGE_DEFAULT
import com.xingchaozhang.androidui.ui.DemoPageConfig.PACKAGE_BRAND_B
import com.xingchaozhang.androidui.ui.DemoPageConfig.PACKAGE_BRAND_C
import com.xingchaozhang.androidui.ui.DemoPageConfig.PACKAGE_BRAND_A
import com.xingchaozhang.androidui.ui.DemoPageConfig.commonList

/**
 * @description : 承载所有component的activity。
 */
class ComponentActivity : BaseThemeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_component)
        setTheme(intent.getIntExtra(DemoListActivity.EXTRA_THEME, R.style.UITheme))
        initView()
    }

    private fun initView() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
//        setSupportActionBar(toolbar)
//        toolbar.setNavigationOnClickListener {
//            onBackPressed()
//        }
        val currentPackage = intent.getStringExtra(CURRENT_PACKAGE)
        intent.getSerializableExtra("page")?.let {
            val data = it as Pair<String, String>
            toolbar.title = data.first
            var className = getRealClassName(data, commonList, PACKAGE_BRAND_A)
            if (TextUtils.equals(PACKAGE_BRAND_B, currentPackage)) {
                className = getRealClassName(data, commonList, PACKAGE_BRAND_A)
            } else if (TextUtils.equals(PACKAGE_BRAND_C, currentPackage)) {
                className = getRealClassName(data, commonList, PACKAGE_BRAND_C)
            }
            try {
                val fragment: Fragment =
                    Class.forName(className).newInstance() as Fragment
                supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment)
                    .commit()
            } catch (e: Exception) {
                Log.e("ComponentActivity", "initView: $e")
            }
        }
    }

    /**
     * 获取真正的完整路径class名称
     */
    private fun getRealClassName(
        data: Pair<String, String>,
        list: MutableList<Pair<String, String>>,
        pkgName: String
    ): String =
        if (containsFragment(data.second, list)) {
            pkgName + data.second
        } else {
            PACKAGE_DEFAULT + data.second
        }

    /**
     * 用来区分是不是公共组件，也就是不需要针对okx，okxlite，okcoin分别配置属性的，这种Fragment直接放到根目录下。
     * 通过遍历整个列表然后来确认是否是公共组件
     */
    private fun containsFragment(name: String, list: MutableList<Pair<String, String>>): Boolean {
        list.forEach {
            if (TextUtils.equals(name, it.second)) {
                return true
            }
        }
        return false
    }
}