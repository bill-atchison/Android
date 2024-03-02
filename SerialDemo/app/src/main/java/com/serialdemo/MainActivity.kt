package com.serialdemo

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val requestState = 100
    private var checkedPermission = PackageManager.PERMISSION_DENIED
    lateinit var manager: TelephonyManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "KotlinApp"
        checkedPermission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_PHONE_STATE);
        if (Build.VERSION.SDK_INT >= 23 && checkedPermission !=
            PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        } else
            checkedPermission = PackageManager.PERMISSION_GRANTED;
    }
    private fun requestPermission() {
        Toast.makeText(this, "Requesting permission", Toast.LENGTH_SHORT).show()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE), requestState)
        }
    }
    fun showDeviceInfo(view: View) {
        manager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val dBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val stringBuilder = StringBuilder()
        if (checkedPermission != PackageManager.PERMISSION_DENIED) {
            dBuilder.setTitle("Device Info")
            stringBuilder.append("""
            SERIAL : ${Build.SERIAL}
            """.trimIndent())
        } else {
            dBuilder.setTitle("Permission denied")
            stringBuilder.append("Can't access device info !")
        }
        dBuilder.setMessage(stringBuilder)
        dBuilder.show()
    }
    override fun onRequestPermissionsResult(requestCode: Int, vararg permissions: String?, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            requestState -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager
                    .PERMISSION_GRANTED) {
                checkedPermission = PackageManager.PERMISSION_GRANTED
            }
        }
    }
}