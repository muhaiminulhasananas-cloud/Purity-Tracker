package com.example

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AntiUninstallAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        val msg = if (LanguageStore.currentLanguage.value == "bn") {
            "ডিভাইস অ্যাডমিন অ্যান্টি-আনইনস্টল সুরক্ষা সক্রিয় হয়েছে!"
        } else {
            "Device Admin Anti-Uninstall Protection Activated!"
        }
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return if (LanguageStore.currentLanguage.value == "bn") {
            "⚠️ সতর্কতা: এই সুরক্ষা নিষ্ক্রিয় করলে Purity Tracker আনইনস্টল হয়ে যাবে এবং আপনার সমস্ত স্ট্রিক মুছে যাবে!"
        } else {
            "⚠️ Warning: Deactivating this will allow Purity Tracker to be uninstalled, resetting your streak and progress!"
        }
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
    }
}
