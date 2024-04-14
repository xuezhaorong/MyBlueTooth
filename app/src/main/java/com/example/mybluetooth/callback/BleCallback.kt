import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.os.Bundle
import android.os.Message
import android.util.Log
import com.example.mybluetooth.HandlerHelper.HandlerHelper
import com.example.mybluetooth.uitls.BluetoothHelper
import java.util.UUID
import kotlin.concurrent.thread

class BleCallback : BluetoothGattCallback() {
//    private lateinit var uiCallback: UiCallback
//
//    fun setUiCallback(uiCallback: UiCallback) {
//        this.uiCallback = uiCallback
//    }

    /**
     * 连接状态回调
     */
    @SuppressLint("MissingPermission")
    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        if (newState != BluetoothGatt.STATE_CONNECTED) {
            Log.d("TAG", "连接失败 $status")
            return
        } else { // GATT成功
            thread {
                gatt.discoverServices() // 扫描服务
                Thread.sleep(3000)

            } // 开启发现服务线程
        }
    }

    // 发现服务回调函数
    @SuppressLint("MissingPermission", "NewApi")
    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        val characteristicsList = gatt?.services // 所有服务的列表
        if (gatt != null) {
            BluetoothHelper.mgatt = gatt
            Log.d("TAG", "获取Gatt成功")
        }
        characteristicsList?.forEach { bluetoothGattService -> // 遍历所有服务
            bluetoothGattService.characteristics.forEach { // 遍历所有特
                if (it.uuid.toString() == BluetoothHelper.SERVICE_EIGENVALUE_SEND) { // 获取写入特征
                    Log.d("TAG", "已获取写入特征")
                    BluetoothHelper.mwriteCharacteristic = it // 存储写入特征
                    BluetoothHelper.mgatt.setCharacteristicNotification(it, true) // 打开通知通道
                    thread {
                        Thread.sleep(200)
                        val clientConfig = BluetoothHelper.mwriteCharacteristic.getDescriptor(
                            UUID.fromString(BluetoothHelper.SERVICE_EIGENVALUE_READ)
                        )
                        if (clientConfig != null) {
                            BluetoothHelper.mgatt.writeDescriptor(
                                clientConfig,
                                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            )// 设置监听模块
                            Log.d("TAG", "设置监听成功")
                            Log.d("TAG", "连接成功")
                            BluetoothHelper.connectFlag = true
                        }

                        Thread.sleep(200)
                    }
                }
            }

        }


    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        if (status == BluetoothGatt.GATT_SUCCESS){
            Log.d("TAG","发送数据成功")
        }else{
            Log.d("TAG","发送数据失败")
        }
    }


    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        super.onCharacteristicChanged(gatt, characteristic, value)
        var result = ""
        value.forEach {
            result += it.toInt().toChar().toString()
        }
        Log.d("TAG", result)
        thread {
            val message = Message()
            message.what = BluetoothHelper.RXFlag
            message.data = Bundle().apply {
                putString("data",result)
            }
            HandlerHelper.messageHandler.sendMessage(message)
        }
    }
}
