package ru.mtuci.bbca

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import ru.mtuci.bbca.app_logger.catchWithPayload


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

            val memeNumber = getInt(ARG_OBJECT)

            catchWithPayload("meme is: $memeNumber") {
                Glide.with(requireContext())
                    .load(Uri.parse("file:///android_asset/meme_${memeNumber}.jpg"))
                    .into(imageView)
            }
        }
    }

}