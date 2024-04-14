package com.example.mybluetooth.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context.BLUETOOTH_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.example.mybluetooth.MainActivity
import com.example.mybluetooth.R



class PermissionFragment : Fragment() {
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                ToastWarining("Permission request granted")
            } else {
                ToastWarining("Permission request denied")
            }
        }

    private fun ToastWarining(content:String) =
        Toast.makeText(mainActivity,content,Toast.LENGTH_LONG).show()


    private fun PermissionGranted(){ // 权限申请

        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) -> {
                // 转到List
                lifecycleScope.launchWhenStarted {
                    Navigation.findNavController(
                        requireActivity(),
                        R.id.fragment_container
                    ).navigate(
                        R.id.action_Permission_Fragment_to_List_Fragment
                    )
                }

            }
            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            }
        }

    }

    private lateinit var mainActivity: MainActivity


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = requireContext() as MainActivity

        // 权限申请
        PermissionGranted()



    }


}