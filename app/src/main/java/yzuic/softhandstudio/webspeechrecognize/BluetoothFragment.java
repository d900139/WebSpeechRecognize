package yzuic.softhandstudio.webspeechrecognize;

import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin Chang on 2017/3/15.
 */

public class BluetoothFragment extends Fragment {

    private final String TAG_BT = "Bluetooth";
    private List<BluetoothDevice> mDevices = new ArrayList<>();
    private String[] mDevicesNameStr = new String[0];
    private String[] mStatusStr = new String[0];
    private Boolean _isFirstClick = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        Log.w(TAG_BT, "OnCreate");
        Log.w(TAG_BT, "Now on BluetoothFragment!");

        Button mPaired = (Button) rootView.findViewById(R.id.paired);
        Button goToSelectAge = (Button) rootView.findViewById(R.id.goToSelectAge);
        ListView mDevicesList = (ListView) rootView.findViewById(R.id.listView);

        MainActivity.mDeviceAdapter = new DeviceAdapter(getActivity(), mDevicesNameStr, mStatusStr);
        mDevicesList.setAdapter(MainActivity.mDeviceAdapter);

        mPaired.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(TAG_BT, " mPaired.OnClick()");
                if (BluetoothAdapter.getDefaultAdapter() == null) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("您的裝置不支援藍牙!")
                            .setMessage("是否要離開？ T.T")
                            .setPositiveButton("是的，我要離開!", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    getActivity().finish();
                                }
                            })
                            .setNegativeButton("否，參觀一下再說!", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                } else if (_isFirstClick) {
                    _isFirstClick = false;
                    showPairedDevices();
                } else
                    MainActivity.mDeviceAdapter.notifyDataSetChanged();
            }
        });

        goToSelectAge.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(TAG_BT, " goToSelectAge.OnClick()");
                getFragmentManager().beginTransaction().replace(R.id.container,
                        new WebviewFragment(), "WebviewFragment").addToBackStack(null).commit();
            }
        });

        mDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.w(TAG_BT, " mDevicesList.OnItemClick()");
                MainActivity.mDevice = mDevices.get(position);

                if (MainActivity.mDeviceAdapter.getDeviceStatus(position).equals("已連線")) {
                    MainActivity.mBtController.disconnectDevice(position);
                } else {
                    MainActivity.mBtController.connectDevice(MainActivity.mDevice, position);
                }
            }
        });

        return rootView;
    }

    public void showPairedDevices() {
        Log.w(TAG_BT, TAG_BT + " showPairedDevices()");

        mDevices = MainActivity.mBtController.getPairedDevices();
        mDevicesNameStr = new String[mDevices.size()];
        String[] mDevicesMacAddressStr = new String[mDevices.size()];
        mStatusStr = new String[mDevices.size()];
        for (int i = 0; i < mDevicesNameStr.length; i++) {
            mDevicesNameStr[i] = mDevices.get(i).getName();
            mDevicesMacAddressStr[i] = mDevices.get(i).getAddress();
            mStatusStr[i] = "未連線";
            Log.w(TAG_BT, "Device Name: " + mDevicesNameStr[i] + " , Mac Address: "
                    + mDevicesMacAddressStr[i]);
        }
        MainActivity.mDeviceAdapter.update(mDevicesNameStr, mStatusStr);
        MainActivity.mDeviceAdapter.notifyDataSetChanged();
    }
}