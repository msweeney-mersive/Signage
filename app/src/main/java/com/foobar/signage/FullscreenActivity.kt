package com.foobar.signage

import android.annotation.SuppressLint
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.foobar.signage.databinding.ActivityFullscreenBinding

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity() {
    private val tag = this.javaClass.simpleName

    private lateinit var binding: ActivityFullscreenBinding
    private lateinit var fullscreenContent: WebView
    @Suppress("DEPRECATION")
    private val hideHandler = Handler()

    @SuppressLint("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar
        if (Build.VERSION.SDK_INT >= 30) {
            fullscreenContent.windowInsetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            fullscreenContent.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
    }
    private var isFullscreen: Boolean = false

    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val delayHideTouchListener = View.OnTouchListener { view, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS)
            }
            MotionEvent.ACTION_UP -> view.performClick()
            else -> {
            }
        }
        false
    }

    @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFullscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isFullscreen = true

        // Set up the user interaction to manually show or hide the system UI.
        fullscreenContent = binding.signageWv
        fullscreenContent.setOnClickListener { toggle() }

        binding.signageWv.settings.javaScriptEnabled = true

        // other settings used in Solstice DS
        @Suppress("DEPRECATION", "DEPRECATION")
        binding.signageWv.settings.setAppCacheEnabled(false)
        binding.signageWv.settings.cacheMode = WebSettings.LOAD_DEFAULT
        binding.signageWv.settings.allowFileAccess = false
        binding.signageWv.settings.useWideViewPort = true
        binding.signageWv.settings.loadWithOverviewMode = true
        binding.signageWv.settings.setSupportZoom( false )
        binding.signageWv.settings.domStorageEnabled = true
        binding.signageWv.settings.mediaPlaybackRequiresUserGesture = false

        binding.signageWv.setInitialScale(0) // Default scale

        CookieManager.getInstance().setAcceptThirdPartyCookies( binding.signageWv, true)

        val OSversion = Build.VERSION.RELEASE
        val userAgentWithSolstice: String = (binding.signageWv.settings.userAgentString
            .replace("Linux; ", "") // strip OS name
            .replace("Android $OSversion; ", "") // strip OS version
            .replace("; wv", "") // webview identifier
            .replace("Mobile ", "") // mobile identifier
                + " Solstice Pod " // identify as a pod
                + "5.5") // with our current version

        binding.signageWv.settings.userAgentString = userAgentWithSolstice

        /* other settings that may or may not be worth looking at */
        //binding.signageWv.settings.setDatabaseEnabled(false);
        //binding.signageWv.settings.setDomStorageEnabled(false);
        //binding.signageWv.settings.setGeolocationEnabled(false);
        //binding.signageWv.settings.setSaveFormData(false);


        /*
        Until we add a UI with a dropdown to select URLS change the string here and rebuild
         */
        val signageUrl = resources.getString(R.string.mersive_signage_url)
        binding.signageWv.loadUrl(signageUrl)
        Log.d(tag, "onCreate(), WebView loading URL: \n\t${signageUrl}")

        binding.signageWv.setOnTouchListener(delayHideTouchListener)

        binding.signageWv.webViewClient = object : WebViewClient() {

            @SuppressLint("WebViewClientOnReceivedSslError")
            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler,
                error: SslError?
            ) {
                Log.w(tag, "onReceivedSslError(), error: $error?")
                handler.proceed()
            }

            override fun onReceivedHttpError(
                view: WebView?, request: WebResourceRequest, errorResponse: WebResourceResponse
            ) {
                if (view != null) {
                    // Run url through decoding twice to get rid of all encoding
                    val urlDecode = Uri.decode(Uri.decode(request.url.toString()))
                    val err = String.format(
                        "Digital Signage loading url  failed, error code: %d, error description: %s, url: %s",
                        errorResponse.statusCode,
                        errorResponse.reasonPhrase,
                        urlDecode
                    )
                    Log.w(tag, "onReceivedHttpError(), error: $err?")
                }
                super.onReceivedHttpError(view, request, errorResponse)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                if (view != null) {
                    // Run url through decoding twice to get rid of all encoding
                    val urlDecode = Uri.decode(Uri.decode(request.url.toString()))
                    val err = String.format(
                        "Digital Signage loading url  failed, error code: %d, error description: %s, url: %s",
                        error.errorCode,
                        error.description,
                        urlDecode
                    )
                    Log.w(tag, "onReceivedError(), error: $err?")
                }
                super.onReceivedError(view, request, error)
            }

            @Deprecated("Deprecated in Java")
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                if (view != null) {
                    // Run url through decoding twice to get rid of all encoding
                    val urlDecode = Uri.decode(Uri.decode(failingUrl))
                    val err = String.format(
                        "Digital Signage loading url failed, error code: %d, error description: %s, url: %s",
                        errorCode,
                        description,
                        urlDecode
                    )
                    Log.w(tag, "onReceivedError(), error: $err?")
                }
                super.onReceivedError(view, errorCode, description, failingUrl)
            }
        }



/*        supportFragmentManager
            .beginTransaction()
            .replace(R.id.signage_wv, SignagePreferences())
            .commit()*/
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun toggle() {
        if (isFullscreen) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        isFullscreen = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        if (Build.VERSION.SDK_INT >= 30) {
            fullscreenContent.windowInsetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            fullscreenContent.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }
        isFullscreen = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }
}