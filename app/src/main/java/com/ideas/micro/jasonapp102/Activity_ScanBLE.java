package com.ideas.micro.jasonapp102;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class Activity_ScanBLE  extends AppCompatActivity implements onHttpPostCallback{
    private final String TAG = "藍牙掃描";
    private Activity_ScanBLE.LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private boolean mScanning = false;
    private Handler mHandler;
    private static final int REQUEST_ENABLE_BT = 1028;
    private static final int LOCATION_REQUEST_CODE = 990;
    private static final int SCAN_REQUEST_CODE = 910;
    private static final int BLECONN_REQUEST_CODE = 920;
    private static final long SCAN_PERIOD = 10000;  // Stops scanning after 10 seconds.
    private boolean listNullName = false;       // set display null name bt or not
    private boolean isBindMacFound = false;

    private TextView tvTitle;
    private Button btn_blescan;
    private ListView scanblelist;
    private String parentActivity = "";
    SharedPreferences sharedPreferences;
    Utility_ActivityAlert activityAlert;
    private SingleUser user = SingleUser.getInstance();
    private String bpmmac = "";
    private String postKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_scanble);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.activity_scanble_title);
//        Toolbar toolbar = findViewById(R.id.toolbar);
        btn_blescan = (Button) findViewById(R.id.btn_blescan);
        scanblelist = (ListView) findViewById(R.id.scanblelist);
        scanblelist.setOnItemClickListener(onItemClickListener);
        activityAlert = new Utility_ActivityAlert(this);
        bpmmac = user.getBpmMAC();
        Log.e(TAG, "user.getBpmMAC = " + bpmmac);
        mHandler = new Handler();

        // 1. 必須要將手機的GPS定位開啟
        // 2. 設定 > 安全性與位置資訊 > 定位 > 應用程式層級權限 > 開啟此應用程式權限
        // 檢查相機授權
//        if (ContextCompat.checkSelfPermission(Activity_ScanBLE.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(Activity_ScanBLE.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GPS_REQUEST_CODE);
//        }
//        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

//        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
//        }else{
//            Intent callGPSSettingIntent = new Intent(
//                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            startActivity(callGPSSettingIntent);
//            Utility.showAlert(Activity_ScanBLE.this, "GPS 關閉");
//        }

        // 檢查APP裝置是否有支援BLE ?
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // APP裝置不支援BLE  即將關閉APP
            activityAlert.showAlertDialog(R.string.dialog_msg_notsupportbluetooth, R.string.dialog_OK, activityAlert.finishActivity);
        } else {
            //如果APP裝置支援BLE -> 獲取藍牙適配器。
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
                // BluetoothAdapter through BluetoothManager.
                final BluetoothManager bluetoothManager =
                        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                // 藍牙適配器
                mBluetoothAdapter = bluetoothManager.getAdapter();
            } else {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            }

            Log.e(TAG, "onCreate : 藍芽開啟 ? = " + mBluetoothAdapter.isEnabled());
            if (mBluetoothAdapter == null) {
                // 如果無法設定適配器，就關閉
                activityAlert.showAlertDialog(R.string.dialog_msg_notsupportbluetooth, R.string.dialog_OK, activityAlert.finishActivity);
//            }  else if (! mBluetoothAdapter.isEnabled()) {
//                Log.e(TAG, "開啟藍芽");
//                mBluetoothAdapter.enable();
            }else {
                // 設定藍牙掃描物件
                mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            }
        }

        if (ContextCompat.checkSelfPermission(Activity_ScanBLE.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            // AppConstant.LOCATION_REQUEST_CODE 為自己隨意定義的 int（例如：999）
        }

        if (ContextCompat.checkSelfPermission(Activity_ScanBLE.this, Manifest.permission.BLUETOOTH_SCAN) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, SCAN_REQUEST_CODE);
        }

        if (ContextCompat.checkSelfPermission(Activity_ScanBLE.this, Manifest.permission.BLUETOOTH_CONNECT) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, BLECONN_REQUEST_CODE);
        }

        btn_blescan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (! mScanning) {
                    //沒有掃描就清除原有列表然後開啟
                    mLeDeviceListAdapter.clear();
                    scanLeDevice(true);
                } else {
                    //  正在掃描時，就關閉掃瞄作業，
                    scanLeDevice(false);
                }
            }
        });
    }       // end of  onCreate

    protected void onStart() {
        super.onStart();
//        LogInOut.log("Activity_ScanBLE", true);

    }

    @Override
    protected void onStop() {
        super.onStop();
        LogInOut.log("Activity_ScanBLE", false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {    // 拒絕GPS授權  系統提示後關閉
                    activityAlert.showAlertDialog(R.string.dialog_msg_bluetoothscanneedgps, R.string.dialog_OK, activityAlert.finishActivity);
                }
            }
            break;
            case SCAN_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {    // 拒絕GPS授權  系統提示後關閉
                    activityAlert.showAlertDialog(R.string.dialog_msg_bluetoothscanneedpermission, R.string.dialog_OK, activityAlert.finishActivity);
                }
            }
            break;
            case BLECONN_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {    // 拒絕GPS授權  系統提示後關閉
                    activityAlert.showAlertDialog(R.string.dialog_msg_bluetoothconnneedpermission, R.string.dialog_OK, activityAlert.finishActivity);
                }
            }
            break;
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        //setListAdapter(mLeDeviceListAdapter);
        scanblelist.setAdapter(mLeDeviceListAdapter);
        if (!mBluetoothAdapter.isEnabled()) {
            Log.e(TAG, "藍芽沒有開啟");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        Log.e(TAG, "onActivityResult");
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_CANCELED) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.dialog_msg_notenabledluetooth)
                            .setTitle(R.string.dialog_systemcomment)
                            .setPositiveButton(R.string.dialog_OK, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                    finish();
                    return;
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
            Intent intent = null;
            if (device == null) return;
            // 量測準備不區分醫護及用戶差異，若有需要則在量測準備中以 GlobalVariable.Login_Role 檢查
            GlobalVariables.Device_Address = device.getAddress();
            GlobalVariables.Device_Name = device.getName();
            sharedPreferences = getSharedPreferences("meridianSharedPreferences", MODE_PRIVATE);
            sharedPreferences.edit()
                    .putString("btdeviceaddress", device.getAddress())
                    .putString("btdevicename", device.getName())
                    .commit();
            Log.e(TAG, "選取 MAC " + GlobalVariables.Device_Address);
            getDeviceType(GlobalVariables.Device_Address);
            if (mScanning) {
                scanLeDevice(false);
            }
            Log.e(TAG, "選取裝置 : " + device.getName());

        }
    };

    // 到資料庫找裝置版次
    private void getDeviceType(String mac){
        JSONObject jqljson = new JSONObject();
        try {
            jqljson.put("command",  "GetDeviceTypeFromID");
            jqljson.put("clinicnamewi", "RootDB");
            jqljson.put("mac", mac);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        postKey = "GetDeviceTypeFromID";
        HttpPost httpPost = new HttpPost(Activity_ScanBLE.this); // 記得要宣告  implements onHttpPostCallback
        httpPost.startPost(Activity_ScanBLE.this, GlobalVariables.http_url, jqljson.toString());
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            Log.e(TAG, "搜尋作業將要啟動");
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    btn_blescan.setText(R.string.btn_scanbleon);
                    Log.e(TAG, "搜尋作業將要自動關閉");
                    mBluetoothLeScanner.stopScan(mscanCallback);
                    if (mLeDeviceListAdapter.mLeDevices.size() == 0) {
                        activityAlert.showAlertDialog(R.string.dialog_msg_nobledevicediscovered, R.string.dialog_OK, activityAlert.doNothing);
                    }
                }
            }, SCAN_PERIOD);        // 十秒鐘以後關閉
            mScanning = true;
            btn_blescan.setText(R.string.btn_scanbleoff);
            mBluetoothLeScanner.startScan(mscanCallback);
        } else {
            Log.e(TAG, "手動關閉搜尋作業");
            mScanning = false;
            btn_blescan.setText(R.string.btn_scanbleon);
            mBluetoothLeScanner.stopScan(mscanCallback);
            if (mLeDeviceListAdapter.mLeDevices.size() == 0) {
                activityAlert.showAlertDialog(R.string.dialog_msg_nobledevicediscovered, R.string.dialog_OK, activityAlert.doNothing);
            }
        }
    }

    @Override
    public void onComplete(String response) {
        try {
            if (postKey.equals("GetDeviceTypeFromID")) {
                final JSONObject responseJson = new JSONObject(response);
                Log.e(TAG, response);
                if (responseJson.getString("status").equals("success")) {      //
                    GlobalVariables.Device_Type = responseJson.getJSONArray("dblist").getJSONObject(0).getString("bpmType");
                    Log.e(TAG, GlobalVariables.Device_Type);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = null;
                        Intent getintent = getIntent();
                        if (getintent.getStringExtra("nextstep").equals("bpmprepare")) {
                            intent = new Intent(Activity_ScanBLE.this, Activity_BPMprepare.class);
                        } else if (getintent.getStringExtra("nextstep").equals("cuffset")) {
                            intent = new Intent(Activity_ScanBLE.this, Activity_CuffReset.class);
                        } else if (getintent.getStringExtra("nextstep").equals("customersn")) {
                            intent = new Intent(Activity_ScanBLE.this, Activity_CustomerSN.class);
                        }
                        startActivity(intent);
                    }
                });
            }
        } catch (JSONException e){

        }
    }

    @Override
    public void onFail(String err) {

    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = Activity_ScanBLE.this.getLayoutInflater();
        }
        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }
        @Override
        public int getCount() {
            return mLeDevices.size();
        }
        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }
        @Override
        public long getItemId(int i) {
            return i;
        }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewAdapter_Scanble.ViewHolder viewHolder;
            //    static class ViewHolder {
            //        TextView deviceName;
            //        TextView deviceAddress;
            //    }
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listview_scanble, null);
                viewHolder = new ViewAdapter_Scanble.ViewHolder();
                viewHolder.scan_deviceAddress = (TextView) view.findViewById(R.id.scan_deviceAddress);
                viewHolder.scan_deviceName = (TextView) view.findViewById(R.id.scan_deviceName);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewAdapter_Scanble.ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            viewHolder.scan_deviceName.setText(getResources().getString(R.string.label_device_name) + " : " + deviceName);
            viewHolder.scan_deviceAddress.setText(getResources().getString(R.string.label_device_address) + " : " + device.getAddress());
            return view;
        }
    }      // end of class LeDeviceListAdapter

    private ScanCallback mscanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            byte[] resultbytes = result.getScanRecord().getBytes();
            Log.e(TAG, "onScanResult result = " + result.getScanRecord().toString());
            Log.e(TAG, "onScanResult address = " + result.getDevice().getAddress());
            Log.e(TAG, "onScanResult name = " + result.getDevice().getName());
            final BluetoothDevice device = result.getDevice();
            Log.e(TAG, " GlobalVariables.isBindMAC " + GlobalVariables.isBindMAC);
            if (GlobalVariables.allowedBLE.contains(device.getName())) {
                if (GlobalVariables.Login_Role.equals("A") || device.getAddress().equals(bpmmac)) {    // RoleA 或是  有綁定的 M
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (listNullName || device.getName() != null) {
                                mLeDeviceListAdapter.addDevice(device);
                                mLeDeviceListAdapter.notifyDataSetChanged();
                                scanblelist.setAdapter(mLeDeviceListAdapter);
                            }
                        }
                    });
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            AlertDialog.Builder builder = new AlertDialog.Builder(Activity_ScanBLE.this);
            builder.setMessage(R.string.dialog_msg_lescanfailed)
                    .setTitle(R.string.dialog_systemcomment)
                    .setPositiveButton(R.string.dialog_OK, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    };      // end of mscanCallBack

}