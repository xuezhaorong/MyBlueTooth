package com.example.mybluetooth.uitls

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothStatusCodes
import android.bluetooth.le.BluetoothLeScanner
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.mybluetooth.HandlerHelper.HandlerHelper
import com.example.mybluetooth.fragment.MessageFragment
import kotlin.concurrent.thread


object BluetoothHelper {
    //蓝牙的特征值，发送
    val SERVICE_EIGENVALUE_SEND = "0000ffe1-0000-1000-8000-00805f9b34fb"

    //蓝牙的特征值，接收
    val SERVICE_EIGENVALUE_READ = "00002902-0000-1000-8000-00805f9b34fb"

    /**
     * 服务 UUID
     */
    val SERVICE_UUID = "0000ff01-0000-1000-8000-00805f9b34fb"

    /**
     * 特性写入 UUID
     */
    val CHARACTERISTIC_WRITE_UUID = "0000ff02-0000-1000-8000-00805f9b34fb"

    /**
     * 特性读取 UUID
     */
    val CHARACTERISTIC_READ_UUID = "0000ff10-0000-1000-8000-00805f9b34fb"

    /**
     * 描述 UUID
     */
    val DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb"

    /**
     * 电池服务 UUID
     */
    val BATTERY_SERVICE_UUID = "0000180f-0000-1000-8000-00805f9b34fb"

    /**
     * 电池特征（特性）读取 UUID
     */
    val BATTERY_CHARACTERISTIC_READ_UUID = "00002a19-0000-1000-8000-00805f9b34fb"

    /**
     * OTA服务 UUID
     */
    val OTA_SERVICE_UUID = "5833ff01-9b8b-5191-6142-22a4536ef123"

    /**
     * OTA特征（特性）写入 UUID
     */
    val OTA_CHARACTERISTIC_WRITE_UUID = "5833ff02-9b8b-5191-6142-22a4536ef123"

    /**
     * OTA特征（特性）表示 UUID
     */
    val OTA_CHARACTERISTIC_INDICATE_UUID = "5833ff03-9b8b-5191-6142-22a4536ef123"

    /**
     * OTA数据特征（特性）写入 UUID
     */
    val OTA_DATA_CHARACTERISTIC_WRITE_UUID = "5833ff04-9b8b-5191-6142-22a4536ef123"


    lateinit var mgatt: BluetoothGatt

    val RXFlag = 0 // 接收数据

    val TXFlag = 1 // 发送数据


    var bluetoothAdapter: BluetoothAdapter? = null

    // 蓝牙扫描器
    var bluetoothScanner: BluetoothLeScanner? = null

    val myBluetoothList = ArrayList<BluetoothDevice>()

    // 写入特征
    lateinit var mwriteCharacteristic: BluetoothGattCharacteristic

    var sendDataSign = 0 // 写入成功标志

    var connectFlag = false //连接成功标志

    // 发送数据
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    fun sendData(Data: String) {
        val txData = Data + "/"
        if (!connectFlag){ // 未连接
            HandlerHelper.handlerSendMessage("未连接")
            return
        }
        // 发送数据线程
        thread {
            val sendData = getSendDataByte(txData)
            sendData.forEach {
                Thread.sleep(100) // 延时100ms
                // 发送数据
                sendDataSign =
                    mgatt.writeCharacteristic(mwriteCharacteristic,it,BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
                if (sendDataSign == BluetoothStatusCodes.SUCCESS) {
                    HandlerHelper.handlerSendMessage(Data)
                } else {
                    Log.d("TAG", "发送数据失败")
                }
            }
        }
    }


    //将数据分包
    private fun dataSeparate(len: Int): IntArray {
        val lens = IntArray(2)
        lens[0] = len / 20
        lens[1] = len % 20
        return lens
    }

    //将String字符串分包为List byte数组
    private fun getSendDataByte(buff: String): List<ByteArray> {
        val listSendData: MutableList<ByteArray> = mutableListOf<ByteArray>()
        val sendDataLength: IntArray = dataSeparate(buff.length)
        if (sendDataLength[0] == 0) { // 不足20
            val dataLess20 = ByteArray(sendDataLength[1])
            for (i in 0 until sendDataLength[1]) {
                dataLess20[i] = buff[i].code.toByte()
            }
            listSendData.add(dataLess20)


        } else { // 超过20
            for (i in 0 until sendDataLength[0]) {
                val dataFor20 = ByteArray(20)
                for (j in 0 until 20) {
                    dataFor20[j] = buff[i * 20 + j].code.toByte()
                }
                listSendData.add(dataFor20)
            }
            if (sendDataLength[1] > 0) {
                val dataLess20 = ByteArray(sendDataLength[1])
                for (i in 0 until sendDataLength[1]) {
                    dataLess20[i] = buff[20 * sendDataLength[0] + i].code.toByte()
                }
                listSendData.add(dataLess20)
            }

        }

        return listSendData
    }
}