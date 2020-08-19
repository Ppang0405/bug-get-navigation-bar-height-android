package com.example.android

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private var cached = false
    private var hasImmersive = false

    // bug of https://stackoverflow.com/questions/46126521/android-navigation-bar-height-react-native
    // after that, has bug of https://www.npmjs.com/package/react-native-extra-dimensions-android

    // ticket bug: https://wrethink.atlassian.net/browse/AIMW-1833

    // so I check android native to find problem, but can not see consistency method to detect navigation show or hide, and get height of it, different devices return different result in each method, so confusing
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        makeFullScreen()

        setContentView(R.layout.activity_main)


        registerListenerUIvisiblityChange()
        setUpText()
        setTextStatusBarHeight()
        setTextCurrentNavigationBarHeight()
        setTextNavigationBarHeight()
    }

    private fun makeFullScreen() {
        // Remove Title
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        // Make Fullscreen
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Hide the toolbar
        supportActionBar?.hide()
    }

    fun registerListenerUIvisiblityChange() {
        val text_info = findViewById<TextView>(R.id.text_info)

        val navigationGestureEnabled = navigationGestureEnabled(this)

        val hasNavBar = hasNavBar(resources)

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            // Note that system bars will only be "visible" if none of the
            // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                // TODO: The system bars are visible. Make any desired
                // adjustments to your UI, such as showing the action bar or
                // other navigational controls.
                Log.d("The system bars are visible", "NAVIGATION_BAR")

                text_info.text = "The system bars are visible $navigationGestureEnabled $hasNavBar"

            } else {
                // TODO: The system bars are NOT visible. Make any desired
                // adjustments to your UI, such as hiding the action bar or
                // other navigational controls.
                Log.d("The system bars are not visible", "NAVIGATION_BAR")

                text_info.text = "The system bars are not visible $navigationGestureEnabled $hasNavBar"
            }
        }
    }

    fun navigationGestureEnabled(context: Context): Boolean {
        val checkNavigationGestureEnabled = Settings.Global.getInt(
                context.getContentResolver(),
                getDeviceInfo(),
                0
        )
        return checkNavigationGestureEnabled != 0
    }

    // https://www.jianshu.com/p/b20047fdea8a
    fun getDeviceInfo(): String {
        val brand = Build.BRAND
        if (TextUtils.isEmpty(brand)) return "navigation_is_min"
        if (brand.contentEquals("HUAWEI")) {
            return "navigationbar_is_min";
        } else if (brand.contentEquals("XIAOMI")) {
            return "force_fsg_nav_bar";
        } else if (brand.contentEquals("VIVO")) {
            return "navigation_gesture_on";
        } else if (brand.contentEquals("OPPO")) {
            return "navigation_gesture_on";
        } else {
            return "navigationbar_is_min";
        }
    }

    fun hasNavBar2(): Boolean {
        val realSize = Point()
        val screenSize = Point()
        var hasNavBar = false
        val metrics = DisplayMetrics()
        this.windowManager.defaultDisplay.getRealMetrics(metrics)
        realSize.x = metrics.widthPixels
        realSize.y = metrics.heightPixels
        windowManager.defaultDisplay.getSize(screenSize)
        if (realSize.y !== screenSize.y) {
            val difference: Int = realSize.y - screenSize.y
            var navBarHeight = 0
            val resources = resources
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            if (resourceId > 0) {
                navBarHeight = resources.getDimensionPixelSize(resourceId)
            }
            if (navBarHeight != 0) {
                if (difference == navBarHeight) {
                    hasNavBar = true
                }
            }
        }
        return hasNavBar
    }

    fun hasSoftKeys(): Boolean {
        val d = windowManager.defaultDisplay
        val realDisplayMetrics = DisplayMetrics()
        d.getRealMetrics(realDisplayMetrics)
        val realHeight = realDisplayMetrics.heightPixels
        val realWidth = realDisplayMetrics.widthPixels
        val displayMetrics = DisplayMetrics()
        d.getMetrics(displayMetrics)
        val displayHeight = displayMetrics.heightPixels
        val displayWidth = displayMetrics.widthPixels
        return realWidth - displayWidth > 0 || realHeight - displayHeight > 0
    }

    fun isNavigationBarAvailable(): Boolean {
        val hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
        val hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME)
        return !(hasBackKey && hasHomeKey)
    }

    fun setUpText() {
        val status_bar_height_is = findViewById<TextView>(R.id.status_bar_height_is)
//        status_bar_height_is.text = "status_bar_height_is: 10"

        val navigation_bar_height_is = findViewById<TextView>(R.id.navigation_bar_height_is)
//        navigation_bar_height_is.text = "navigation_bar_height_is: 48"


        // get real height, width
        val metrics = resources.displayMetrics
        windowManager.defaultDisplay.getMetrics(metrics)

        val realHeightText = findViewById<TextView>(R.id.real_height)
        val realWidthText = findViewById<TextView>(R.id.real_width)

        val realHeight = getRealHeight(metrics)
        val realWidth = getRealWidth(metrics)


        realHeightText.text = "real_height: $realHeight"
        realWidthText.text = "real_width: $realWidth"

        // show text navigation bar hide or show

        val navigation_bar_shown = findViewById<TextView>(R.id.navigation_bar_shown)

        val hasNavBar = hasNavBar(resources)
        navigation_bar_shown.text = "navigation_bar_shown: $hasNavBar"

        // is_navigation_bar_avaiable
        val is_navigation_avaiable = findViewById<TextView>(R.id.is_navigation_avaiable)

        val isNavAvaiable = isNavigationBarAvailable()
        is_navigation_avaiable.text = "is_navigation_avaiable: $isNavAvaiable"

        // has_soft_key
        val has_soft_key = findViewById<TextView>(R.id.has_soft_key)
        val hasKeySoft = hasSoftKeys()
        has_soft_key.text = "has_soft_key: $hasKeySoft"


        // has nav bar 2
        val has_nav_bar_2 = findViewById<TextView>(R.id.has_nav_bar_2)
        val hasNavBar2 = hasNavBar2()
        has_nav_bar_2.text = "has_nav_bar_2: $hasNavBar2"

        val has_Immersive = findViewById<TextView>(R.id.hasImmersive)
        val hasImmersive = hasImmersive()
        has_Immersive.text ="has_Immersive: $hasImmersive"

    }

    private fun convertToRealPoint(value: Int): Float {
        val metrics = resources.displayMetrics
        windowManager.defaultDisplay.getMetrics(metrics)
        return value / metrics.density
    }

    private fun getRealHeight(metrics: DisplayMetrics): Float {
        return convertToRealPoint(metrics.heightPixels)
    }

    private fun getRealWidth(metrics: DisplayMetrics): Float {
        return convertToRealPoint(metrics.widthPixels)
    }

    fun setTextStatusBarHeight(): Int {
        val heightResId = resources.getIdentifier("status_bar_height", "dimen", "android")
        var statusBarHeight = 0

        statusBarHeight = convertToRealPoint(resources.getDimensionPixelSize(heightResId)).toInt()

        val status_bar_height_is = findViewById<TextView>(R.id.status_bar_height_is)
        status_bar_height_is.text = "status_bar_height_is: $statusBarHeight"

        return statusBarHeight
    }

    fun setTextCurrentNavigationBarHeight() {

    }

    fun getCurrentNavigationBarHeight() {

    }

    fun setTextNavigationBarHeight() {
        val heightResId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        val navigationBarHeight = if (heightResId > 0)
            resources.getDimensionPixelSize(heightResId)
            else 0

        val navigationVarHeightConverted = convertToRealPoint(navigationBarHeight)

        val navigation_bar_height_is = findViewById<TextView>(R.id.navigation_bar_height_is)
        navigation_bar_height_is.text = "navigation_bar_height_is: $navigationVarHeightConverted"

    }

    fun isNavigationBarShown() {

    }

    fun hasNavBar(resources: Resources): Boolean {
        val id: Int = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return !(id > 0 && resources.getBoolean(id))
    }

    // https://github.com/lequanghuylc/react-native-detect-navbar-android
    // it still not work
    private fun hasImmersive(): Boolean {
        if (!cached) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                hasImmersive = false
                cached = true
                return false
            }
            val d = (getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay
            val realDisplayMetrics = DisplayMetrics()
            d.getRealMetrics(realDisplayMetrics)
            val realHeight = realDisplayMetrics.heightPixels
            val realWidth = realDisplayMetrics.widthPixels
            val displayMetrics = DisplayMetrics()
            d.getMetrics(displayMetrics)
            val displayHeight = displayMetrics.heightPixels
            val displayWidth = displayMetrics.widthPixels
            hasImmersive = realWidth > displayWidth || realHeight > displayHeight
            cached = true
        }
        return hasImmersive
    }
}