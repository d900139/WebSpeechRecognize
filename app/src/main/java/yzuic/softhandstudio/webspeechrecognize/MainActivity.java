package yzuic.softhandstudio.webspeechrecognize;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends Activity {
    private final String TAG = "MainActivity";

    public static BtController mBtController;
    public static BluetoothDevice mDevice;
    public static DeviceAdapter mDeviceAdapter;

    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_DEVICE_NAME = 2;
    public static final int CONNECTION_LOST = 3;
    public static final int CONNECTION_FAIL = 4;
    public static final int MESSAGE_WRITE = 5;
    public static final String DEVICE_NAME = "device_name";
    public static int DEVICE_NUM = 0;

    private Boolean _isReStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_main);

        Log.w(TAG, "OnCreate");
        Log.w(TAG, "Now on MainActivity!");

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.container, new BluetoothFragment()).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.w(TAG, "OnResume");

        if (!_isReStart) {
            Log.w(TAG, "First Start!");
            _isReStart = true;
            mBtController = new BtController(mHandler);

            if (!mBtController.start()) {
                Log.w(TAG, "Start BtController failed.");
                if (BluetoothAdapter.getDefaultAdapter() == null) {
                    new AlertDialog.Builder(this)
                            .setTitle("您的裝置不支援藍牙!")
                            .setMessage("是否要離開？ T.T")
                            .setPositiveButton("是的，我要離開!", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            })
                            .setNegativeButton("否，參觀一下再說!", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                }
            }
        } else {
            Log.w(TAG, "ReStart!");
        }
    }

    @Override
    public void onBackPressed() {
        Log.w(TAG, "onBackPressed()");
        int count = getFragmentManager().getBackStackEntryCount();

        switch (count) {
            case 0:
                Log.w(TAG, "onBackPressed() , Case 0");
                Log.w(TAG, "Now on BluetoothFragment!");
                new AlertDialog.Builder(this)
                        .setTitle("是否離開程式？")
                        .setMessage("確定要離開？ T.T")
                        .setPositiveButton("是的，我要離開!", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mBtController.disconnectAllDevices();
                                dialog.dismiss();
                                finish();
                            }
                        })
                        .setNegativeButton("否，我按錯了!", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                break;
            case 1:
                Log.w(TAG, "onBackPressed() , Case 1");
                if (getFragmentManager().findFragmentByTag("SelectAgeFragment") != null) {
                    new AlertDialog.Builder(this)
                            .setTitle("返回設定？")
                            .setMessage("確定要回到藍芽設定頁面？")
                            .setPositiveButton("是的，我要回去!", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    getFragmentManager().popBackStack();
//                                    SelectAgeFragment.reset();
                                    Log.w(TAG, "Now on BluetoothFragment!");
                                }
                            })
                            .setNegativeButton("否，我按錯了!", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                } else {
                    getFragmentManager().popBackStack();
                    Log.w(TAG, "Now on BluetoothFragment!");
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBtController.disableBluetooth();
//        if(SpeechRecognizeFragment.recognizer != null)
//            SpeechRecognizeFragment.stopSpeechRecognizer();
//        System.exit(0); // kill process
    }

    public final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.w(TAG, "Get message from " + mDeviceAdapter.getDeviceName(DEVICE_NUM) + " : " + readMessage);
                    Toast.makeText(getApplicationContext(), readMessage, Toast.LENGTH_SHORT).show();
                    switch (readMessage) {
                        case "t":
                            Log.w(TAG, mDeviceAdapter.getDeviceName(DEVICE_NUM) + ": t");
//                            SpeechRecognizeFragment.startSpeechRecognizer(MainActivity.this);
                            break;
                        case "f":
                            Log.w(TAG, mDeviceAdapter.getDeviceName(DEVICE_NUM) + ": f");
//                            SpeechRecognizeFragment.stopSpeechRecognizer();
                            break;
                        default:
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Log.w(TAG, "Send message : " + writeMessage);
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    Toast.makeText(getApplicationContext(), msg.getData().getString(DEVICE_NAME) + " 已連線!", Toast.LENGTH_SHORT).show();
                    int p = msg.getData().getInt("pos");
                    updateDeviceStatus(p);
                    break;
                case CONNECTION_LOST:
                    int ps = msg.getData().getInt("ps");
                    getFragmentManager().beginTransaction().add(R.id.container, new ConnectionLostFragment()).addToBackStack(null).commit();
                    ConnectionLostFragment.CONN_FAIL_DEVICE_NAME = mDeviceAdapter.getDeviceName(ps);
                    Log.w(TAG, mDeviceAdapter.getDeviceName(ps) + " 連線已中斷!" + " 請稍候工作人員維修及安排...");
                    updateDeviceStatus(ps);
                    mDeviceAdapter.notifyDataSetChanged();
                    break;
                case CONNECTION_FAIL:
                    Toast.makeText(getApplicationContext(), "無法連線至 " + msg.getData().getString(DEVICE_NAME) + " !", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    public static void updateDeviceStatus(int position) {
        if (mDeviceAdapter.getDeviceStatus(position).equals("未連線"))
            mDeviceAdapter.setDeviceStatus("已連線", position);
        else
            mDeviceAdapter.setDeviceStatus("未連線", position);
        mDeviceAdapter.notifyDataSetChanged();
    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //消除APP標題列
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); //消除頂部狀態列(STATUS_BAR)

        // 隱藏底部系統導航列(NAVIGATION_BAR)
        // 方法1: http://stackoverflow.com/questions/21724420/how-to-hide-navigation-bar-permanently-in-android-activity
        if(Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if(Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }

        // 方法2: 原文網址: https://read01.com/PkBMkN.html
        // myView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        // myView.setFitsSystemWindows(true);

        // 使螢幕固定橫向(兩側皆可): http://stackoverflow.com/questions/11045228/set-orientation-to-landscape-in-android-on-both-sides
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
    }
}
