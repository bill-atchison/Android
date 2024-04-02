package com.serialdemo

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.*
import java.lang.Exception
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

class MainActivity : AppCompatActivity() {
    private val requestState = 100
    private var checkedPermission = PackageManager.PERMISSION_DENIED
    lateinit var manager: TelephonyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "KotlinApp"

        /*
        checkedPermission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_PHONE_STATE);
        if (Build.VERSION.SDK_INT >= 23 && checkedPermission !=
            PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        } else
            checkedPermission = PackageManager.PERMISSION_GRANTED;
         */

        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                checkedPermission = PackageManager.PERMISSION_GRANTED
            }
        }
        if (checkedPermission != PackageManager.PERMISSION_GRANTED) {
            val uri = Uri.parse(String.format("package:%s", applicationContext.packageName))
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri
                )
            )
        }

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
        /*
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
        */

        dBuilder.setTitle("Device Info")

        val doc = readXml()
        try {
            val serial = getAttributeValuesByAttributeNameAndAttributeValue(
                doc, "CC610",
                "Device", "Serial"
            )
            dBuilder.setMessage(serial)
        } catch(e:Exception) {
            dBuilder.setMessage(e.message)
        }
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

    fun createSampleFile(view: View) {
        val dBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        dBuilder.setTitle("Device Info")

        val resolver = this.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "my_sample_file")
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents")
        }
        val uri: Uri? = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

        //dBuilder.setMessage("$uri")
        //dBuilder.show()

        try {
            if (uri != null) {
                resolver.openOutputStream(uri).use {
                    // TODO something with the stream
                    it?.write("This is a sample File.".toByteArray())
                    it?.close()
                    dBuilder.setMessage("Sample File Has Been Created")
                }
            }
        } catch (e: Exception) {
            dBuilder.setMessage(e.message)
        }

        dBuilder.show()
    }

    fun readStatsFile(view: View) {
        val dBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        dBuilder.setTitle("Device Info")

        var txtFile = File("/storage/emulated/0/Stats", "MxStats.xml")
        val text = java.lang.StringBuilder()
        try {
            // om below line creating amd initializing buffer reader.
            val br = BufferedReader(FileReader(txtFile))
            // on below line creating string variable
            var line:String?
            // on below line setting the data to text
            while (br.readLine().also{line = it } != null) {
                text.append(line)
                val space = " "
                text.append(space)
            }
            br.close()
            dBuilder.setMessage(text.toString())
        } catch (e:Exception) {
            dBuilder.setMessage(e.message)
        }

        dBuilder.show()
    }

    fun readXml(): Document {
        val xmlFile = File("/storage/emulated/0/Stats", "MxStats.xml")

        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val xmlInput = InputSource(StringReader(xmlFile.readText()))
        val doc = dBuilder.parse(xmlInput)

        return doc
    }

    fun getAttributeValuesByAttributeNameAndAttributeValue(doc: Document, attributeValue: String,
        attributeName: String, attributeValueName: String): String {
        val xpFactory = XPathFactory.newInstance()
        val xPath = xpFactory.newXPath()

        // <MXStats End="2024/03/08 10:38:38" Start="2024/02/28 11:39:11" Device="CC610" Serial="22348524301423">
        val xpath = "/MXStats[contains(@$attributeName, '$attributeValue')]"

        val items = xPath.evaluate(xpath, doc, XPathConstants.NODESET) as NodeList
        val attributeValue = items.item(0).attributes.getNamedItem(attributeValueName)
        val value = attributeValue.nodeValue
//    return xpath.toString()
        return value
    }
}