package com.luiz.nativ.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream

class Base64Converter {
    companion object {
        fun drawableToString(drawable: Drawable): String {
            val pictureDrawable = drawable as BitmapDrawable
            val originalBitmap = pictureDrawable.bitmap

            val ratio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
            val width = if (originalBitmap.width > originalBitmap.height) 800 else (800 * ratio).toInt()
            val height = if (originalBitmap.width > originalBitmap.height) (800 / ratio).toInt() else 800
            val bitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val imageString = Base64.encodeToString(outputStream.toByteArray(), 0)
            return imageString
        }

        fun stringToBitmap(imageString: String): Bitmap {
            val imageBytes = Base64.decode(imageString, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }
    }
}