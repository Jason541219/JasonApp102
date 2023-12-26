/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ideas.micro.jasonapp102;

/**
 * Defines several constants used between {@link BluetoothChatService} and the UI.
 */
public interface Constants {

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    public static final byte[] MEASUREMENT_START_TO_APP = {(byte) 0x3C, (byte) 0x42, (byte) 0x00, (byte) 0x7E};
    public static final byte[] MEASUREMENT_START_BY_APP = {(byte) 0xC3, (byte) 0x42, (byte) 0x00, (byte) 0x81};
    public static final byte[] MEASUREMENT_END_TO_APP = {(byte) 0x3C, (byte) 0x43, (byte) 0x00, (byte) 0x7F};
    public static final byte[] MEASUREMENT_END_BY_APP = {(byte) 0xC3, (byte) 0x43, (byte) 0x00, (byte) 0x80};

}
