# Signage Sandbox
Very simple fullscreen app with a WebView.

The WebView's URL is set via resource strings
* `mersive_signage_url`
* `fwi_signage_url`
* plus many more in values/strings.xml

``` java
// in FullscreenActivity.onCreate() change the R.string
val signageUrl = resources.getString(R.string.mersive_signage_url)
```

To start (after the APK is installed) or stop with adb
``` console
> adb shell am start -n com.foobar.signage/com.foobar.signage.FullscreenActivity

> adb shell am force-stop com.foobar.signage
```

