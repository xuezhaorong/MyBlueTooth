package com.example.mybluetooth.fragment

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mybluetooth.HandlerHelper.HandlerHelper
import com.example.mybluetooth.MainActivity
import com.example.mybluetooth.databinding.FragmentMessageBinding
import com.example.mybluetooth.uitls.BluetoothHelper
import kotlinx.coroutines.flow.combine


class MessageFragment : Fragment() {



    private lateinit var binding: FragmentMessageBinding
    private lateinit var mainActivity:MainActivity

    @SuppressLint("NewApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if(!::binding.isInitialized) {
            binding = FragmentMessageBinding.inflate(inflater, container, false)
            // 注册handler
            HandlerHelper.registerMessageBinding(binding)
        }

        mainActivity = requireContext() as MainActivity

        binding.sendButton.setOnClickListener{
            BluetoothHelper.sendData(binding.editText.text.toString())
        }


        return binding.root
    }



}