package com.example.mybluetooth.fragment

import BleCallback
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mybluetooth.uitls.BluetoothHelper
import com.example.mybluetooth.MainActivity
import com.example.mybluetooth.databinding.BlutoothItemBinding
import com.example.mybluetooth.databinding.FragmentListBinding
import java.io.IOException
import java.util.UUID


class ListFragment : Fragment(){


    private val bleCallback = BleCallback() // ble回调


    private lateinit var binding: FragmentListBinding
    private lateinit var mainActivity: MainActivity




    //打开蓝牙意图
    val enableBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        when(it.resultCode){
            Activity.RESULT_OK->
                ToastWarining("蓝牙已打开" )
            else->
                ToastWarining("蓝牙未打开" )

        }
    }

    //请求BLUETOOTH_SCAN权限意图
    private val requestBluetoothScan =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                //进行扫描
                startScan()
            } else {
                ToastWarining("Android12中未获取此权限，则无法扫描蓝牙。")
            }
        }



    private fun ToastWarining(content:String) =
        Toast.makeText(mainActivity,content, Toast.LENGTH_LONG).show()


    // 适配器
    inner class MyDeviceAdapter(val context: Context, val bluetoothList:List<BluetoothDevice>): RecyclerView.Adapter<MyDeviceAdapter.ViewHolder>() {
        inner class ViewHolder(val binding: BlutoothItemBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding: BlutoothItemBinding =
                BlutoothItemBinding.inflate(LayoutInflater.from(context), parent, false)
            return ViewHolder(binding)
        }

        @SuppressLint("MissingPermission")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val device = bluetoothList[position]
            holder.binding.bluetoothName.text = device.name.toString()
            holder.binding.bluetoothMacId?.text = device.address.toString()
            holder.itemView.setOnClickListener {// 开始配对
                stopScan() // 停止搜索
                device.connectGatt(mainActivity,false,bleCallback)
            }
        }

        override fun getItemCount() = bluetoothList.size

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if(!::binding.isInitialized) {
            binding = FragmentListBinding.inflate(inflater, container, false)
        }
        mainActivity = requireContext() as MainActivity



        // Inflate the layout for this fragment
        with(binding.recyclerView){
            layoutManager = LinearLayoutManager(mainActivity)
            adapter = MyDeviceAdapter(mainActivity, BluetoothHelper.myBluetoothList)
        }

        // 强制刷新
        binding.fab.setOnClickListener{view->
            BluetoothHelper.myBluetoothList.clear()
            isScanning = false
            startScan()
        }

        return binding.root
    }


    //扫描结果回调
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
//            Log.d("TAG", "name: ${device.name}, address: ${device.address}")
            if (device !in BluetoothHelper.myBluetoothList && device.name != null){ // 添加进列表中
                BluetoothHelper.myBluetoothList.add(device)
                binding.recyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }

    private var isScanning = false

    @SuppressLint("MissingPermission")
    private fun startScan() {
        if (!isScanning) {
            BluetoothHelper.bluetoothScanner?.startScan(scanCallback)
            isScanning = true
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopScan() {
        if (isScanning) {
            BluetoothHelper.bluetoothScanner?.stopScan(scanCallback)
            isScanning = false
        }
    }




    override fun onResume() {
        super.onResume()


        // 蓝牙未开启，开启蓝牙
        if (BluetoothHelper.bluetoothAdapter?.isEnabled == false){
            enableBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }



        // 获得扫描器
        BluetoothHelper.bluetoothScanner = BluetoothHelper.bluetoothAdapter?.bluetoothLeScanner

        // 开始扫描
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_SCAN
            ) -> {
                startScan()
            }
            else -> {
                requestBluetoothScan.launch(
                    Manifest.permission.BLUETOOTH_SCAN
                )
            }
        }


    }

    override fun onPause() {
        super.onPause()

        // 暂停
        stopScan()

    }



}