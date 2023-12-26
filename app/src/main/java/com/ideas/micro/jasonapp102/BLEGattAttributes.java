package com.ideas.micro.jasonapp102;


import java.util.HashMap;
/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class BLEGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String HLIFE_COMMUNICATION_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";
    public static String HLIFE_READ_CHARACTERISTIC_SERVICE_F4 = "0000fff4-0000-1000-8000-00805f9b34fb";
    public static String HLIFE_WRITE_CHARACTERISTIC_SERVICE = "0000fff5-0000-1000-8000-00805f9b34fb";
    public static String HLIFE_READ_CHARACTERISTIC_SERVICE_F6 = "0000fff6-0000-1000-8000-00805f9b34fb";

    public static String OSTAR_COMMUNICATION_SERVICE = "49535343-fe7d-4ae5-8fa9-9fafd205e455";
    public static String OSTAR_READ_CHARACTERISTIC = "49535343-1e4d-4bd9-ba61-23c647249616";
    public static String OSTAT_WRITE_CHARACTERISTIC = "49535343-8841-43f4-a8d4-ecbe34729bb3";

    public static byte[] askFW = new byte[]{(byte) 0x6C, (byte) 0x56,  (byte) 0x00,  (byte) 0x3A};
    public static byte[] askBattery = new byte[]{(byte) 0x6C, (byte) 0xA3,  (byte) 0x00,  (byte) 0xCF};
    public static byte[] askCuffCount  = new byte[] { (byte) 0x6C, (byte) 0xA7,  (byte) 0x00,  (byte) 0xCB}; // "(0x) 6C-A7-00-CB" sent (APP Send CuffCount Request to BPM，需求計數)
    public static byte[] clearCuffCount = new byte[]{ (byte) 0x6C, (byte) 0xA8,  (byte) 0x00,  (byte) 0xC4}; // "{"(0x) 6C-A8-00-C4" sent (APP Send CuffClear Request to BPM，清除計數)}

    static {
        // Sample Services.
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Generic Access");
        attributes.put("00001801-0000-1000-8000-00805f9b34fb", "Generic Attribute");
        attributes.put("00001812-0000-1000-8000-00805f9b34fb", "Human Interface Device");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180f-0000-1000-8000-00805f9b34fb", "Battery Service");

        attributes.put("0000fff0-0000-1000-8000-00805f9b34fb", "Communication Service");
        attributes.put("0000fff4-0000-1000-8000-00805f9b34fb", "Read Characteristic of the Service");
        attributes.put("0000fff5-0000-1000-8000-00805f9b34fb", "Write Characteristic of the Service");
        attributes.put("0000fff6-0000-1000-8000-00805f9b34fb", "Read Characteristic of the Service424");


        // Sample Characteristics.
        attributes.put("00002a00-0000-1000-8000-00805f9b34fb", "Device Name");
        attributes.put("00002a01-0000-1000-8000-00805f9b34fb", "Appearance");
        attributes.put("00002a04-0000-1000-8000-00805f9b34fb", "Peripheral Preferred Connection Parameters");
        attributes.put("00002a05-0000-1000-8000-00805f9b34fb", "Service Changed");
        attributes.put("00002aa6-0000-1000-8000-00805f9b34fb", "Central Address Resolution");
        attributes.put("00002ac9-0000-1000-8000-00805f9b34fb", "Resolvable Private Address Only");
        attributes.put("00002a23-0000-1000-8000-00805f9b34fb", "System ID");
        attributes.put("00002a24-0000-1000-8000-00805f9b34fb", "Model Number String");
        attributes.put("00002a25-0000-1000-8000-00805f9b34fb", "Serial Number String");
        attributes.put("00002a26-0000-1000-8000-00805f9b34fb", "Firmware Revision String");
        attributes.put("00002a27-0000-1000-8000-00805f9b34fb", "Hardware Revision String");
        attributes.put("00002a28-0000-1000-8000-00805f9b34fb", "Software Revision String");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put("00002a2a-0000-1000-8000-00805f9b34fb", "Regulatory Certification Data List");
        attributes.put("00002a37-0000-1000-8000-00805f9b34fb", "Heart Rate Measurement");

    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
