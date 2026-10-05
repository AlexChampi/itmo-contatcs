package ru.itmo.contacts

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.StringRes

fun Context.hasContactsPermission(): Boolean =
    checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

fun Context.dial(phoneNumber: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null))
    startActivityOrToast(intent, R.string.error_no_dialer)
}

fun Context.openAppSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null),
    )
    startActivityOrToast(intent, R.string.error_no_settings)
}

private fun Context.startActivityOrToast(intent: Intent, @StringRes errorMessage: Int) {
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
}
