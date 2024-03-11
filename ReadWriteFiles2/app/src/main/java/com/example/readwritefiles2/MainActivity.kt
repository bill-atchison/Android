package com.example.readwritefiles2

import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.lang.Exception
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    private lateinit var btn: Button
    private var imageUri: Uri? = null
    private lateinit var btn2: Button
    private lateinit var btn3: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn = findViewById(R.id.btnAddText)
        btn2 = findViewById(R.id.btnReadText)
        btn3 = findViewById(R.id.btnUpdateText)

        btn.setOnClickListener {
            lifecycleScope.launch {
                saveDocument(applicationContext, "Original ")
            }
        }
        val dBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        dBuilder.setTitle("Device Info")

        btn2.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val collection =
                        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

                    val dirDest = File(
                        Environment.DIRECTORY_DOCUMENTS,
                        applicationContext.getString(R.string.app_name)
                    )
                    val date = System.currentTimeMillis()
                    val fileName = "test.txt"

                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            "$dirDest${File.separator}"
                        )
                        put(MediaStore.Files.FileColumns.IS_PENDING, 1)
                    }

                    imageUri = applicationContext.contentResolver.insert(collection, contentValues)

                    imageUri?.let { it1 ->
                        val data = read(applicationContext, it1)

                        Toast.makeText(applicationContext, "The data is $data ", Toast.LENGTH_LONG)
                            .show()
                    }
                } catch(e: Exception) {
                    dBuilder.setMessage(e.message)
                    dBuilder.show()
                }
            }
        }

        dBuilder.show()

        btn3.setOnClickListener {
            lifecycleScope.launch {
                updateData(applicationContext, "Appended")
            }
        }
    }

    suspend fun saveDocument(context: Context, text: String) {
        withContext(Dispatchers.IO) {
            try {
                val collection =
                    MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

                val dirDest = File(
                    Environment.DIRECTORY_DOCUMENTS,
                    context.getString(R.string.app_name)
                )
                val date = System.currentTimeMillis()
                val fileName = "test.txt"


                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        "$dirDest${File.separator}"
                    )
                    put(MediaStore.Files.FileColumns.IS_PENDING, 1)
                }

                imageUri = context.contentResolver.insert(collection, contentValues)

                withContext(Dispatchers.IO) {
                    imageUri?.let { uri ->
                        context.contentResolver.openOutputStream(uri, "w").use { out ->
                            out?.write(text.toByteArray())
                        }
                        contentValues.clear()
                        contentValues.put(MediaStore.Files.FileColumns.IS_PENDING, 0)

                        context.contentResolver.update(uri, contentValues, null, null)
                    }
                }
            } catch (e: FileNotFoundException) {
                null
            }
        }
    }

    suspend fun updateData(context: Context, text: String) {
        withContext(Dispatchers.IO) {
            try {
                val collection =
                    MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

                val dirDest = File(
                    Environment.DIRECTORY_DOCUMENTS,
                    context.getString(R.string.app_name)
                )
                val fileName = "test.txt"

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        "$dirDest${File.separator}"
                    )
                    put(MediaStore.Files.FileColumns.IS_PENDING, 1)
                }

                withContext(Dispatchers.IO) {
                    imageUri?.let { uri ->
                        context.contentResolver.openOutputStream(uri, "wa").use { out ->
                            out?.write(text.toByteArray())
                        }
                        contentValues.clear()
                        contentValues.put(MediaStore.Files.FileColumns.IS_PENDING, 0)

                        context.contentResolver.update(uri, contentValues, null, null)
                    }
                }
            } catch (e: FileNotFoundException) {
                null
            }
        }
    }

    suspend fun read(context: Context, source: Uri): String = withContext(Dispatchers.IO) {
        val resolver: ContentResolver = context.contentResolver

        resolver.openInputStream(source)?.use { stream -> stream.readText() }
            ?: throw IllegalStateException("could not open $source")
    }

    private fun InputStream.readText(charset: Charset = Charsets.UTF_8): String =
        readBytes().toString(charset)
}