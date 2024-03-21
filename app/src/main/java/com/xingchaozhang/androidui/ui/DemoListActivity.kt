package com.xingchaozhang.androidui.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xingchaozhang.androidui.R
import com.xingchaozhang.androidui.UIEnv
import com.xingchaozhang.androidui.databinding.ActivityDemoListBinding
import com.xingchaozhang.androidui.ui.DemoPageConfig.CURRENT_PACKAGE
import com.xingchaozhang.androidui.ui.DemoPageConfig.PACKAGE_BRAND_B
import com.xingchaozhang.androidui.ui.DemoPageConfig.PACKAGE_BRAND_C
import com.xingchaozhang.androidui.ui.DemoPageConfig.PACKAGE_BRAND_A

/**
 * @description : Demo list page.
 */
class DemoListActivity : BaseThemeActivity() {
    companion object {
        var style = R.style.UITheme
        var currentPackage = PACKAGE_BRAND_A
        const val EXTRA_THEME = "ok_theme"
    }

    private lateinit var viewBinding: ActivityDemoListBinding

    private lateinit var adapter: HomeAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityDemoListBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        initView()
    }

    private fun initView() {
//        setSupportActionBar(viewBinding.toolbar)
//        viewBinding.toolbar.setNavigationOnClickListener {
//            onBackPressed()
//        }

        val allComponents = mutableListOf<Pair<String, String>>()
        allComponents.addAll(DemoPageConfig.commonList)
        adapter = HomeAdapter(this, allComponents)

        viewBinding.demoWidgetRcy.let {
            it.layoutManager = LinearLayoutManager(this)
            val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
            it.addItemDecoration(dividerItemDecoration)
            it.adapter = adapter
        }
        viewBinding.buttonDay.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            startActivity(Intent(this, DemoListActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
        viewBinding.buttonNight.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            startActivity(Intent(this, DemoListActivity::class.java))
            recreate()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        viewBinding.theme.setOnCheckedChangeListener { group, checkedId ->
            adapter.dataList.clear()
            style = when (checkedId) {
                viewBinding.okx.id -> {
                    currentPackage = PACKAGE_BRAND_A
                    adapter.dataList.addAll(DemoPageConfig.brandAList)
                    R.style.UITheme
                }

                viewBinding.lite.id -> {
                    currentPackage = PACKAGE_BRAND_B
                    adapter.dataList.addAll(DemoPageConfig.brandBList)
                    R.style.UITheme
                }

                else -> {
                    currentPackage = PACKAGE_BRAND_C
                    adapter.dataList.addAll(DemoPageConfig.brandCList)
                    R.style.UITheme
                }
            }
            adapter.dataList.addAll(DemoPageConfig.commonList)
            adapter.notifyDataSetChanged()
            setTheme(style)
            UIEnv.setTheme(style)
        }
    }

    private class HomeAdapter(
        var context: Context,
        var dataList: MutableList<Pair<String, String>>
    ) :
        RecyclerView.Adapter<HomeAdapter.MyViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(
                LayoutInflater.from(context).inflate(R.layout.item_main, parent, false)
            )
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.tv.text = dataList[position].first
            if (dataList[position].first.contains("Base")) {
                holder.tv.textSize = 14f
                holder.itemView
                    .setBackgroundColor(ContextCompat.getColor(context, R.color.GoGreen200))
                return
            } else if (dataList[position].first.contains("Business")) {
                holder.tv.textSize = 14f
                holder.itemView
                    .setBackgroundColor(ContextCompat.getColor(context, R.color.RubyRed200))
                return
            } else {
                holder.tv.textSize = 12f
                holder.itemView.background = null
            }
            holder.itemView.setOnClickListener {
                when (dataList[position].first) {
                    "TestActivity (TestActivity)" -> {
                        context.startActivity(Intent(context, TestActivity::class.java).apply {
                            putExtra(EXTRA_THEME, style)
                        })
                    }

                    else -> {
                        context.startActivity(Intent(context, ComponentActivity::class.java).apply {
                            putExtra("page", dataList[position])
                            putExtra(CURRENT_PACKAGE, currentPackage)
                            putExtra(EXTRA_THEME, style)
                        })
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var tv: TextView = view.findViewById(R.id.item_name)
        }
    }
}