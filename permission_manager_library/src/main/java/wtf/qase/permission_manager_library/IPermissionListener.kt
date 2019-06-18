package wtf.qase.permission_manager_library

interface IPermissionListener {

    fun onPermissionChange(permissions: Array<String>, grantResults: Array<PermissionState>, requestCode: Int)
}