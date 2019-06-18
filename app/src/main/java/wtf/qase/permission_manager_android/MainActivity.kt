package wtf.qase.permission_manager_android

import android.Manifest
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import wtf.qase.permission_manager_library.IPermissionListener
import wtf.qase.permission_manager_library.PermissionManager
import wtf.qase.permission_manager_library.PermissionState

class MainActivity : AppCompatActivity(), IPermissionListener {

    val permissionManager: PermissionManager = PermissionManager(this)

    private val EXAMPLE_REQUEST_CODE = 5002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        val hasGPS = PermissionManager.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION, this)
        if (hasGPS) {
            permissionStatus.text = "GPS access granted"
            permissionStatus.setTextColor(resources.getColor(R.color.green))
        } else {
            permissionStatus.text = "GPS not granted"
            permissionStatus.setTextColor(resources.getColor(R.color.red))
        }
        requestPermissionButton.setOnClickListener {
            permissionManager.registerListener(this)
            permissionManager.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, EXAMPLE_REQUEST_CODE)
        }
        explainButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("I really need that GPS permission!")
            builder.setCancelable(false)
            builder.setPositiveButton("OK, what can I do", DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
                permissionManager.goToAppSettings()
            })
            val dialog = builder.create()
            dialog.show()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onPermissionChange(
        permissions: Array<String>,
        grantResults: Array<PermissionState>,
        requestCode: Int
    ) {
        if (requestCode != EXAMPLE_REQUEST_CODE) {
            return
        }
        for ((i, permission) in permissions.withIndex()) {
            when (grantResults[i]) {
                PermissionState.GRANTED -> {
                    Toast.makeText(
                        this,
                        "Permission granted: $permission",
                        Toast.LENGTH_LONG
                    ).show()
                }
                PermissionState.DECLINED -> {
                    Toast.makeText(
                        this,
                        "Permission declined: $permission",
                        Toast.LENGTH_LONG
                    ).show()
                }
                PermissionState.DECLINED_FOR_EVER -> {
                    Toast.makeText(
                        this,
                        "Permission declined forever, explain why you need it: $permission",
                        Toast.LENGTH_LONG
                    ).show()
                    explainButton.visibility = View.VISIBLE
                }
            }
        }
        permissionManager.unRegisterListener(this)
    }

}
