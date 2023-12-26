package com.ideas.micro.jasonapp102;

public class BluetoothChatService_Ostar {
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String COMMUNICATION_SERVICE = "49535343-fe7d-4ae5-8fa9-9fafd205e455";
    public static String READ_SERVICE = "49535343-1e4d-4bd9-ba61-23c647249616";
    public static String WRITE_SERVICE = "49535343-8841-43f4-a8d4-ecbe34729bb3";
    public static final long OSTAR_START = 0xA5C00C0001L;
    public static final long OSTAR_READ = 0xA5C00C0002L;
    public static final long OSTAR_STOP =0xA5C00C0003L;
    public static final long OSTAR_GETBP = 0xA5C00C0004L;
    public static final long OSTAR_STATUS = 0xA5C00C0005L;
    public static final long OSTAR_START_OK = 0x5AC00C0100L;
    public static final long OSTAR_READ_ACCEPT = 0x5AC00C0200L;
    public static final long OSTAR_STOP_OK = 0x5AC00C0300L;
    /*
    Bluetooth adverstisement information must include following
1. BT MAC addr
2. Device NAME:  OSTAR_BLE
3. Transparent UART Mode.
   UUID:
   Service  49535343-fe7d-4ae5-8fa9-9fafd205e455
   Read     49535343-1e4d-4bd9-ba61-23c647249616
   Write    49535343-8841-43f4-a8d4-ecbe34729bb3


CMD : Start monitor standard pressure
    0xA5C00C 00 01

RESP :
    0x5AC00C 01 00  <-- command accept
    0x5AC00C 01 0x  <-- command reject with error code x


CMD : Start monitor average pressure
    0xA5C00C 00 02

RESP :
    0x5AC00C 02 00  <-- command accept
    0x5AC00C 02 0x  <-- command reject with error code x


CMD : Stop monitor
    0xA5C00C 00 03

RESP :
    0x5AC00C 03 00  <-- command accept
    0x5AC00C 03 0x  <-- command reject with error code x


CMD : Get pressure/heartbeat and etc
    0xA5C00C 00 04

RESP:
    0x5AC00C 04 00 [Systolicpressure] [Diastolic pressure] [heartbeat] [i0] [i1] [i2] [i3] [i4] [AF] <-- command accept
    0x5AC00C 04 0x  <-- command reject with error code x


CMD : Get status
    0xA5C00C 00 05

RESP :
    0x5AC00C 05 0x  <-- status with return code x


std monitor data trnasfer

    0x5AC00C 00 01
    0x000000 DATA
    0x000001 DATA
    0x0XXXXX DATA
    0x0XXXXX DATA
    0x5A0CC0 01 00 <-- Data end
    0x5A0CC0 01 01 [Systolicpressure] [Diastolic pressure] [heartbeat] [i0] [i1] [i2] [i3] [i4] [AF]
    0x5A0CC0 01 02 <-- Data end if error


average monitor data trnasfer
    0x5AC00C 00 02
    0x000000 DATA
    0x000001 DATA
    0x0XXXXX DATA
    0x0XXXXX DATA
    0x5A0CC0 02 00 <-- Data end
    0x5A0CC0 02 01 <-- Data end if error


Note:
    DATA :       2 bytes ADC Raw Data (500 samples/sec)
    Systolic :   2 bytes integer
    Diastolic :  2 bytes integer
    Heartbeat :  2 bytes integer
    i0 ~ i4   :  1 byte  integer
    AF :         1 byte  integer

    error/status code:
    00: ready / no error
    01: busy / measurement in progress
    02: measurement fails

     */
}
