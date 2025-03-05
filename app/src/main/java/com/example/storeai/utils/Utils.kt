package com.example.storeai.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

// Utils.kt
fun Uri.toTempFile(context: Context): File {
    val inputStream = context.contentResolver.openInputStream(this)
        ?: throw FileNotFoundException()

    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val file = File.createTempFile("IMG_${timeStamp}_", ".jpg", context.cacheDir)

    file.outputStream().use { output ->
        inputStream.copyTo(output)
    }

    return file
}