package wtf.qase.permission_manager_library

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager(private val activity: Activity) {

    private val permissionListeners = hashSetOf<IPermissionListener>()

    private val DEFAULT_REQUEST_CODE = 69

    fun registerListener(iPermissionListener: IPermissionListener) {
        permissionListeners.add(iPermissionListener)
    }

    fun unRegisterListener(iPermissionListener: IPermissionListener) {
        permissionListeners.remove(iPermissionListener)
    }

    fun requestPermission(permission: String, requestCode: Int = DEFAULT_REQUEST_CODE) {
        ActivityCompat.requestPermissions(activity,
                arrayOf(permission),
                requestCode)
    }

    fun requestPermissions(permissions: Array<String>, requestCode: Int = DEFAULT_REQUEST_CODE) {
        ActivityCompat.requestPermissions(activity,
                permissions,
                requestCode)
    }

    //You need to override MainActivity::onRequestPermissionsResult and call this inside your overridden method
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        val permissionStates = mutableListOf<PermissionState>()
        for ((index, permission) in permissions.withIndex()) {
            val permissionState =
                    if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                        PermissionState.GRANTED
                    } else {
                        // If app requests permission that does not exist on android 6, it will result in PackageManager.PERMISSION_DENIED
                        // and also ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) crashes the app, which we want to prevent
                        val shouldShowRequestPermissionRationale = try {
                            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
                        } catch (e: IllegalArgumentException) {
                            true
                        }
                        if (!shouldShowRequestPermissionRationale) {
                            PermissionState.DECLINED_FOR_EVER
                        } else {
                            PermissionState.DECLINED
                        }
                    }
            permissionStates.add(permissionState)
        }
        informListeners(permissions, permissionStates.toTypedArray(), requestCode)
    }

    @RequiresApi(Build.VERSION_CODES.GINGERBREAD)
    fun goToAppSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivity(intent)
    }

    private fun informListeners(permissions: Array<String>, permissionStates: Array<PermissionState>, requestCode: Int) {
        val copy = ArrayList(permissionListeners)
        for (permissionListener in copy) {
            permissionListener.onPermissionChange(permissions, permissionStates, requestCode)
        }
    }

    companion object {
        fun hasPermission(permission: String, context: Context, minRequiredApiLvl: Int? = null): Boolean {
            if (minRequiredApiLvl != null && Build.VERSION.SDK_INT < minRequiredApiLvl) {
                return true
            }
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }

        fun hasPermissions(permissions: Array<String>, context: Context, minRequiredApiLvl: Int? = null): Boolean {
            return permissions.all {
                hasPermission(it, context, minRequiredApiLvl)
            }
        }
    }
}