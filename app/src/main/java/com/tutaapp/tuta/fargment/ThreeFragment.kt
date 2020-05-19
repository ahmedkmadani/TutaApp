package com.tutaapp.tuta.fargment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tutaapp.tuta.R

class ThreeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.fragment_service, container, false)!!
}
