package com.example.mybluetooth.HandlerHelper

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.example.mybluetooth.databinding.FragmentMessageBinding
import com.example.mybluetooth.uitls.BluetoothHelper
import kotlin.concurrent.thread

object HandlerHelper {

    var messagebinding: FragmentMessageBinding?= null

    fun registerMessageBinding(binding: FragmentMessageBinding){
        messagebinding = binding
    }

    val messageHandler = object :Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val data = msg.data.getString("data")
            val message = messagebinding?.messageView?.text
            when(msg.what){
                BluetoothHelper.TXFlag->{
                    messagebinding?.messageView?.text = "${message}\n<-${data}"
                }
                BluetoothHelper.RXFlag->{
                    messagebinding?.messageView?.text = "${message}\n->${data}"
                }
            }
        }
    }

    fun handlerSendMessage(Data:String){
        thread {
            val message = Message()
            message.what = BluetoothHelper.TXFlag
            message.data = Bundle().apply {
                putString("data",Data)
            }
            messageHandler.sendMessage(message)
        }

    }
}