package pharmrus.org.pharmrus.utils

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import pharmrus.org.pharmrus.R

const val MY_PERMISSIONS_REQUEST_CALL_PHONE = 1001

object CallUtility {
    fun callPharmacyListener(view: View, parent: Activity) {
        if (ContextCompat.checkSelfPermission(parent, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(parent, Manifest.permission.CALL_PHONE)) {
                Snackbar.make(view, "Need info", Snackbar.LENGTH_LONG).setAction("Action", null).show()
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(parent, arrayOf(Manifest.permission.CALL_PHONE), MY_PERMISSIONS_REQUEST_CALL_PHONE)
            }
        } else {
            callCallActivity(parent)
        }
    }

    private fun callCallActivity (parent: Activity) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:" + parent.resources.getString(R.string.pharmacy_phone))

        // Make call
        try {
            parent.startActivity(intent)
        } catch (ex:SecurityException) {
            Log.e("error", "When trying to call", ex)
        }
    }

    fun processPermissionCallRequest (parent: Activity, grantResults: IntArray) {
        // If request is cancelled, the result arrays are empty.
        if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            callCallActivity(parent)
        } else {
            Log.w("warning", "Permission denied for make calls")
        }
    }
}