package com.example.mybluetooth

import android.bluetooth.BluetoothManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.palette.graphics.Palette
import com.example.mybluetooth.databinding.ActivityMainBinding
import com.example.mybluetooth.uitls.BluetoothHelper

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding

    private fun ToastWarining(content:String) =
        Toast.makeText(this,content, Toast.LENGTH_LONG).show()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!::binding.isInitialized){
            binding = ActivityMainBinding.inflate(layoutInflater)
        }

        adapterStatus() // 自适应状态栏

        // 设置导航栏容器
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        binding.navigation.setupWithNavController(navController)

        // 获取蓝牙适配器
        val manager = getSystemService(BluetoothManager::class.java)
        BluetoothHelper.bluetoothAdapter = manager.adapter

        BluetoothHelper.myBluetoothList.clear()

        setContentView(binding.root)
    }

    private fun adapterStatus(){ // 自适应状态栏
        val displayMetrics = resources.displayMetrics
        val colorCount = 5
        val left = 0
        val top = 0
        val right = displayMetrics.widthPixels
        val bottom = getStatusBarHeight()

        // 获取背景颜色
        val typedValue = TypedValue()
        // 获取当前主题的 windowBackground 属性
        theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true)


        // 使用 TypedArray 获取 windowBackground 的颜色值
        val backgroundResourceId = typedValue.resourceId
        val typedArray = obtainStyledAttributes(backgroundResourceId, intArrayOf(android.R.attr.colorBackground))
        val backgroundColor = typedArray.getColor(0, 0)
        typedArray.recycle()

        // 如果颜色值为 0，则表示获取失败，可以使用默认颜色
        val finalBackgroundColor = if (backgroundColor != 0) backgroundColor else Color.WHITE

        // finalBackgroundColor 包含了当前主题的 windowBackground 颜色
        val bitmap = Bitmap.createBitmap(displayMetrics.widthPixels, displayMetrics.heightPixels, Bitmap.Config.ARGB_8888)

        // 使用 Canvas 将颜色值填充到 Bitmap 上
        val canvas = Canvas(bitmap)
        canvas.drawColor(finalBackgroundColor)

        if (finalBackgroundColor == Color.WHITE){
            setLightStatusBar()

        }else{
            // 解析状态栏上的颜色
            Palette
                .from(bitmap)
                .maximumColorCount(colorCount)
                .setRegion(left, top, right, bottom)
                .generate {
                    it?.let { palette ->
                        var mostPopularSwatch: Palette.Swatch? = null
                        for (swatch in palette.swatches) {
                            if (mostPopularSwatch == null
                                || swatch.population > mostPopularSwatch.population) {
                                mostPopularSwatch = swatch
                            }
                        }
                        mostPopularSwatch?.let { swatch ->
                            val luminance = ColorUtils.calculateLuminance(swatch.rgb)
                            if (luminance < 0.5) {
                                setDarkStatusBar()
                            } else {
                                setLightStatusBar()
                            }
                        }
                    }
                }
        }
    }


    private fun setLightStatusBar() { // 状态栏亮色 图标为黑色
        WindowInsetsControllerCompat(window,window.decorView).isAppearanceLightStatusBars = true
    }


    private fun setDarkStatusBar() { // 状态栏黑色 图标白色
        WindowInsetsControllerCompat(window,window.decorView).isAppearanceLightStatusBars = false
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

}