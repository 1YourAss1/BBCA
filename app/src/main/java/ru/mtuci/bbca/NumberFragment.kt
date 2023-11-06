package ru.mtuci.bbca

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import java.io.ByteArrayOutputStream


const val ARG_OBJECT = "object"

class NumberFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_number, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.takeIf { it.containsKey(ARG_OBJECT) }?.apply {
            val imageView: ImageView = view.findViewById(R.id.imageView)
            resources.assets.open("meme_${getInt(ARG_OBJECT)}.jpg").use { inputStream ->
                val drawable = Drawable.createFromStream(inputStream, null)
                val bitmap = drawable?.toBitmap()
                val compressedBitmapOutputStream = ByteArrayOutputStream()

                bitmap?.compress(
                    Bitmap.CompressFormat.JPEG,
                    80,
                    compressedBitmapOutputStream
                )

                val compressedBitmapBytes = compressedBitmapOutputStream.toByteArray()

                val compressedBitmap = BitmapFactory.decodeByteArray(
                    compressedBitmapBytes,
                    0,
                    compressedBitmapBytes.size
                )

                imageView.setImageBitmap(compressedBitmap)
            }
        }
    }

}