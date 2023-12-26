package com.ideas.micro.jasonapp102;

import android.bluetooth.BluetoothGattCallback;

import org.jetbrains.annotations.NotNull;

public class Helper_BPM_Heshi {
    public boolean is_bpinitial_first, is_pulseinitial_first;
    public boolean is_runbpwave_first ;
    public boolean is_runpulsewave1_first, is_runpulsewave2_first;
    public boolean is_runbpinflat_first, is_runpulseinflat_first;
    public boolean is_stopmeasure;

    public Helper_BPM_Heshi(){
        is_bpinitial_first = true;
        is_pulseinitial_first = true;
        is_runbpwave_first = true;
        is_runpulsewave1_first = true;
        is_runpulsewave2_first = true;
        is_runbpinflat_first = true;
        is_runpulseinflat_first = true;
        is_stopmeasure = true;
    }

    public int getADC0mmHg(@NotNull byte[] bytes, String device_ver){
        return getDataValue3Bytes(bytes, 0);    // 所有device_ver 都一樣  Data2:0
    }
    public int getADCpermmHg(@NotNull byte[] bytes, String device_ver){
        return getDataValue2Bytes(bytes, 3);    // 所有device_ver 都一樣  Data4:3
    }
    public int getADC0mmHgOffset(@NotNull byte[] bytes, String device_ver){
        int preindex = 5;                 // byte[0 ~ 4] = 0x3F, 0x41, datalength, mode, phase
        return (short) (((bytes[preindex + 5] & 0xFF) << 8) |
                ((bytes[preindex + 5 + 1] & 0xFF)));    // 所有device_ver 都一樣  Data6:5
    }

    public int getADCmmHg(@NotNull byte[] bytes, String device_ver){
        return getDataValue3Bytes(bytes, 0);    // 所有device_ver 都一樣   Data2:0
    }

    public int getADCbpwave(@NotNull byte[] bytes, String device_ver){
        return getDataValue3Bytes(bytes, 3);    // 所有device_ver 都一樣 Data5:3
    }

    public int getADCpulsewave(@NotNull byte[] bytes, String device_ver){
        return getDataValue3Bytes(bytes, 0);    // 所有device_ver 都一樣   Data2:0
    }

    public int getSbp(@NotNull byte[] bytes, String device_ver){
        int _sbp = 0;
        switch (device_ver){
            case "v2":          // byte[0 ~ 4] = 0x3F, 0x41, datalength, mode, phase
                _sbp = getDataValue2Bytes(bytes, 0);
                break;
            case "v3":
                _sbp = getDataValue2Bytes(bytes, 1);
                break;
            case "v4.5.1": case "v4.5.2": case "v4.x":
                _sbp = getDataValue2Bytes(bytes, 5);
                break;
            case "v5.0":
                _sbp = getDataValue2Bytes(bytes, 5);
                break;
        }
        return _sbp;
    }

    public int getDbp(@NotNull byte[] bytes, String device_ver){
        int _dbp = 0;
        switch (device_ver){
            case "v2":          // byte[0 ~ 4] = 0x3F, 0x41, datalength, mode, phase
                _dbp = getDataValue2Bytes(bytes, 2);
                break;
            case "v3":
                _dbp = getDataValue2Bytes(bytes, 3);
                break;
            case "v4.5.1": case "v4.5.2": case "v4.x":
                _dbp = getDataValue2Bytes(bytes, 7);
                break;
            case "v5.0":
                _dbp = getDataValue2Bytes(bytes, 7);
                break;
        }
        return _dbp;
    }

    public int getHr(@NotNull byte[] bytes, String device_ver){
        int _hr = 0;
        switch (device_ver){
            case "v2":          // byte[0 ~ 4] = 0x3F, 0x41, datalength, mode, phase
                _hr = getDataValue1Bytes(bytes, 4);
                break;
            case "v3":         // byte[0 ~ 4] = 0x3F, 0x41, datalength, mode, phase
                _hr = getDataValue1Bytes(bytes, 5);
                break;
            case "v4.5.1": case "v4.5.2": case "v4.x":         // byte[0 ~ 4] = 0x3F, 0x41, datalength, mode, phase
                _hr = getDataValue1Bytes(bytes, 9);
                break;
            case "v5.0":
                _hr = getDataValue1Bytes(bytes, 9);
                break;
        }
        return _hr;
    }

    public int getIHB(@NotNull byte[] bytes, String device_ver){
        int preindex = 5;                 // byte[0 ~ 4] = 0x3F, 0x41, datalength, mode, phase
        int _ihb = -1;          // -1 表示沒有這項資訊
        switch (device_ver){
            case "v2":
                _ihb = -1;
                break;
            case "v3":
                _ihb = (int) (bytes[preindex + 0] & 0x01);
                break;
            case "v4.5.1": case "v4.5.2": case "v4.x":         // byte[0 ~ 4] = 0x3F, 0x41, datalength, mode, phase
                _ihb = (int) (bytes[preindex + 4] & 0x01);
                break;
            case "v5.0":
                _ihb = (int) (bytes[preindex + 4] & 0x01);
                break;
        }
        return _ihb;
    }

    public int getErrorByte(@NotNull byte[] bytes, String device_ver){
        int preindex = 5;                 // byte[0 ~ 4] = 0x3F, 0x41, datalength, mode, phase
        int _err = 0;          // -1 表示沒有這項資訊
        switch (device_ver){
            case "v2":
                _err = -1;
                break;
            case "v3":
                _err =getDataValue1Bytes(bytes, 0);
                break;
            case "v4.5.1": case "v4.5.2": case "v4.x":          // byte[0 ~ 4] = 0x3F, 0x41, datalength, mode, phase
                _err = getDataValue1Bytes(bytes, 4);
                break;
            case "v5.0":
                _err = getDataValue1Bytes(bytes, 4);
                break;
        }
        _err = (_err > 0xE0?_err:0);        // 超過 E0 才算是Errot
        return _err;      // 回覆支援 R.string  v2 會回應 -1
    }

    public int getErrorCode(int errorbyte){
        return getErrorResourceCode(errorbyte);      // 回覆支援 R.string  v2 會回應 0
    }

    private int getErrorResourceCode (int _err){
        int errorcode = 0;
        switch (_err){
            case 0x00: errorcode = R.string.bpmstatus_00; break;
            case 0x01: errorcode = R.string.bpmstatus_01; break;
            case 0xE0: errorcode = R.string.bpmstatus_E0; break;
            case 0xE1: errorcode = R.string.bpmstatus_E1; break;
            case 0xE2: errorcode = R.string.bpmstatus_E2; break;
            case 0xE3: errorcode = R.string.bpmstatus_E3; break;
            case 0xEA: errorcode = R.string.bpmstatus_EA; break;
            case 0xEB: errorcode = R.string.bpmstatus_EB;break;
            case 0xEE: errorcode = R.string.bpmstatus_EE;break;
            }
            return errorcode;
    }

    public int getCuffcount(@NotNull byte[] bytes, String device_ver){
        int _cuffcount = -1;         // -1 表示沒有這項資訊 v2, v3 沒有這項資訊輸出
        switch (device_ver){
            case "v4.5.1": case "v4.5.2": case "v4.x":           // byte[0 ~ 4] = 0x3F, 0x41, datalength, mode, phase
                _cuffcount =getDataValue4Bytes(bytes, 0);
                break;
            case "v5.0":           // byte[0 ~ 4] = 0x3F, 0x41, datalength, mode, phase
                _cuffcount =getDataValue4Bytes(bytes, 0);
                break;
        }
        return _cuffcount;
    }

    public int getDataValue1Bytes(@NotNull byte[] bytes, int startindex ){
        int preindex = 5;                 // byte[0 ~ 4] = 0x3F, 0x41, datalength, mode, phase
        return (int) (bytes[preindex + startindex] & 0xFF);
    }

    public int getDataValue2Bytes(@NotNull byte[] bytes, int startindex ){
        int preindex = 5;                 // byte[0 ~ 4] = 0x3F, 0x41, datalength, mode, phase
        return (int) (((bytes[preindex + startindex] & 0xFF) << 8) |
                ((bytes[preindex + startindex + 1] & 0xFF)));
    }

    public int getDataValue3Bytes(@NotNull byte[] bytes, int startindex ){
        int preindex = 5;                 // byte[0 ~ 4] = 0x3F, 0x41, datalength, mode, phase
        return  (int) (((bytes[preindex + startindex] & 0xFF) << 16) |
                    ((bytes[preindex + startindex + 1] & 0xFF) << 8) |
                    ((bytes[preindex + startindex + 2] & 0xFF)));

    }

    public int getDataValue4Bytes(@NotNull byte[] bytes, int startindex ){
        int preindex = 5;                 // byte[0 ~ 4] = 0x3F, 0x41, datalength, mode, phase
        return  (int) (((bytes[preindex + startindex] & 0xFF) << 24) |
                ((bytes[preindex + startindex + 1] & 0xFF) << 16) |
                ((bytes[preindex + startindex + 2] & 0xFF) << 8) |
                ((bytes[preindex + startindex + 3] & 0xFF)));
    }

}
