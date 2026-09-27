package com.petmorph.ai.overlay

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object OverlayPermissionHelper {
    fun hasPermission(context: Context): Boolean =
        Settings.canDrawOverlays(context)

    fun requestPermission(activity: Activity, requestCode: Int = REQ_CODE) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + activity.packageName)
        )
        activity.startActivityForResult(intent, requestCode)
    }

    fun explainBeforeRequest(): String =
        "PetMorph needs the "Display over other apps" permission so your companion " +
        "can float on top of other apps. It never reads your screen content — it " +
        "only draws the character. You can revoke it anytime in Settings."

    const val REQ_CODE = 1001
}
