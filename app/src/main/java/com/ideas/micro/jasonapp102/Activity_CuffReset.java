package com.ideas.micro.jasonapp102;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ideas.micro.jasonapp102.database.AppDatabase;
import com.ideas.micro.jasonapp102.database.MDJsonRecord;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Activity_CuffReset extends AppCompatActivity {
    private final String TAG = "計數歸零";
    private String mDeviceName, mDeviceAddress;
    private TextView txt_cuffcount;        // 顯示使用次數
    private Button btn_cuffreset;
    private Utility_ActivityAlert activityAlert;
    private static final int LOCATION_REQUEST_CODE = 990;
    private static final int REQUEST_ENABLE_BT = 1028;
    private byte[] askBattery = new byte[]{(byte) 0x6C, (byte) 0xA3,  (byte) 0x00,  (byte) 0xCF};
    private byte[] askCuffCount  = new byte[] { (byte) 0x6C, (byte) 0xA7,  (byte) 0x00,  (byte) 0xCB}; // "(0x) 6C-A7-00-CB" sent (APP Send CuffCount Request to BPM，需求計數)
    private byte[] clearCuffCount = new byte[]{ (byte) 0x6C, (byte) 0xA8,  (byte) 0x00,  (byte) 0xC4}; // "{"(0x) 6C-A8-00-C4" sent (APP Send CuffClear Request to BPM，清除計數)}
    private byte[] askCustomerSN = new byte[]{(byte) 0x6C, (byte) 0xA9,  (byte) 0x00,  (byte) 0xC6};
    private byte[] setCustomerSN = new byte[]{(byte) 0x6C, (byte) 0xAA,  (byte) 0x06};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuffreset);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.activity_cuffreset_title);
        txt_cuffcount = (TextView) findViewById(R.id.txt_cuffcount);
        btn_cuffreset = (Button) findViewById(R.id.btn_cuffreset);
        SharedPreferences msharedPreferences = getSharedPreferences("meridianSharedPreferences", MODE_PRIVATE);
//        mDeviceName = GlobalVariables.Device_Name;
//        mDeviceAddress = GlobalVariables.Device_Address;
        mDeviceName = msharedPreferences.getString("btdevicename", "");
        mDeviceAddress = msharedPreferences.getString("btdeviceaddress", "");
        Log.e(TAG, "SharedPreference : btname = " + mDeviceName + " ;  btaddress = " + mDeviceAddress);
        Log.e(TAG, "APP 創建 onCreate");
        activityAlert = new Utility_ActivityAlert(this);
        int resultint = initializeBT();                           // BT 初始化
        if (resultint > 0) {    // 藍芽管理員或藍芽適配器建立錯誤 終止Activity
            activityAlert.showAlertDialog(resultint, R.string.dialog_OK, activityAlert.finishActivity);
        }
        if (ContextCompat.checkSelfPermission(Activity_CuffReset.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            // AppConstant.LOCATION_REQUEST_CODE 為自己隨意定義的 int（例如：999）
        }

        // Button  歸零
        btn_cuffreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 是否要將壓脈帶計數歸零
                        activityAlert.showAlertDialog(R.string.dialog_msg_askcuffreset,
                                R.string.dialog_cuffreset,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        doWriteCommandToF5(clearCuffCount);  // 歸零
//                                        doWriteCommandToF5(askBattery);
                                    }
                                },
                                R.string.dialog_Cancel, activityAlert.doNothing);
                    }
                });
            }
        });
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
                return;
            }
            // other 'case' lines to check for other permissions this app might request
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "APP 恢復 onResume -> 準備連線藍芽");
        if (!mBluetoothAdapter.isEnabled()) {
            Log.e(TAG, "藍芽沒有開啟");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        if (mBTConnectionState == STATE_DISCONNECTED) {         // 沒有連線時 就連線
            final boolean result = connect2BTDevice(mDeviceAddress);
            Log.e(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        Log.e(TAG, "onActivityResult");
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_CANCELED) {
                    activityAlert.showAlertDialog(R.string.dialog_msg_notenabledluetooth, R.string.dialog_OK, activityAlert.finishActivity);
                    return;
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "APP 銷毀 onDestroy");
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        super.onDestroy();
    }

    // ******  藍芽Gatt回調
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private int mBTConnectionState = STATE_DISCONNECTED;
    private byte[] batteryStaus = new byte[]{(byte) 0x6C, (byte) 0xA3, (byte) 0x00, (byte) 0x00};
    private boolean writable = true;

    private boolean writeCommandToBLE(byte[] value){
        Log.e(TAG, "writeCommand" + byteArrayToHexStr(value));
        if (mBluetoothGatt == null) {
            Log.e(TAG, "write Command : mBluetoothGatt null");
            activityAlert.showAlertDialog("Bluetooth Disconnected.", R.string.dialog_OK, activityAlert.doNothing);
            Log.e(TAG, "lost connection");
            return false;
        }
        BluetoothGattService Service = mBluetoothGatt
                .getService(UUID.fromString(BLEGattAttributes.HLIFE_COMMUNICATION_SERVICE));
        if (Service == null) {
            Log.e(TAG, "write Command : service null");
            activityAlert.showAlertDialog("Bluetooth Service NOT Found.", R.string.dialog_OK, activityAlert.doNothing);
            Log.e(TAG, "service not found!");
            return false;
        }
        BluetoothGattCharacteristic charac = Service
                .getCharacteristic(UUID.fromString(BLEGattAttributes.HLIFE_WRITE_CHARACTERISTIC_SERVICE));
        if (charac == null) {
            Log.e(TAG, "write Command : writeChar null");
            activityAlert.showAlertDialog("Bluetooth WRITE Characteristic NOT Found.", R.string.dialog_OK, activityAlert.doNothing);
            Log.e(TAG, "char not found!");
            return false;
        }
//        byte[] value = "e04fd020ea3a6910a2d808002b30309d".getBytes();
//        byte[] value = new byte[1];
//        value[0] = (byte) (21 & 0xFF);
        Log.e(TAG, "setvalue " + byteArrayToHexStr(value));
        charac.setValue(value);
        boolean status = mBluetoothGatt.writeCharacteristic(charac);
        Log.e(TAG, "writeCharacteristic = " + status);
        return status;
    }

    private void doWriteCommandToF5(byte[] command){
        List<BluetoothGattService> gattServices = mBluetoothGatt.getServices();   // gatt 發現到的藍芽服務集合
        for (BluetoothGattService gattService : gattServices) {
            if (gattService.getUuid().toString().equals(BLEGattAttributes.HLIFE_COMMUNICATION_SERVICE)) {
                //  如果 gattService 服務UUID = COMMUNICATION_SERVICE
                for (BluetoothGattCharacteristic characteristic : gattService.getCharacteristics()) {
                    // 檢查服務底下的特徵 尋求 F5
                    if (characteristic.getUuid().toString().equals(BLEGattAttributes.HLIFE_WRITE_CHARACTERISTIC_SERVICE)) {
                        characteristic.setValue(command);
                        mBluetoothGatt.writeCharacteristic(characteristic);
                    }
                }
            }
        }
    }

    // 藍芽服務運作過程中的回調函式
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mBTConnectionState = STATE_CONNECTED;       // 連接到GATT服務
                mBluetoothGatt.discoverServices();      // -> onServiceDiscover
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {           // 斷線
                mBTConnectionState = STATE_DISCONNECTED;    // 從GATT服務斷線
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 如果是脈診儀因為某些因素斷線，底下的作業有效
                        // 但是如果是因為 onDestroy 而斷線，則會造成當機
//                            activityAlert .showAlertDialog(R.string.dialog_msg_bpmbluetoothondisconnected,
//                                    R.string.dialog_OK, activityAlert.finishActivity );
                    }
                });
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS){
                // 搜尋特徵
                boolean isBPM = true;
                List<BluetoothGattService> gattServices = gatt.getServices();   // gatt 發現到的藍芽服務集合
                Log.e(TAG, "mBluetoothGatt 找到 " + gattServices.size() + " 項服務");
                for (BluetoothGattService gattservice : gattServices) {
                    Log.e(TAG, "服務 UUID =  " + gattservice.getUuid().toString() + " 服務");
                    List<BluetoothGattCharacteristic> gattCharacteristics = gattservice.getCharacteristics();
                    Log.e(TAG, "此項服務有 " + gattCharacteristics.size() + " 項特徵");
                    int charindex = 1;
                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                        Log.e(TAG, "特徵 " + charindex + " UUID = " + gattCharacteristic.getUuid().toString());
                        List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
                        for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
                            Log.e(TAG, "----描述 UUID = " + gattDescriptor.getUuid().toString());
                        }
                        charindex++;
                    }
                }
                doWriteDescription(gatt, BLEGattAttributes.HLIFE_READ_CHARACTERISTIC_SERVICE_F4);
            } else {        // NOT  (status == BluetoothGatt.GATT_SUCCESS)

            }
        }

        private void doWriteDescription(BluetoothGatt gatt, String charuuid) {
            Log.e(TAG, "doWriteDescription UUID " + charuuid);
            List<BluetoothGattService> gattServices = gatt.getServices();   // gatt 發現到的藍芽服務集合
            for (BluetoothGattService gattService : gattServices) {
                if (gattService.getUuid().toString().equals(BLEGattAttributes.HLIFE_COMMUNICATION_SERVICE)) {
                    //  如果 gattService 服務UUID = COMMUNICATION_SERVICE
                    for (BluetoothGattCharacteristic characteristic : gattService.getCharacteristics()) {
                        // 檢查服務底下的特徵
                        if (characteristic.getUuid().toString().equals(charuuid)) {
                            Log.e(TAG, "setCharacteristicNotification " + charuuid);
                            mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                            List<BluetoothGattDescriptor> deslist = characteristic.getDescriptors();
                            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                                    UUID.fromString(BLEGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            mBluetoothGatt.writeDescriptor(descriptor);
                        }
                    }
                }
            }
        }

        // 藍芽有得到資料時
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            final byte[] data = characteristic.getValue();
            String responsestr = byteArrayToHexStr(data);
            if (data != null && data.length > 0) {
                Log.e(TAG, "壓脈帶計數：Hex " + byteArrayToHexStr(data));
            } else {
                Log.e(TAG, "沒有資料");
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (responsestr.substring(0,4)) {
                        case "33A3":        // 查詢電量
                                if (responsestr.substring(6, 8).equals("00")){
                                    Log.e(TAG, "電池電量正常");
                                    activityAlert.showAlertDialog("電池電量正常", R.string.dialog_OK, activityAlert.doNothing);
                                } else if (responsestr.substring(6, 8).equals("01")){
                                    Log.e(TAG, "電池電量不足");
                                    activityAlert.showAlertDialog("電池電量不足", R.string.dialog_OK, activityAlert.doNothing);
                                }
                            break;
                        case "3F41":
                            // "(0x) 3F-41-08-33-A7-04-12-34-56-78-98-76" received
                            // "(0x) 3F-41-04-33-A8-00-9B-7A" received has been cleared
                            if (responsestr.substring(6, 10).equals("33A7")){
                                int countint = (int)  (((data[6] & 0xFF) << 24) | ((data[7] & 0xFF) << 16) | ((data[8] & 0xFF) << 8) | (data[9] & 0xFF));
                                txt_cuffcount.setText(String.format("%04d", countint));         // 四位整數 前方補 0
                            } else if (responsestr.substring(6, 10).equals("33A8")) {
                                txt_cuffcount.setText(String.format("%04d", 0));         // 四位整數 前方補 0
                            } else {
                                txt_cuffcount.setText("資料錯誤");
                            }
                            break;
                    }
                }
            });
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.e(TAG, "onCharacteristicRead..讀取特徵事件：GATT_SUCCESS" + status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicWrite..寫入特徵事件：");
            // doWriteCommandToF5 不會到這裡 onCharacteristicWrite
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.e(TAG, "onDescriptorRead..讀取特徵描述DES事件：");
        }

        private boolean isF6Written = false;        // 只處理一次
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.e(TAG, "onDescriptorWrite..寫入特徵描述DES事件：");
            super.onDescriptorWrite(gatt, descriptor, status);
            if (! isF6Written) {
                Log.e(TAG, "F4 Ready");
                doWriteDescription(gatt, BLEGattAttributes.HLIFE_READ_CHARACTERISTIC_SERVICE_F6);
//                doWriteCommandToF5(askCuffCount);
                isF6Written = true;
            } else if (isF6Written){
                Log.e(TAG, "F6 Ready");
                Log.e(TAG, "askCuffCount");
                doWriteCommandToF5(askCuffCount);
                // doWriteCommandToF5 不會再回到這裡 onDescriptorWrite
            }
        }
    };
    // end of 藍芽服務運作過程中的回調函式

    public int initializeBT() {
        // For API level 18 and above, get a reference to BluetoothAdapter through BluetoothManager.
        int resultInt = 0;
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "初始化藍牙過程中，無法建立藍芽管理員");
                resultInt = R.string.dialog_msg_bluetoothmanagerbuildfail;
                return resultInt;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "初始化藍牙過程中，無法取得藍牙適配器");
            resultInt = R.string.dialog_msg_bluetoothadapterbuildfail;
            return resultInt;
        } else if (! mBluetoothAdapter.isEnabled()) {
            Log.e(TAG, "開啟藍芽");
            mBluetoothAdapter.enable();
        }
        return resultInt;
    }

    public boolean connect2BTDevice(final String address) {     // at onResume
        if (mBluetoothAdapter == null || address == null) {
            Log.e(TAG, "準備藍芽連線時，發現藍牙適配器尚未初始化或是未指定裝置位址");
            return false;
        }
        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.e(TAG, "嘗試使用現有的藍牙GATT連接");
            if (mBluetoothGatt.connect()) {
                mBTConnectionState = STATE_CONNECTING;
                return true;
            } else {

                return false;
            }
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.e(TAG, "未發現裝置，無法連接");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.e(TAG, "onResume > connect2BTdevice 嘗試建立新的連接");
        mBluetoothDeviceAddress = address;
        mBTConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect2BTDevice() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "中斷連線過程中，藍牙適配器尚未初始化");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    private  String byteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        StringBuilder hex = new StringBuilder(byteArray.length * 2);
        for (byte aData : byteArray) {
            hex.append(String.format("%02X", aData));
        }
        String gethex = hex.toString();
        return gethex;
    }

    private byte getCheckSum(byte[] bytes){
        int bytecount = bytes.length;
        int checksum = (int)  bytes[0];
        for (int i=1; i<=bytecount - 2; i++){
            checksum ^= (int) bytes[i];
        }
        Log.e(TAG, String.format("電池狀態校驗和 = %02X", (byte) checksum));
        return (byte) (0xFF & checksum);
    }

// ***************************************************************************************************
//*****************************************************************************************************



    private void nothing(){
        // 2022/01/03
        // onStop 中 不要中斷藍芽連線 以免中斷連線觸法回調造成當機
        /* 2022-02-03
            v2 區分 v3 的方法：
            系統預設為 v3，但是v2 在充氣階段大約十幾秒不會送出資料，因此 bpmDogWatch 來檢查空白資料的時間，如果超過六秒就判斷為 v2
            v2 與 v3 使用的 actionhintjson, bpmstatusjson, bpmstagejson 分開處理
            處理資料的函式 getPulseDatav2, getPulseDatav3 也不相同。未來如果v2機器全部收回，就可以將 v2 相關內容刪除
         */
    }
}