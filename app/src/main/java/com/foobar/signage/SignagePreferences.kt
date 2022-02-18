package com.foobar.signage
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat;

class SignagePreferences : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}