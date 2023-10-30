package ru.mtuci.bbca

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class NumberAdapter(fragment: FragmentActivity, private val randomInts: IntArray) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = randomInts.size

    override fun createFragment(position: Int): Fragment {
        val fragment = NumberFragment()
        fragment.arguments = Bundle().apply {
            putInt(ARG_OBJECT, randomInts[position])
        }
        return fragment
    }

}