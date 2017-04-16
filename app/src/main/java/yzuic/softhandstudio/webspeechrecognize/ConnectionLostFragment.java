package yzuic.softhandstudio.webspeechrecognize;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Kevin Chang on 2017/3/15.
 */

public class ConnectionLostFragment extends Fragment implements View.OnTouchListener {
    private final String TAG_CL = "ConnectionLost";
    public static String CONN_FAIL_DEVICE_NAME = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_connection_lost, container, false);
        rootView.setOnTouchListener(this);

        Log.w(TAG_CL, "OnCreate");
        Log.w(TAG_CL, "Now on ConnectionLostFragment!");

        Button btn_stopAndBack = (Button) rootView.findViewById(R.id.btn_stopAndBack);
        TextView text_close = (TextView) rootView.findViewById(R.id.text_close);
        TextView text_title = (TextView) rootView.findViewById(R.id.text_title);
        TextView text_content = (TextView) rootView.findViewById(R.id.text_content);

        text_content.setText("請稍候...");

        btn_stopAndBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(TAG_CL, "Go Back!");

                if (getFragmentManager().getBackStackEntryCount() == 2) {
                    if (getFragmentManager().findFragmentByTag("SelectAgeFragment") != null) {
                        getFragmentManager().popBackStack();
                        getFragmentManager().popBackStack();
                        Log.w(TAG_CL, "Now on BluetoothFragment!");
                    } else {
                        getFragmentManager().popBackStack();
                    }
                } else if (getFragmentManager().getBackStackEntryCount() == 1) {
                    getFragmentManager().popBackStack();
                    Log.w(TAG_CL, "Now on BluetoothFragment!");
                } else {
                    getFragmentManager().popBackStack();
                }
                MainActivity.mDeviceAdapter.notifyDataSetChanged();
            }
        });

        text_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(TAG_CL, "Close!");

                if (getFragmentManager().getBackStackEntryCount() == 2) {
                    if (getFragmentManager().findFragmentByTag("SelectAgeFragment") != null) {
                        getFragmentManager().popBackStack();
                        getFragmentManager().popBackStack();
                        Log.w(TAG_CL, "Now on BluetoothFragment!");
                    } else {
                        getFragmentManager().popBackStack();
                    }
                } else if (getFragmentManager().getBackStackEntryCount() == 1) {
                    getFragmentManager().popBackStack();
                    Log.w(TAG_CL, "Now on BluetoothFragment!");
                } else {
                    getFragmentManager().popBackStack();
                }
                MainActivity.mDeviceAdapter.notifyDataSetChanged();
            }
        });

        text_title.setText("待維修...");

        String msg = CONN_FAIL_DEVICE_NAME + " 連線已中斷! 請稍候工作人員維修及安排...";
        text_content.setText(msg);

        return rootView;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;   //使點擊無法穿透 - 原文網址：https://read01.com/L6Lyx.html
    }
}
