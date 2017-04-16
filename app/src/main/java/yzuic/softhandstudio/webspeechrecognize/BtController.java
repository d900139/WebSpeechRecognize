package yzuic.softhandstudio.webspeechrecognize;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Kevin Chang on 2017/2/9.
 * Reference 1 : Google git - BluetoothChat
 * https://android.googlesource.com/platform/development/+/25b6aed7b2e01ce7bdc0dfa1a79eaf009ad178fe/
 * samples/BluetoothChat?autodive=0
 * Reference 2 : Tony Huang's GitHub -  BtController
 * https://github.com/starlightslo/BtController
 */

public class BtController {
    private final String TAG = "BtController";

    private BluetoothAdapter mBluetoothAdapter = null;
    private final Handler mHandler;
    private ConnectedThread mConnectedThread = null;
    private ArrayList<ConnectedThread> mConnectedThreads;
    private ArrayList<BluetoothSocket> mSockets;
    private ArrayList<BluetoothDevice> mDevices;

    //藍牙串口服務 : SerialPortServiceClass_UUID = '{00001101-0000-1000-8000-00805F9B34FB}'
    private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    public BtController(Handler handler) {
        this.mHandler = handler;
    }

    /**
         * 開啟裝置藍芽服務
         *  Enable device Bluetooth service
         */
    public boolean enableBluetooth() {
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.enable()) {
                return false;
            }
        }
        return true;
    }

    /**
         * 關閉裝置藍芽服務
         *  Disable device Bluetooth service
         */
    public void disableBluetooth() {
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
        }
    }

    /**
         * 開始執行並檢查裝置是否支援藍芽
         *  Start and check if device support bluetooth service
         */
    public boolean start() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        initializeArrayLists();
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "Your device do not support bluetooth!");
            return false;
        }
        if (!enableBluetooth()) {
            Log.w(TAG, "Bluetooth do not enable!");
            return false;
        }
        return true;
    }

    /**
         * 初始化陣列
         *  Initialize ArrayLists
         */
    private void initializeArrayLists() {
        mConnectedThreads = new ArrayList<ConnectedThread>(7);
        mSockets = new ArrayList<BluetoothSocket>(7);
        mDevices = new ArrayList<BluetoothDevice>(7);

        for (int i = 0; i < 7; i++) {
            mConnectedThreads.add(null);
            mSockets.add(null);
            mDevices.add(null);
        }

        Log.i(TAG, "mConnThreads.size() in Service Constructor--"
                + mConnectedThreads.size());
    }

    /**
         * 取得已配對裝置列表
         *  Get paired devices list
         */
    public List<BluetoothDevice> getPairedDevices() {
        if (mBluetoothAdapter == null)
            return null;
        Set<BluetoothDevice> setPairedDevices = mBluetoothAdapter.getBondedDevices();
        List<BluetoothDevice> pairedDevices = new ArrayList<>();
        for (BluetoothDevice device : setPairedDevices) {
            pairedDevices.add(device);
        }
        return pairedDevices;
    }

    /**
         * 連線裝置
         *  Connect device
         */
    public void connectDevice(BluetoothDevice device, int position) {
        Log.w(TAG, "connectDevice()");
        mDevices.set(position, mBluetoothAdapter.getRemoteDevice(device.getAddress()));
        try {
            mSockets.set(position, device.createRfcommSocketToServiceRecord(uuid));
            mSockets.get(position).connect();
            if (mSockets.get(position).isConnected()) {
                Log.w(TAG, "Device " + device.getName() + " is connected!");
                mConnectedThread = new ConnectedThread(position);
                mConnectedThreads.set(position, mConnectedThread);
                mConnectedThreads.get(position).start();
                // 將連線上的裝置名稱回傳至 UI 介面
                // Send the name of the connected device back to the UI Activity
                Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
                Bundle bundle = new Bundle();
                bundle.putString(MainActivity.DEVICE_NAME, device.getName());
                bundle.putInt("pos", position);
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            } else {
                Log.w(TAG, "Device connect fail!");
            }
        } catch (IOException e) {
            Log.w(TAG, "Device connect fail!", e);
            // 將連線失敗的裝置名稱回傳至 UI 介面
            // Send the name of the connected fail device back to the UI Activity
            Message msg = mHandler.obtainMessage(MainActivity.CONNECTION_FAIL);
            Bundle bundle = new Bundle();
            bundle.putString(MainActivity.DEVICE_NAME, device.getName());
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
        Log.w(TAG, mConnectedThreads.toString());
    }

    /**
         * 關閉指定裝置連線
         *  Disconnect specific device
         */
    public void disconnectDevice(int position) {
        Log.w(TAG, "disconnectDevice()");

        if (mSockets.get(position).isConnected()) {
            mConnectedThreads.get(position).cancel();
        }
        mConnectedThreads.set(position, null);
        mSockets.set(position, null);
        Log.w(TAG, mConnectedThreads.toString());
    }

    /**
         * 關閉所有裝置連線
         *  Disconnect all devices
         */
    public void disconnectAllDevices() {
        Log.w(TAG, "disconnectAllDevices()");
        for (int i = 0; i < 7; i++) {
            if (mSockets.get(i) != null && mSockets.get(i).isConnected()) {
                mConnectedThreads.get(i).cancel();
            }
            mConnectedThreads.set(i, null);
            mSockets.set(i, null);
        }
        Log.w(TAG, mConnectedThreads.toString());
    }

    /**
         * 取得指定裝置連線狀態
         *  Get specific device connect status
         */
    public Boolean getConnectStatus(int position) {
        Boolean status;
        if (mSockets.get(position) != null) {
            if (!mSockets.get(position).isConnected())
                status = false;
            else
                status = true;
        } else
            status = false;
        return status;
    }

    /**
         * 傳送訊息至已連線的指定裝置
         *  Send msg to specific device if it's connected
         */
    public void sendToSpecificDevice(String msg, int position) {
        if (!msg.equals("")) {
            if (getConnectStatus(position)) {
                mConnectedThreads.get(position).write(msg.getBytes());
                Log.w(TAG, "Sending: str msg :" + msg + " , msg.getBytes() : " + msg.getBytes());
            } else {
                Log.w(TAG, "No device connected !");
            }
        }
    }

    /**
         * 傳送訊息至所有已連線的裝置
         *  Send msg to all connected devices
         */
    public void sendToAllDevices(String msg) {
        if (!msg.equals("")) {
            for (int i = 0; i < 7; i++) {
                if (getConnectStatus(i)) {
                    mConnectedThreads.get(i).write(msg.getBytes());
                    Log.w(TAG, "Sending: str msg :" + msg + " , msg.getBytes() : " + msg.getBytes());
                } else {
                    Log.w(TAG, "Device " + i + " doesn't connected !");
                }
            }
        }
    }

    /**
         *  傳送訊息至除指定裝置外的所有已連線裝置
         *  Send msg to all connected devices except specific one
         */
    public void sendToOthersDevices(String msg, int position) {
        if (!msg.equals("")) {
            for (int i = 0; i < 7; i++) {
                if (getConnectStatus(i)) {
                    if (i != position) {
                        mConnectedThreads.get(i).write(msg.getBytes());
                        Log.w(TAG, "Sending: str msg :" + msg + " , msg.getBytes() : " + msg.getBytes());
                    }
                } else {
                    Log.w(TAG, "Device " + i + " doesn't connected !");
                }
            }
        }
    }

    /**
         * Indicate that the connection was lost and notify the UI Activity.
         */
    private void connectionLost(int position) {
        Message msg = mHandler.obtainMessage(MainActivity.CONNECTION_LOST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.DEVICE_NAME, mDevices.get(position).getName());
        bundle.putInt("ps", position);
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        mConnectedThreads.set(position, null);
        mSockets.set(position, null);
        Log.w(TAG, mConnectedThreads.toString());
    }

    /*-----------------------------ConnectedThread---------------------------*/
    // 裝至連線後開啟多重執行緒以持續監聽及傳送訊息

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private int position = -1;

        public ConnectedThread(int position) {
            Log.d(TAG, "create ConnectedThread");

            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            this.position = position;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = mSockets.get(position).getInputStream();
                tmpOut = mSockets.get(position).getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;
            // 連線時持續監聽 InputStream，並將收到的訊息回傳至 UI 介面
            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                    MainActivity.DEVICE_NUM = position;
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost(position);
                    break;
                }
            }
        }

        // 送出訊息至已連線裝置 Send message
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                mHandler.obtainMessage(MainActivity.MESSAGE_WRITE, -1,
                        -1, buffer).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        // 關閉裝置連線 Close connection
        public void cancel() {
            try {
                mSockets.get(position).close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
