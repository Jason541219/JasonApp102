package com.ideas.micro.jasonapp102;

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

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

public class Activity_BPM_Btbpm extends AppCompatActivity implements onHttpPostCallback, View.OnClickListener {
    private final String TAG = "合世_血壓計";
    private String mDeviceName, mDeviceAddress;
    private String deviceType = "heshi_bt";       // 舊版 v2,  新版 v3
    private String device_ver = "";
    private TextView text_bpmdevicename;        // 顯示BLE DeviceName
    private TextView text_bpmdevicestatus;      // 顯示BLE狀態
    private TextView text_bpmdata;                      // 顯示BLE 數據
    private TextView text_sbpvalue, text_dbpvalue,  text_hrvalue, text_ihbvalue;
    private String sbp_name, dbp_name, hr_name, ihb_name;
    private TextView text_bpwavesummary, text_pulse1summary, text_pulse2summary;           // 顯示資料數據
    private TextView text_actionhint;
    private TextView text_progressupload, text_progressanalysis;
    private ProgressBar progressBar_bpmupload, progressBar_bpmanalysis;
    private Button btn_bpmupload, btn_bpmstopmeasue;
    private int position, posture, oxygen, memberid, age;
    private String memname, gender;
    private String encoded64 = "";
    private String postKey;
    private DrawBPCurveView drawView;
    private long localrecordID;


    private JSONArray pulsewave1 = new JSONArray();      // 脈診波一
    private JSONArray pulsewave2 = new JSONArray();      // 脈診波二
    private JSONArray bpmmHg = new JSONArray();          //  血壓波 mmHg
    private JSONArray bpinflat = new JSONArray();
    private JSONArray pulseinflat = new JSONArray();
    private JSONArray bpwave = new JSONArray();              // 血壓波 waveform

    private JSONObject uploadBPWaveData = new JSONObject();

    private boolean isUploading = false;                // 資料正在上傳，禁止再次上傳
    private boolean isMeasureing = false;
    private boolean isReadyUpload = false;
    private boolean isActivityAlive = true;
    private boolean isDrawing = false;
    public static int pulsedata = 0;
    private int bpmin = Integer.MAX_VALUE, bpmax = 0, bpdif = 1;
    private int p1min =  Integer.MAX_VALUE, p1max = 0, p1dif = 1;
    private int p2min =  Integer.MAX_VALUE, p2max = 0, p2dif = 1;
    private int bpdatacount = 0, p1datacount = 0, p2datacount = 0;   //  用作累加藍芽資料吐出量，檢查資料丟失的比例
    private Utility_ActivityAlert activityAlert;

    private static final int LOCATION_REQUEST_CODE = 990;
    private static final int REQUEST_ENABLE_BT = 1028;
    private JSONObject actionhintjson = new JSONObject();
    private JSONObject bpmstatusjson = new JSONObject();
    private JSONObject bpmstagejson = new JSONObject();
    private Helper_BPM_Heshi helper = new Helper_BPM_Heshi();
    // BT 指令
    private byte[] askFW = new byte[]{(byte) 0x6C, (byte) 0x56, (byte) 0x00, (byte) 0x3A};
    private byte[] askBattery = new byte[]{(byte) 0x6C, (byte) 0xA3, (byte) 0x00, (byte) 0xCF};
    private byte[] askCuffCount = new byte[]{(byte) 0x6C, (byte) 0xA7, (byte) 0x00, (byte) 0xCB}; // "(0x) 6C-A7-00-CB" sent (APP Send CuffCount Request to BPM，需求計數)
    private byte[] clearCuffCount = new byte[]{(byte) 0x6C, (byte) 0xA8, (byte) 0x00, (byte) 0xC4}; // "{"(0x) 6C-A8-00-C4" sent (APP Send CuffClear Request to BPM，清除計數)}
    private String bleCommand = "measureing";
    private String deviceFW = "NA";
    private String cuffCount = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "Activity_BPM_Btbpm APP 創建 onCreate");
        getDeviceIntent();              // **** 取得藍芽設備Intent
        setUIComponent();           // **** 設定UI
        setDrawLayout();                    // **** 設定繪圖區
        actionhintjson = Utility.setActionJSON(device_ver);      // new JSONObject();   // 提示
        bpmstatusjson = Utility.setBpmStatusJSON(device_ver); // new JSONObject();    // 狀態
        bpmstagejson = Utility.setBpmStageJSON(device_ver); // new JSONObject();     //  階段

        activityAlert = new Utility_ActivityAlert(this);
        int resultint = initializeBT();                           // BT 初始化
        if (resultint > 0) {    // 藍芽管理員或藍芽適配器建立錯誤 終止Activity
            activityAlert.showAlertDialog(resultint, R.string.dialog_OK, activityAlert.finishActivity);
        }

        if (ContextCompat.checkSelfPermission(Activity_BPM_Btbpm.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            // AppConstant.LOCATION_REQUEST_CODE 為自己隨意定義的 int（例如：999）
        }

        // Button  資料上傳 implements View.OnClickListener
        btn_bpmupload.setOnClickListener(this);
        // Button  停止量測並詢問 implements View.OnClickListener
        btn_bpmstopmeasue.setOnClickListener(this);

    }   //  end of onCreate


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
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_bpmupload:
//                if (isReadyUpload) askUploadData(); // 取消此項功能。資料上傳 排除賓客
                break;
            case R.id.btn_bpmstopmeasue:
//                askStopmeasure();                                         // 取消此項功能。詢問是否停止量測
                break;
        }
    }

    @Override
    public void onBackPressed() {
        askStopmeasure();    // 詢問是否停止量測
    }

    // 詢問是否停止量測
    private void askStopmeasure(){
        if (isMeasureing) {                             // 正在量測中  需要詢問
            activityAlert.showAlertDialog(R.string.dialog_msg_measurehasbeenstopped,
                    R.string.dialog_measurestop_yes, ready2stopmeasure_exit,
                    R.string.dialog_measurestop_no, activityAlert.doNothing);
        } else {
            dogWatchingFlag = false;    // 關閉監聽
            isMeasureing = false;
            finish();       // 沒有量測就直接關閉   不需要訊問
        }
    }

    // 準備停止量測
    private DialogInterface.OnClickListener ready2stopmeasure_exit = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dogWatchingFlag = false;    // 關閉監聽
            isMeasureing = false;
            mBluetoothGatt.close();
            mBluetoothGatt = null;
            finish();       // 關閉
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "APP 開始 onStart... 開始繪圖執行緒");
        pulsemode = -1;        pulsephase = -1;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isMeasureing = true;                // 準備開始量測
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
        isActivityAlive = false;
        LogInOut.log("Activity_BPM", false);
        Log.e(TAG, "APP 暫停 onStop");
//        if (mBTConnectionState != STATE_DISCONNECTED) disconnect2BTDevice();
        bpwave = new JSONArray();
        pulsewave1 = new JSONArray();
        pulsewave2 = new JSONArray();
        encoded64 = "";     // 刪除頭像內容
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "APP 銷毀 onDestroy");
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        super.onDestroy();
    }

    // 更新藍芽連線狀態
    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text_bpmdevicestatus.setText(resourceId);
                if (resourceId == R.string.connected) {     // 藍芽已連接
                    text_actionhint.setText(R.string.actionhint_pressStart);
                }
                // 以下這一段 在按下重新量測按鍵時會當機 可能是在onStop 的時候觸發這段 可是 activity 已經關閉
                else if (resourceId == R.string.disconnected){     // 藍芽已斷線
                    text_actionhint.setText(R.string.dialog_msg_bpmbluetoothondisconnected);
                    activityAlert .showAlertDialog(R.string.dialog_msg_bpmbluetoothondisconnected,
                            R.string.dialog_OK, activityAlert.finishActivity );
                }
            }
        });
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

    // 藍芽服務運作過程中的回調函式
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mBTConnectionState = STATE_CONNECTED;       // 連接到GATT服務
                updateConnectionState(R.string.connected);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        text_bpmdevicestatus.setText(R.string.connected);
                        text_actionhint.setText(R.string.actionhint_pressStart);
                        bleCommand = "measureing";
                    }
                });
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {           // 斷線
                mBTConnectionState = STATE_DISCONNECTED;    // 從GATT服務斷線
                updateConnectionState(R.string.disconnected);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        text_bpmdevicestatus.setText(R.string.disconnected);
                        text_actionhint.setText(R.string.dialog_msg_bpmbluetoothondisconnected);
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
            List<BluetoothGattService> gattServices = gatt.getServices();   // gatt 發現到的藍芽服務集合
            for (BluetoothGattService gattService : gattServices) {
                if (gattService.getUuid().toString().equals(BLEGattAttributes.HLIFE_COMMUNICATION_SERVICE)) {
                    //  如果 gattService 服務UUID = COMMUNICATION_SERVICE
                    for (BluetoothGattCharacteristic characteristic : gattService.getCharacteristics()) {
                        // 檢查服務底下的特徵
                        if (characteristic.getUuid().toString().equals(charuuid)) {
                            // 如果特徵 UUID = READ_CHARACTERISTIC_SERVICE4
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
            if (data != null && data.length > 0) {
                Log.e(TAG, "數據：Hex " + byteArrayToHexStr(data));
                switch (bleCommand){
                    case "measureing":
                        getPulseData(data);
                        break;
                    case "askFW":
                        Log.e(TAG, "data.length = " + data.length);
                        if (data.length == 14) {
                            responseFWversion(data);
                            bleCommand = "measureing";
                        }
                        break;
                    case "askCuffCount":
                        responseCuffCount(data);
                        bleCommand = "measureing";
                        break;
                    case "askBattery":
                        responseBatteryStatus(data);
                        bleCommand = "measureing";
                        break;
                }
            } else {
                Log.e(TAG, "沒有資料");
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.e(TAG, "onCharacteristicRead..讀取特徵事件：GATT_SUCCESS" + status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicWrite..寫入特徵事件：");
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.e(TAG, "onDescriptorRead..讀取特徵描述事件：");
        }

        private boolean isF6Written = false;        // 只處理一次
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if (! isF6Written) {
                doWriteDescription(gatt, BLEGattAttributes.HLIFE_READ_CHARACTERISTIC_SERVICE_F6);
                isF6Written = true;
            }
        }
    };
    // end of 藍芽服務運作過程中的回調函式

    // 寫執行指令
    private void doWriteCommandToF5(byte[] command) {
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
        Log.e(TAG, "嘗試建立新的連接");
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

    // 取得脈波數據
    private int pulsemode = 2;
    private int pulsephase = 3;
    private int pre_pulsemode = 0;
    private int pre_pulsephase = 0;
    private int sbp = 0, dbp = 0, hr = 0, ihb = 0, cuffcount = 0, errvalue = 0, errcode = 0;
    private int adc0mmhg = 0, adcpermmhg = 0, adc0mmhgoffset = 0;
    private int ymin1 = 0, ymax1 = 0, ymin2 = 0, ymax2=0;
    private String bpm_stage = "00";

    private boolean dogWatchingFlag = false;
    private boolean watchFlag = false;      // 監看旗標
    // 血壓器監看
    private Runnable bpmWatchDog = new Runnable() {
        @Override
        public void run() {
            String pulsestate = "";
            int watchcount = 0;
            Log.e(TAG, "開啟監看模式");
            while (dogWatchingFlag) {
                pulsestate = "" + pulsemode  + pulsephase;
                Log.e(TAG, "WatchDog pulsestate = " + pulsestate);
                try {
                    Thread.sleep(2000);     //每個兩秒檢查一次
                    if (watchFlag){
                        watchFlag = false;      // 正常情況下，Flag 會被血壓計設為 True,  此時撤銷Flag 等待再被血壓計設定
                        watchcount = 0;
                    } else {
                        Log.e(TAG, "資料沒有更新 " + watchcount + "次，顯示對話框");
                        watchcount++;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.e(TAG, "關閉監看模式");
        }
    };

    private void setActionHint(int resid){
        final int res_id = resid;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text_actionhint.setText(res_id);
            }
        });
    }

    // 詢問是否要上傳資料
    private void askUploadData(){
        if (GlobalVariables.Login_Role.equals("M")) {
            SingleUser user = SingleUser.getInstance();
            if (user.getUseraccount().equals("guest")) {     // 賓客的資料不能上傳
                activityAlert.showAlertDialog(R.string.dialog_msg_guestnoupload, R.string.dialog_OK, activityAlert.doNothing);
                return;
            } else {    // 不是賓客，提示
                activityAlert.showAlertDialog(R.string.dialog_msg_bpmmeasurecomplete, R.string.dialog_confirmupload,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                uploadwave();
                            }
                        },
                        R.string.dialog_notupload, activityAlert.doNothing);
            }
        } else {    // GlobalVariables.Login_Role = A
            uploadwave();
        }
    }

    // **** 繪圖類別區域 **** //
    LinearLayout layout;
    private JSONArray wavedata = new JSONArray();
    private int wavetoshow;
    //    Canvas canvas;
    private int timeelapsed = 0;    // 開始量測後的時間
    private int timestep = 100;      // 心電圖更新時間 100ms
    private int Ymin, Ymax, Xmin, Xmax;
    private int isWaving = -1;

    // 設定繪圖布局
    private void setDrawLayout() {
        layout = (LinearLayout) findViewById(R.id.drawhlifelayout);
        drawView = new DrawBPCurveView(Activity_BPM_Btbpm.this, 8000000, 8500000, 0, 1220, ismmHg_Y, mmHg_L, mmHg_H);
        drawView.setMinimumHeight(500);
        drawView.setMinimumWidth(300);
        drawView.invalidate();
        layout.addView(drawView);
    }

    // 設定UI元件
    private void setUIComponent() {
        setContentView(R.layout.activity_bpm_btbpm);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.activity_bpm_title);
        text_bpmdevicename = (TextView) findViewById(R.id.text_bpmdevicename);
        text_bpmdevicename.setText(memname);        // 受測者姓名
        text_bpmdevicestatus = (TextView) findViewById((R.id.text_bpmdevicestatus));
        text_bpmdata = (TextView) findViewById(R.id.text_bpmdata);
        text_sbpvalue = (TextView) findViewById(R.id.label_sbp);
        text_dbpvalue = (TextView) findViewById(R.id.label_dbp);
        text_hrvalue = (TextView) findViewById(R.id.label_hr);
        text_ihbvalue = (TextView) findViewById(R.id.label_ihb);
        text_actionhint = (TextView) findViewById(R.id.text_actionhint);
        text_progressanalysis = (TextView) findViewById(R.id.text_progressanalysis);
        text_progressupload = (TextView) findViewById(R.id.text_progressupload);
        progressBar_bpmupload = (ProgressBar) findViewById(R.id.progressBar_bpmupload);
        progressBar_bpmanalysis = (ProgressBar) findViewById(R.id.progressBar_bpmanalysis);
        progressBar_bpmupload.setVisibility(View.GONE);
        progressBar_bpmanalysis.setVisibility(View.GONE);
        text_progressupload.setVisibility(View.GONE);
        text_progressanalysis.setVisibility(View.GONE);
        sbp_name = getResources().getString(R.string.label_sbp);
        dbp_name = getResources().getString(R.string.label_dbp);
        hr_name = getResources().getString(R.string.label_hr);
        ihb_name = getResources().getString(R.string.label_ihb);
        text_sbpvalue.setText(sbp_name + "  --mmHg");
        text_dbpvalue.setText(dbp_name +  "  --mmHg");
        text_hrvalue.setText(hr_name +  "  --/min");
        text_ihbvalue.setText(ihb_name + "  --");
        text_bpwavesummary = (TextView) findViewById(R.id.text_bpwavesummary);
        text_pulse1summary = (TextView) findViewById(R.id.text_pulse1summary);
        text_pulse2summary = (TextView) findViewById(R.id.text_pulse2summary);
        btn_bpmstopmeasue = (Button) findViewById(R.id.btn_bpmstopmeasue);   // 停止量測
        btn_bpmupload = (Button) findViewById(R.id.btn_bpmupload);
        btn_bpmupload.setText(R.string.btn_bleupload);
        btn_bpmupload.setEnabled(false);            //  不啟用
        btn_bpmupload.setVisibility(View.INVISIBLE);        // 不顯示
        btn_bpmstopmeasue.setVisibility(View.INVISIBLE);        // 不顯示
        Log.e(TAG, "device_ver = " + device_ver);
        if (device_ver.equals("v2")) {
            text_ihbvalue.setVisibility(View.INVISIBLE);
        }
    }

    // 取得設備資訊 Intent
    private void getDeviceIntent() {
        final Intent intent = getIntent();
//        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
//        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        SharedPreferences msharedPreferences = getSharedPreferences("meridianSharedPreferences", MODE_PRIVATE);
//        mDeviceName = GlobalVariables.Device_Name;
//        mDeviceAddress = GlobalVariables.Device_Address;
        mDeviceName = msharedPreferences.getString("btdevicename", "");
        mDeviceAddress = msharedPreferences.getString("btdeviceaddress", "");
        Log.e(TAG, "SharedPreference : btname = " + mDeviceName + " ;  btaddress = " + mDeviceAddress);
        position = intent.getIntExtra("position", 0);
        posture = intent.getIntExtra("posture", 0);
        memberid = intent.getIntExtra("memberid", 0);
        oxygen = intent.getIntExtra("oxygen", 0);
        gender = intent.getStringExtra("gender");
        age = intent.getIntExtra("age", 0);
        encoded64 = intent.getStringExtra("avatar");
        memname = intent.getStringExtra("memname");
        device_ver = intent.getStringExtra("deviceversion");
        Log.e(TAG, "血壓計版次 = " + device_ver + " 會員名稱 = " + memname);
        Log.e(TAG, "onCreate : 名稱 " + mDeviceName + " 地址" + mDeviceAddress + " 手部" + position + " 姿勢" + posture + " 會員編號" + memberid);

    }

    // 每秒問一下分析結果
    private boolean isAnalyzing = false;        // 預設關閉分析功能
    private boolean askWaveFlagAvailable = true;        // 預設可以詢問波形結果
    private final int UPDATEPROGRESSBAR = 1000;
    Handler hd = new WaveAnalysisHandler();

    // 分析結果
    private Runnable WaveAnalysisRunnable = new Runnable (){
        @Override
        public void run() {
            isAnalyzing = true;         // 開啟分析功能
            int time_interval = 100; // ms
            int time_running = 0;
            int time_out = 10000;
            turnONprogressbar();
            while (isAnalyzing) {
                try {
                    Thread.sleep(time_interval);     // 等待 1 秒
                    time_running += time_interval;
                    if (askWaveFlagAvailable && flagCut == 1 && flagFFT == 1){
                        // 已經得到分析結果
                        time_running = time_out;        // 跳出執行續
                        analyzeFFT();
                    } else {
                        getWaveFlag();      //  flag 尚未被設定
                    }
                    askWaveFlagAvailable = false;       // 在等待Flag回應之前 不再詢問
                    Message msg = new Message();
                    msg.what = UPDATEPROGRESSBAR; // 使用者自定義的一個值，用於標識不同型別的訊息
                    msg.arg1 = time_running;
                    msg.arg2 = time_out;
                    hd.sendMessage(msg); // 傳送訊息
                    if (time_running >= time_out){   // 超過時間
                        isAnalyzing = false;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    isAnalyzing = false;        //      出現錯誤，強制停止分析
                } finally {

                }
            }
        }
    };

    // 定義一個內部類繼承自Handler，並且覆蓋handleMessage方法用於處理子執行緒傳過來的訊息
    // 顯示進度條
    private  class WaveAnalysisHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATEPROGRESSBAR: // 接受到訊息之後，對UI控制元件進行修改
                    int progress = (int) msg.arg1 * 100 / msg.arg2;
                    int progressmax = progressBar_bpmanalysis.getMax();
                    if (progress < progressmax) { // 小於100 表示正在運作
                        progressBar_bpmanalysis.setProgress(progress);
                    } else {
                        turnOFFprogressbar();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    // 檢查資料分析
    private int flagCut =0;
    private int flagFFT = 0;
    private long flagID = 0;
    private String fftData = "";

    private void analyzeFFT(){      // 解析 fftData
        try {
            JSONObject fft_json = new JSONObject(fftData);
            float[] fft_ampv = new float[11];
            float[] fft_ampv_limit = new float[]  {5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 10.0f, 10.0f, 10.0f, 10.0f, 10.0f, 10.0f};
            int redCount = 0;
            int fft_jsonsection = fft_json.names().length();    // 總共有幾個元素
            Log.e(TAG, "最後一個 Section = " + fft_json.names().getString(fft_jsonsection - 1));
            JSONArray fft_array = fft_json.getJSONArray("s" + (fft_jsonsection - 1));     // 取用最後一組資料 s3
            if (fft_array.length() == 18 * 14) {
                // 確認資料長度正確
                // 將一維資料轉換為二維資料
                Log.e(TAG, "資料長度 18 * 14");
                for (int harmonicindex = 0; harmonicindex<11; harmonicindex++) {
                    int ampvCol = 9;        // 能量變異在第 10 欄位 (index = 9)
                    try {
                        fft_ampv[harmonicindex] = (float) fft_array.getDouble((harmonicindex + 1) * 18 + ampvCol);
                    } catch (JSONException ee){
                        Log.e(TAG, "JSON 錯誤 at harmonicindex" + harmonicindex);
                    }
                }
                Log.e(TAG, "FFT_能量變異 H0 ~ H10 = " + fft_ampv.toString());
                for (int harmonicindex = 0; harmonicindex < 11; harmonicindex ++){
                    if (fft_ampv[harmonicindex] > fft_ampv_limit[harmonicindex]) {
                        redCount++;
                    }
                }
                Log.e(TAG, "FFT_能量變異有 " + redCount + " 個紅字");
                if (redCount > 0){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activityAlert.showAlertDialog(R.string.dialog_msg_bpmanlaysisresult,
                                    R.string.dialog_OK, activityAlert.doNothing);
                        }
                    });
                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activityAlert.showAlertDialog(R.string.dialog_msg_fftcounterror,
                                R.string.dialog_OK, activityAlert.doNothing);
                    }
                });
            }
        } catch (JSONException e) {
            // JSONArray 轉換失敗
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activityAlert.showAlertDialog(R.string.dialog_msg_jsontranserror,
                            R.string.dialog_OK, activityAlert.doNothing);
                }
            });
        }
    }

    private  void getWaveFlag(){
        JSONObject jqljson = new JSONObject();
        JSONObject wavejson = new JSONObject();
        try {
            jqljson.put("command", "GetWaveFlagByMidDate_APP");
            jqljson.put("versionTag", "1.1.0");
            jqljson.put("clinicnamewi", GlobalVariables.Login_Clinic);
            jqljson.put("mid", memberid);
            jqljson.put("date", uploadDate);
            jqljson.put("time", uploadTime);
            postKey = "GetWaveFlagByMidDate_APP";

            HttpPost httpPost = new HttpPost(Activity_BPM_Btbpm.this);
            httpPost.startPost(Activity_BPM_Btbpm.this, GlobalVariables.http_url,  jqljson.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            String errmsg = "JSON Object Error, " + e.toString() + "\nPlease measure again!";
            activityAlert.showAlertDialog(errmsg, R.string.dialog_OK, activityAlert.finishActivity);
        }
    }

    // 上傳資料
    private String uploadDate, uploadTime;
    private  void uploadwave(){
        if (isUploading) return;
        isUploading = true;     // 回應以後會 fasle
        uploadBPWaveData = new JSONObject();
//        JSONObject wavejson = new JSONObject();
        Calendar calendar = Calendar.getInstance();
        String DateTimeNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(calendar.getTimeInMillis()));
        Log.e(TAG, DateTimeNow);
        uploadDate = DateTimeNow.substring(0, 10);
        uploadTime = DateTimeNow.substring(11);
        localrecordID = Calendar.getInstance().getTimeInMillis();
        // 這裡的 localrecordID 除了用來標示本機資料庫的紀錄ID 之外
        // 也是記錄在 MDrecrod 中的 recordID，原本是在伺服器中自行產生的

        try {
//            if (device_ver.equals("v2")){
                uploadBPWaveData.put("command", "UploadBPMWave_APP");
//            } else {        // 臨床版本
//                uploadBPWaveData.put("command", "UploadBPMWave_APP4X");
//            }
            uploadBPWaveData.put("recordID", localrecordID);
            uploadBPWaveData.put("versionTag", "1.1.0");
            uploadBPWaveData.put("name", memname);
//            jqljson.put("clinicnamewi", clinicNamew);
            uploadBPWaveData.put("clinicnamewi", GlobalVariables.Login_Clinic);
            //jqljson.put("id", calendar.getTimeInMillis());        recordID 由伺服器產生 getTimeInMillis
            uploadBPWaveData.put("sbp", sbp);    // 收縮壓
            uploadBPWaveData.put("dbp", dbp);    // 舒張壓
            uploadBPWaveData.put("hr", hr);    // 心跳
            uploadBPWaveData.put("ihb", ihb);
            uploadBPWaveData.put("avatar", encoded64);    // 頭像
            uploadBPWaveData.put("bpwave", bpwave.toString());
            uploadBPWaveData.put("p1wave", pulsewave1.toString());
            uploadBPWaveData.put("p2wave", pulsewave2.toString());
            uploadBPWaveData.put("date", uploadDate);
            uploadBPWaveData.put("time", uploadTime);
            uploadBPWaveData.put("position", position);
            uploadBPWaveData.put("posture", posture);
            uploadBPWaveData.put("gender", gender);
            uploadBPWaveData.put("age", age);
            uploadBPWaveData.put("oxygen", oxygen);  //可能會改成波形
            uploadBPWaveData.put("memberid", memberid);
            uploadBPWaveData.put("addr", mDeviceAddress);
//            uploadBPWaveData.put("devicetype", deviceType + "_" + device_ver);              // heshi_bt_v2
            uploadBPWaveData.put("devicetype", GlobalVariables.Device_Type);              // heshi_bt_v2
            uploadBPWaveData.put("measureAPP", "ARD" + GlobalVariables.appversion);      // 引用的APP
            uploadBPWaveData.put("bpInflate", bpinflat.toString());                      // 血壓充氣波
            uploadBPWaveData.put("pulseInflate", pulseinflat.toString());            // 脈診充氣波
            uploadBPWaveData.put("bpmmHg", bpmmHg.toString());              // 壓力波
            uploadBPWaveData.put("adc0mmHg", adc0mmhg);                             // 參數
            uploadBPWaveData.put("adcpermmHg", adcpermmhg);                     // 參數
            uploadBPWaveData.put("adc0mmOffset", adc0mmhgoffset);        // 參數
            uploadBPWaveData.put("cuffCount", cuffcount);
            uploadBPWaveData.put("result", errvalue );
            uploadBPWaveData.put("deviceFW", deviceFW);

            if (GlobalVariables.Login_Role.equals("A")) {
                SingleAdmin _admin = SingleAdmin.getInstance();
                uploadBPWaveData.put("measureRole", "A");               // 量測模式為診所
                uploadBPWaveData.put("measuredBy", _admin.getAdminID()) ;                  // 實施量測的ID
            } else if (GlobalVariables.Login_Role.equals("M")) {
                SingleUser _user = SingleUser.getInstance();
                uploadBPWaveData.put("measureRole", "M");              // 量測模式為個人
                uploadBPWaveData.put("measuredBy", _user.getUserID());                 // 實施量測的ID
                uploadBPWaveData.put("attending", _user.getUserattending());
                uploadBPWaveData.put("fcmmessage", memname + getResources().getString(R.string.dialog_msg_notifyattending));
            }
            // 不管是否是 onLine 或是 offLine 都要先存起來
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "儲存紀錄 localrecordID = " + localrecordID);
                    //   uploadBPWaveData.put("recordID", localrecordID);          // 已經寫進去了
                    MDJsonRecord mDrecord = new MDJsonRecord(uploadBPWaveData.toString());      // 資料列
                    mDrecord.setMdrecordid(localrecordID);          // 用來作為本機上傳及刪除的指標
                    AppDatabase.getInstance(Activity_BPM_Btbpm.this).getMDjsonrecordDao().insertOneRecord(mDrecord);
                }
            }).start();

            if (GlobalVariables.onLine) {
//            new Thread(saveOnLocalDevice).start();
                postKey = "UploadBPMWave_APP";
                turnONprogressbar();
                HttpPost httpPost = new HttpPost(Activity_BPM_Btbpm.this);
                httpPost.startPost(Activity_BPM_Btbpm.this, GlobalVariables.http_url,  uploadBPWaveData.toString());
            } else {    // 存在Local
                new Thread(saveOnLocalDevice).start();      // 使用 localrecordID
            }
        } catch (JSONException e) {
            e.printStackTrace();
            String errmsg = "JSON Object Error, " + e.toString() + "\nPlease measure again!";
            activityAlert.showAlertDialog(errmsg, R.string.dialog_OK, activityAlert.finishActivity);
        }
    }

    @Override
    public void onComplete(String response) {
        turnOFFprogressbar();
        try {
            final JSONObject completeJson = new JSONObject(response);
            if (postKey.equals("UploadBPMWave_APP")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String response_msg = "";
                        try {
                            if (completeJson.getString("status").equals("success")) {
                                // 資料上傳成功。是否繼續量測另一側手腕，或是換下一個受測者量測?"
                                new Thread(deleteOneRecord).start();        // 刪除在本機端的一筆紀錄
                                isReadyUpload = false;      // 防止再上傳
                                isUploading = false;
                                btn_bpmstopmeasue.setText(R.string.btn_nextmeasure);
                                if (GlobalVariables.Login_Role.equals("M")) {       // 一般用戶就繼續分析
                                    // 不顯示上傳成功的對話框
//                                    new Thread(WaveAnalysisRunnable).start();       // 啟動背景執行續 詢問WaveFlag
                                    activityAlert.showAlertDialog(R.string.dialog_msg_waitdoctorresponse,
                                            R.string.dialog_OK, activityAlert.finishActivity);
                                } else {
                                    // 顯示對話框
                                    activityAlert.showAlertDialog(R.string.dialog_msg_bpmuploadsuccess,
                                            R.string.dialog_nextmember, goNextMember,
                                            R.string.dialog_switchhand, activityAlert.finishActivity);
                                }
                                //btn_bpmupload.setEnabled(true);        // 暫停上傳功能
                            } else if (completeJson.getString("status").equals("fail")) {
                                response_msg = getResources().getString(R.string.dialog_msg_bpmuploaderror) + " : " + completeJson.getString("result");
                                // 顯示對話框
                                activityAlert.showAlertDialog(R.string.dialog_msg_bpmuploaderror,
                                        R.string.dialog_reupload, reUpload,
                                        R.string.dialog_abordupload, activityAlert.finishActivity);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }   // end of run
                });
            } else if (postKey.equals("GetWaveFlagByMidDate_APP")) {    // 檢查旗標
                if (completeJson.getString("status").equals("success")) {      //
                    JSONObject flag = new JSONArray(completeJson.getString("dblist")).getJSONObject(0);
                    flagCut = flag.getInt("cutflag");
                    flagFFT = flag.getInt("fftflag");
                    flagID = flag.getLong("idflag");
                    fftData = flag.getString("fft");
                    askWaveFlagAvailable = true;
                }
            } else if (postKey.equals("UpdateMemberCheckin")) {
                finish();       // 確定收到變更之後 就關閉回到BPM Prepare
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFail(String err) {
        turnOFFprogressbar();
        try {
            final JSONObject failJson = new JSONObject(err);
            final String failStatus = failJson.getString("status");
            final String failResult = failJson.getString("result");
            String alertmsg = "";
            if (failStatus.equals("fail")) {
                int stringid = getResources().getIdentifier(failResult, "string", getPackageName());
                if (stringid > 0) {
                    alertmsg = getString(stringid);
                } else {
                    alertmsg = failResult;
                }

            } else if (failStatus.equals("exception")) {
                alertmsg = "Exception : \n" + failResult;
            }
            final String msg = alertmsg;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 顯示對話框
                    activityAlert.showAlertDialog(msg,
                            R.string.dialog_reupload, reUpload,
                            R.string.dialog_localsave, localSave);
                    activityAlert.showAlertDialog(msg, R.string.dialog_OK, activityAlert.doNothing);
                }
            });     // end of runOnUiThread
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //檢查血壓脈診儀的量測序列，如果錯誤就關閉Activity
    private boolean checkPulseSequence(String pulsestage, String pre_pulsesage){
        boolean checkResult = true;
        int int_stage, int_prestage;
        try {
            int_stage = bpmstagejson.getInt(pulsestage);
            int_prestage = bpmstagejson.getInt(pre_pulsesage);
            checkResult = (int_stage == int_prestage) || (int_stage == int_prestage + 1); // 等於現階段 或 等於上一個階段
        } catch (JSONException e){

        }
        if (checkResult){
            pre_pulsemode = pulsemode;
            pre_pulsephase = pulsephase;    // 更新 prestage
        } else {
            // 顯示警告對話框 並結束Acitivity
            dogWatchingFlag = false;    // 跳出迴圈就結束 run()  -> 關閉監聽
            isMeasureing = false;
            setActionHint(R.string.dialog_msg_bpmreadscheduleerror);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activityAlert.showAlertDialog(R.string.dialog_msg_bpmreadscheduleerror,
                            R.string.dialog_OK, activityAlert.finishActivity);
                }
            });
        }
        return checkResult;
    }

    private int wavemin = Integer.MAX_VALUE, wavemax = 0, wavedif = 1;

    // 血壓波啟動
    private void bpInitialize(byte[] bytes, String device_ver){
        if (helper.is_bpinitial_first) {        //  啟動階段 不區分版次
            wavedata = new JSONArray();        // 圖形波
            bpinflat = new JSONArray();         // 血壓充氣波陣列
            bpmmHg = new JSONArray();       // 充氣壓力
            bpwave = new JSONArray();           // 血壓波型
            wavemin = Integer.MAX_VALUE;wavemax = 0; wavedif = 1;
            isMeasureing = true;        //                  已經開始量測
            dogWatchingFlag = true;     //                     開啟監看
            new Thread(bpmWatchDog).start();        // 啟動監看
            drawView.setIsWaving(1);
            drawView.setXrange(0, 1220);        // 容納10秒
            Ymin = -850; Ymax = 850;
            helper.is_bpinitial_first = false;      // 設定完才能變
        }
        adc0mmhg = helper.getADC0mmHg(bytes, device_ver);
        adcpermmhg = helper.getADCpermmHg(bytes, device_ver);
        adc0mmhgoffset = helper.getADC0mmHgOffset(bytes, device_ver);
        wavedata.put(425);   // 1/2 高度
        if (ismmHg_Y) {
            Ymin = getADC(mmHg_L);
            Ymax = getADC(mmHg_H);
        }
        Log.e(TAG, "血壓 adc0mmhg = " + adc0mmhg + ", adcpermmhg = " + adcpermmhg + ", adc0mmhgoffset = " + adc0mmhgoffset);
    }

    private boolean ismmHg_Y = false;
    private int mmHg_L = 0;
    private int mmHg_H = 200;
    private int getADC(int mmHg){
//        return (mmHg) * adcpermmhg + adc0mmhg;
        return (mmHg + adc0mmhgoffset) * adcpermmhg + adc0mmhg;
    }
    private int getmmHg(int adc){
        return (int) ((adc - adc0mmhg) / adcpermmhg - adc0mmhgoffset);
    }


    // 脈診啟動
    private void pulseInitialize(byte[] bytes, String device_ver){
        if (helper.is_pulseinitial_first) {        //  啟動階段 不區分版次
            wavedata = new JSONArray();     // 圖形波
            pulseinflat = new JSONArray();      // 脈診充氣波
            pulsewave1 = new JSONArray();   // 脈診波 1
            pulsewave2 = new JSONArray();   // 脈診波 2
            wavemin = Integer.MAX_VALUE;wavemax = 0; wavedif = 1;
            drawView.setIsWaving(1);
            drawView.setXrange(0, 4880);        // 容納10秒
            Ymin = -850; Ymax = 850;
            helper.is_pulseinitial_first = false;      // 設定完才能變
        }
        Log.e(TAG, "血壓 adc0mmhg = " + adc0mmhg + ", adcpermmhg = " + adcpermmhg + ", adc0mmhgoffset = " + adc0mmhgoffset);
        adc0mmhg = helper.getADC0mmHg(bytes, device_ver);
        adcpermmhg = helper.getADCpermmHg(bytes, device_ver);
        adc0mmhgoffset = helper.getADC0mmHgOffset(bytes, device_ver);
        Log.e(TAG, "脈診 adc0mmhg = " + adc0mmhg + ", adcpermmhg = " + adcpermmhg + ", adc0mmhgoffset = " + adc0mmhgoffset);
        wavedata.put(425);   // 1/2 高度

    }

    private void initializeWaveData(int _xrange) {   // X Range =  取樣頻率 x10 秒
        wavedata = new JSONArray();     // 圖形波更新
        wavemin = Integer.MAX_VALUE;wavemax = 0; wavedif = 1;
        drawView.setIsWaving(1);
        drawView.setXrange(0, _xrange);
        Ymin = Integer.MAX_VALUE; Ymax = 0;
    }

    // 血壓波量測
    private void runbpwave(byte[] bytes, String device_ver){
        if (helper.is_runbpwave_first){
            initializeWaveData(1220);   // 血壓波取樣頻率 122Hz，X Range = 10 秒
            helper.is_runbpwave_first = false;
            }
            updateWaveData(bytes, "bpwave");
        }

    private void runpulsewave1(byte[] bytes, String device_ver){
        if (helper.is_runpulsewave1_first){
            initializeWaveData(4880);   // 脈診波取樣頻率  488Hz，X Range = 10 秒
            helper.is_runpulsewave1_first = false;
            int datafirst = helper.getDataValue3Bytes(bytes, 0);
            mmHg_L = getmmHg(datafirst) - 10;
            mmHg_H = mmHg_L + 20;
            drawView.setYaxis(mmHg_L, mmHg_H);
        }
        updateWaveData(bytes, "pulsewave1");
    }

    private void runpulsewave2(byte[] bytes, String device_ver){
        if (helper.is_runpulsewave2_first){
            initializeWaveData(4880);   // 脈診波取樣頻率  488Hz，X Range = 10 秒
            helper.is_runpulsewave2_first = false;
            int datafirst = helper.getDataValue3Bytes(bytes, 0);
            mmHg_L = getmmHg(datafirst) - 10;
            mmHg_H = mmHg_L + 20;
            drawView.setYaxis(mmHg_L, mmHg_H);
        }
        updateWaveData(bytes, "pulsewave2");
    }

    // 量測血壓時充氣
    private void runbpinflat (byte[] bytes, String device_ver){
        updateWaveData(bytes, "bpinflat");      // 將資料寫進 bpinflate陣列
    }

    // 量測血壓時充氣
    private void runpulseinflat (byte[] bytes, String device_ver){
        updateWaveData(bytes, "pulseinflat");
    }






    // *********************************************  血壓波 **********************************************
    // 藍芽CallBack
    private void getPulseData(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return;     // 陣列數量 = 0
        watchFlag = true;
        if (!isMeasureing) {
            Log.e(TAG, "isMesaureing False " );
            return;                                         // 停止量測就不再處理資料
        }
        // bytes[0 - 3] = command and length
        pulsemode = (int) bytes[3];
        pulsephase = (int) bytes[4];
        String pulsestage = "" + pulsemode + pulsephase;
        String pre_pulsestage = "" + pre_pulsemode + pre_pulsephase;
//        boolean checkSequence = checkPulseSequence(pulsestage, pre_pulsestage);
//        Log.e(TAG, "getPulseDate : 現在階段 = " + pulsestage + " 前一階段 = " + pre_pulsestage + " 階段檢查 = " + checkSequence);
        switch (pulsestage) {
            case "00":
                bpInitialize(bytes, device_ver);
                break;

            case "01":
                if (device_ver.equals("v2")) {
                    runbpwave(bytes, device_ver);       // 血壓量測
                } else {
                    runbpinflat(bytes, device_ver);         // 充氣階段
                }
                break;

            case "02":              // 血壓量測結束
                if (device_ver.equals("v2")) {
                    displayBloodPressure(bytes, device_ver);       // 血壓量測結束
                } else {
                    runbpwave(bytes, device_ver);                         // 血壓量測
                }
                break;

            case "03":
                displayBloodPressure(bytes, device_ver);        // v2 沒有 Phase 03

                break;

            case "10":             // v2充氣階段
                    pulseInitialize(bytes, device_ver);
                break;

            case "11":          // v2血壓波及時顯示
                if (device_ver.equals("v2")) {
                    runpulsewave1(bytes, device_ver);       // 脈診1量測
                } else {
                    runpulseinflat(bytes, device_ver);         // 脈診充氣階段
                }
                break;

            case "12":              //
                if (device_ver.equals("v2")) {
                    runpulsewave2(bytes, device_ver);       // 脈診2量測
                } else {
                    runpulsewave1(bytes, device_ver);       // 脈診1量測
                }
                break;
            case "13":              //
                if (device_ver.equals("v2")) {
                    stopmeasure(bytes, device_ver, "pulse");         // 停止量測
                } else {
                    runpulsewave2(bytes, device_ver);       // 脈診2量測
                }
                break;

            case "14":
                stopmeasure(bytes, device_ver, "pulse");         // 停止量測
                break;

        }       // end of switch
        try {
            // ***** 設定提示文字及狀態
            text_bpmdevicestatus.setText(bpmstatusjson.getInt(pulsestage));
            text_actionhint.setText(actionhintjson.getInt(pulsestage));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        pre_pulsemode = pulsemode;
        pre_pulsephase = pulsephase;
        drawView.setYrange(Ymin, Ymax);
        drawView.setWavedata(wavedata);
        drawView.refreshDraw();
    }

    private void updateWaveData(byte[] bytes, String type) {
        int byteslength = bytes.length;
        int datashift = 5;     // 起始 index
        int shift = 0;
        int dataint = 0;
        while ( (shift + datashift) < byteslength - 1) {
            switch (type) {
                case "bpinflat":    // 血壓波充氣
                    dataint = helper.getDataValue3Bytes(bytes, shift);
                    shift += 3;
//                    wavedata.put(dataint);        // 充氣時不要顯示圖形波
                    bpinflat.put(dataint);
                    break;
                case "bpwave":
                    dataint = helper.getDataValue3Bytes(bytes, shift);
                    bpmmHg.put(dataint);
                    shift += 3;
                    dataint = helper.getDataValue3Bytes(bytes, shift);
                    bpwave.put(dataint);
                    wavedata.put(dataint);
                    shift += 3;
                    break;
                case "pulseinflat":
                    dataint = helper.getDataValue3Bytes(bytes, shift);
                    shift += 3;
//                    wavedata.put(dataint);            // 充氣時不要顯示圖形波
                    pulseinflat.put(dataint);
                    break;
                case "pulsewave1":
                    dataint = helper.getDataValue3Bytes(bytes, shift);
                    shift += 3;
                    wavedata.put(dataint);
                    pulsewave1.put(dataint);
                    break;
                case "pulsewave2":
                    dataint = helper.getDataValue3Bytes(bytes, shift);
                    shift += 3;
                    wavedata.put(dataint);
                    pulsewave2.put(dataint);
                    break;
            }
            if (ismmHg_Y) {
                text_bpmdata.setText(device_ver + "M" + pulsemode + "P" + pulsephase + " " +
                        String.format("%08d", dataint) + "(" + String.format("%04d", getmmHg(dataint)) + "mmHg)");
            } else {
                text_bpmdata.setText(device_ver + "M" + pulsemode + "P" + pulsephase + " " +
                        String.format("%08d", dataint));
            }
        } // end of while

       switch (type) {      // 調整Y軸比例
           case "bpwave":
                   if (dataint < wavemin) {
                       wavemin = dataint;
                   }
                   if (dataint > wavemax) {
                       wavemax = dataint + 1;
                   }
                   bpdif = (int) ((wavemax - wavemin) / 5.0);
                   Ymin = wavemin - bpdif;
                   Ymax = wavemax + bpdif;
               break;
           case "pulsewave2":
           case "pulsewave1":
           if (ismmHg_Y) {
               Ymin = getADC(mmHg_L);
               Ymax = getADC(mmHg_H);
           } else {
               if (dataint < wavemin) {
                   wavemin = dataint;
               }
               if (dataint > wavemax) {
                   wavemax = dataint + 1;
               }
               bpdif = (int) ((wavemax - wavemin) / 5.0);
               Ymin = wavemin - bpdif;
               Ymax = wavemax + bpdif;
           }
               break;
       }
    }

    //  v2血壓量測結束
    private void displayBloodPressure(@NotNull byte[] bytes, String device_ver){
            sbp=helper.getSbp(bytes, device_ver);
            dbp = helper.getDbp(bytes, device_ver);
            hr = helper.getHr(bytes, device_ver);
            ihb = helper.getIHB(bytes, device_ver);                             // v2 ihb = -1
            errvalue = helper.getErrorByte(bytes, device_ver);      // v2 errvalue = -1
            errcode = helper.getErrorCode(errvalue);                    // v2 errcode = 0
            cuffcount = helper.getCuffcount(bytes, device_ver);
            Log.e(TAG, "sbp = " + sbp + " dbp = " + dbp + " hr = " + hr);
            Log.e(TAG, "ihb = " + ihb + " errvalue = " + errvalue + " errcode = " + errcode + " cuffcount = " + cuffcount);
        if (sbp == 0 || dbp == 0 || hr == 0 || errvalue > 0) {
            dogWatchingFlag = false;    // 跳出迴圈就結束 run()  -> 關閉監聽
            isMeasureing = false;
            // 強制回應停止量測
            Log.e(TAG, "強制回應停止量測");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (device_ver){
                        case "v2":
                            text_actionhint.setText(R.string.dialog_msg_bpmreaderror);
                            activityAlert.showAlertDialog(R.string.dialog_msg_bpmreaderror,
                                    R.string.dialog_measurereload,  activityAlert.finishActivity);
                            break;
                        default:            // 顯示設備提供的錯誤碼
                            text_actionhint.setText(R.string.dialog_msg_bpmreaderrorandreport);
                            activityAlert.showAlertDialog(errcode,
                                    R.string.dialog_measurereload,  activityAlert.finishActivity,
                                    R.string.dialog_errorreport,  askUploadWave );
                            break;
                    }   // end of switch
                }
            });
        } else {
            // 血壓及脈搏量測正常
            Xmin = 0;
            Xmax = bpwave.length();
            text_bpmdevicestatus.setText(R.string.bpmstatus_M0P2);
            text_sbpvalue.setText(sbp_name + "  " + (sbp > 0 ? String.valueOf(sbp) : "--") + "mmHg");       // 收縮壓
            text_dbpvalue.setText(dbp_name + "  " + (dbp > 0 ? String.valueOf(dbp) : "--") + "mmHg");   //  舒張壓
            text_hrvalue.setText(hr_name + "  " +  (hr > 0 ? String.valueOf(hr) : "--") + "/min");                       //  脈搏
            text_ihbvalue.setText(ihb_name + " " + (ihb ==1 ? "有":"無"));                       //  脈搏
            if (device_ver.equals("v4.x")) {                        //
                stopmeasure(bytes, device_ver, "bp");         // 臨床版本 v4.x     血壓量測結束
            }
        }
    }

    private void stopmeasure (byte[] bytes, String device_ver, String stopat){
        if (helper.is_stopmeasure) {
            Log.e(TAG, "脈診結束，停止監看  is_stopmeasure = " + helper.is_stopmeasure);
            dogWatchingFlag = false;        // 停止監看
            isMeasureing = false;       // 停止量測
            if (stopat.equals("pulse") && (pulsewave1.length() < 12000 || pulsewave2.length() < 12000)){
                Log.e(TAG, "脈診數據不足");
                text_actionhint.setText(R.string.actionhint_readinsufficient);     // 脈診儀量測數據不足，請停止量測作業並檢查脈診儀的狀態。
                text_bpmdevicestatus.setText(R.string.btn_bpmreloadmeasure);
                btn_bpmstopmeasue.setText(R.string.btn_bpmreloadmeasure);       // 重新量測
                activityAlert.showAlertDialog(R.string.actionhint_readinsufficient, R.string.btn_bpmreloadmeasure,activityAlert.finishActivity);
            } else {
                if (device_ver.equals("v2")) {
                    askDataUpload();
                } else {
                    bleCommand = "askFW";       //  詢問韌體
                    doWriteCommandToF5(askFW);  // response -> askDataUpload
                }
            }
            helper.is_stopmeasure = false;
        }
    }

    private void askDataUpload(){
        Log.e(TAG, "脈診數據充足");
        Log.e(TAG, "脈診數據充足，啟用上傳");
        isReadyUpload = true;
//        btn_bpmupload.setText(R.string.btn_bleupload);
//        btn_bpmstopmeasue.setText(R.string.btn_bpmreloadmeasure);       // 重新量測
        text_actionhint.setText(R.string.actionhint_measurefinish);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                activityAlert.getWindow().getAttributes().gravity = Gravity.BOTTOM;             // 將對話框移到下方
                activityAlert.showAlertDialog(R.string.actionhint_measurefinish,
                        R.string.btn_bleupload, askUploadWave,
                        R.string.btn_bpmreloadmeasure, activityAlert.finishActivity);
            }
        });
        Log.e(TAG, "bpwave 長度 = " + bpwave.length());
        Log.e(TAG, "pulsewave1 長度 = " + pulsewave1.length());
        Log.e(TAG, "pulsewave2 長度 = " + pulsewave2.length());
    }


    // 詢問是否要上傳
    private  DialogInterface.OnClickListener askUploadWave = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            askUploadData();
        }
    };

    // 資料上傳成功，更新掛號報到日期 下一筆量測
    private DialogInterface.OnClickListener goNextMember = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            //  更新掛號報到日期  2000-01-01 23:59:59
            JSONObject jqljson = new JSONObject();
            try {
                jqljson.put("command", "UpdateMemberCheckin");
                jqljson.put("clinicnamewi", GlobalVariables.Login_Clinic);
                jqljson.put("mid", memberid);
                jqljson.put("date", "2000-01-01 23:59:59");
                postKey = "UpdateMemberCheckin";
                HttpPost httpPost = new HttpPost(Activity_BPM_Btbpm.this);
                httpPost.startPost(Activity_BPM_Btbpm.this, GlobalVariables.http_url,  jqljson.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    // 資料上傳失敗，重新上傳
    private DialogInterface.OnClickListener reUpload = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            isReadyUpload = true;      // 防止再上傳
            isUploading = false;            // 沒有資料正在上傳
            uploadwave();
        }
    };

    // 資料上傳失敗，選擇本機儲存
    // 儲存完畢後，詢問是否繼續量測另一側手腕，或是換下一個受測者量測？
    private DialogInterface.OnClickListener localSave = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            new Thread(saveOnLocalDevice).start();     // end of Thread
//            new Thread(new Runnable_LocalSave(Activity_BPM_Btbpm.this, uploadBPWaveData)).start();
        }
    };

    // 刪除儲存在本機端的一筆紀錄
    private Runnable deleteOneRecord = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG, "刪除紀錄 LocalRecordID = " + localrecordID);
            AppDatabase.getInstance(Activity_BPM_Btbpm.this).getMDjsonrecordDao().deleteOneRecord(localrecordID);
        }
    };

    // 將這一筆資料存進本地端資料庫 AppDatabase
    private Runnable saveOnLocalDevice = new Runnable() {
        @Override
        public void run() {
            try {
                uploadBPWaveData.put("recordID", localrecordID);
                MDJsonRecord mDrecord = new MDJsonRecord(uploadBPWaveData.toString());      // 資料列
                mDrecord.setMdrecordid(localrecordID);
                AppDatabase.getInstance(Activity_BPM_Btbpm.this).getMDjsonrecordDao().insertOneRecord(mDrecord);
                // 顯示對話框
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activityAlert.showAlertDialog(R.string.dialog_msg_bpmsavesuccess,
                                R.string.dialog_nextmember, goNextMember,
                                R.string.dialog_switchhand, activityAlert.finishActivity);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    // 啟用進度條
    private void turnONprogressbar(){
        runOnUiThread(new Runnable() {  // 關閉進度條
            @Override
            public void run() {
                progressBar_bpmupload.setVisibility(View.VISIBLE);
                text_progressupload.setVisibility(View.VISIBLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });
    }

    // 關閉進度條
    private void turnOFFprogressbar(){
        runOnUiThread(new Runnable() {  // 關閉進度條
            @Override
            public void run() {
                progressBar_bpmupload.setVisibility(View.GONE);
                text_progressupload.setVisibility(View.GONE);
                // 恢復觸控功能
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });
    }

    private void responseBatteryStatus(byte[] data) {
        String responsestr = byteArrayToHexStr(data);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (responsestr.substring(0, 4)) {
                    case "33A3":        // 查詢電量
                        if (responsestr.substring(6, 8).equals("00")) {
                            Log.e(TAG, "電池電量正常");
                            activityAlert.showAlertDialog("電池電量正常", R.string.dialog_OK, activityAlert.doNothing);
                        } else if (responsestr.substring(6, 8).equals("01")) {
                            Log.e(TAG, "電池電量不足");
                            activityAlert.showAlertDialog("電池電量不足", R.string.dialog_OK, activityAlert.doNothing);
                        }
                        break;
                    default:
                        activityAlert.showAlertDialog("電池狀態資料錯誤" + responsestr, R.string.dialog_OK, activityAlert.doNothing);
                        break;
                }
            }
        });
    }

    private void responseFWversion(byte[] data) {
        if (data.length == 0) {
            activityAlert.showAlertDialog("韌體資料長度 0 ", R.string.dialog_OK, activityAlert.doNothing);
            return;
        }
        String responsestr = byteArrayToHexStr(data);
        Log.e(TAG, "FW 回應 " + responsestr);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (responsestr.substring(6, 10)) {
                    case "3356":        // 查詢版次
                        String fwstr = "";
                        for (int i = 6; i < 12; i++) {
                            int datai = (int) data[i];
                            fwstr += Character.toString((char) datai);
                            Log.e(TAG, "data[" + i + "] = " + datai + "  文字 =" +  Character.toString((char) datai));
                        }
                        deviceFW = fwstr;
                        Log.e(TAG, deviceFW);
                        askDataUpload();
                        break;
                    default:
                        activityAlert.showAlertDialog("韌體資料錯誤" + responsestr, R.string.dialog_OK, activityAlert.doNothing);

                        break;
                }
            }
        });
    }


    private void responseCuffCount(byte[] data) {
        if (data.length == 0) {
            activityAlert.showAlertDialog("壓脈帶數據長度 0 ", R.string.dialog_OK, activityAlert.doNothing);
            return;
        }
        String responsestr = byteArrayToHexStr(data);
        Log.e(TAG, "Cuff 回應 " + responsestr);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (responsestr.substring(6, 10)) {
                    case "33A7":        // 查詢版次
                        int countint = (int) (((data[6] & 0xFF) << 24) | ((data[7] & 0xFF) << 16) | ((data[8] & 0xFF) << 8) | (data[9] & 0xFF));
                        cuffCount = String.format("%04d", countint);         // 四位整數 前方補 0
                        break;
                    default:
                        activityAlert.showAlertDialog("量測次數資料錯誤" + responsestr, R.string.dialog_OK, activityAlert.doNothing);
                        break;
                }
            }
        });
    }


    private void nothing(){
        // 2022/01/03
        // onStop 中 不要中斷藍芽連線 以免中斷連線觸法回調造成當機
        /* 2022-02-03
            v2 區分 v3 的方法：
            系統預設為 v3，但是v2 在充氣階段大約十幾秒不會送出資料，因此 bpmDogWatch 來檢查空白資料的時間，如果超過六秒就判斷為 v2
            v2 與 v3 使用的 actionhintjson, bpmstatusjson, bpmstagejson 分開處理
            處理資料的函式 getPulseData, getPulseDatav3 也不相同。未來如果v2機器全部收回，就可以將 v2 相關內容刪除
         */
    }

}   // End of Activity_BPM

