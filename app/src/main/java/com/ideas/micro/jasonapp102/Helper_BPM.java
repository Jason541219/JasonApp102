package com.ideas.micro.jasonapp102;

public class Helper_BPM {
    public static final String OSTAR_START1 = "A5C00C0001";           // 啟動標準血壓量測
    public static final String OSTAR_START1_ACCEPT = "5AC00C0100";        // 回覆：收到啟動指令
    public static final String OSTAR_START1_REJECT = "5AC00C010x";        // 回覆：拒絕啟動指令
    public static final String OSTAR_START2 = "A5C00C0002";           // 啟動脈診血壓量測
    public static final String OSTAR_START2_ACCEPT = "5AC00C0200";        // 回覆：收到啟動指令
    public static final String OSTAR_START2_REJECT = "5AC00C020x";        // 回覆：拒絕啟動指令
    public static final String OSTAR_STOP = "A5C00C0003";           // 停止血壓量測
    public static final String OSTAR_STOP_ACCEPT = "5AC00C0300";        // 回覆：收到停止指令
    public static final String OSTAR_STOP_REJECT = "5AC00C030x";        // 回覆：拒絕停止指令
    public static final String OSTAR_GETBP = "A5C00C0004";           // 讀取血壓
    public static final String OSTAR_GETBP_ACCEPT = "5AC00C0400";        // 回覆：收到讀取指令
    public static final String OSTAR_GETBP_REJECT = "5AC00C040x";        // 回覆：拒絕讀取指令
    public static final String OSTAR_GETSTATUS = "A5C00C0005";           // 讀取狀態
    public static final String OSTAR_GETSTATUS_READY = "5AC00C0500";        // 回覆：狀態
    public static final String OSTAR_DATA1_READ = "5AC00C0001";
    public static final String OSTAR_DATA1_END = "5A0CC00100";
    public static final String OSTAR_DATA1_RESULT = "5A0CC00101";
    public static final String OSTAR_DATA1_ERROR = "5A0CC00102";
    public static final String OSTAR_DATA2_READ = "5AC00C0002";
    public static final String OSTAR_DATA2_END = "5A0CC00200";
    public static final String OSTAR_DATA2_ERROR = "5A0CC00201";


}
