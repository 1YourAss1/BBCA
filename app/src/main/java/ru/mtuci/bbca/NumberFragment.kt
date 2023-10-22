package ru.mtuci.bbca

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment


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
            val imageResource : Int = resources.getIdentifier("meme_${getInt(ARG_OBJECT)}", "drawable", activity?.packageName)
            val res = resources.getDrawable(imageResource)
            imageView.setImageDrawable(res)
//            val textViewNumber: TextView = view.findViewById(R.id.textViewNumber)
//            textViewNumber.text = getInt(ARG_OBJECT).toString()
        }
    }

}