package com.ideas.micro.jasonapp102;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class HttpPost {
    private final String TAG = "Http_Post";
    // onHttpPostCallback   是 Interface 介面
    private onHttpPostCallback mHttpPostCallback;

    private String url = GlobalVariables.http_url;

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

    // 建構式
    //  HttpPost httpPost = new HttpPost(Activity_BPM_Btbpm.this);   因為 Activity_BPM_Btbpm 實作了 implements onHttpPostCallback
    public HttpPost(onHttpPostCallback callback) {
        this.mHttpPostCallback = callback;
    }

    public static boolean isNetAvailable(Context context) {
        if (context!= null) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                    Network network = cm.getActiveNetwork();
                    if (network != null) {
                        NetworkCapabilities nc = cm.getNetworkCapabilities(network);
                        if (nc != null) {
                            if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {//WIFI
                                return true;
                            } else if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {//移動數據
                                return true;
                            }
                        }
                    }
                }
        }
        return false;
    }

    private boolean isNetworkConnect(Context context) {
        int result = 0; // Returns connection type. 0: none; 1: mobile data; 2: wifi
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (cm != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        result = 2;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        result = 1;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        result = 3;
                    }
                }
            }
        }
        return result >0?true:false;
    }

    public void startPost(Context context, String urlconnect, String postData){
        if (!isNetworkConnect(context)){    //網路沒有連線
            try {
                JSONObject responsenull = new JSONObject();
                responsenull.put("status", "exception");
                responsenull.put("result", "網路沒有連線，請檢查您設備的網路通訊功能");
                String netnull = responsenull.toString();
                netnull = GlobalVariables.isEnclypted? Utility_AES.Decrypt(netnull):netnull;        // 加密傳送
                mHttpPostCallback.onFail(netnull);          //  將exception
            } catch (JSONException e) {
                Log.e(TAG, "JSONException");
            }
        } else {
            final String postdata =  GlobalVariables.isEnclypted? Utility_AES.Encrypt(postData):postData;
            // 建立一個新的執行緒，執行Runnable工作
            Log.e(TAG, "postdata = " + postData);
            Log.e(TAG, "postData = " + postdata);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String result = "";
                    String urlstr = "";
                    HttpURLConnection connection;
                    try {
                        URL url = new URL(urlconnect);        // 定義在類別 GlobalVariable 的全域變數
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setDoInput(true);
                        connection.setDoOutput(true);
                        connection.setConnectTimeout(5000);
                        Log.e(TAG, "postdata = " + postdata);
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(),
                                "UTF-8"));
                        bw.write(postdata);
                        bw.flush();
                        int status = connection.getResponseCode();
                        Log.e(TAG, "回應碼 Response Code : " + String.valueOf(status));
                        bw.close();

                        InputStream inputStream = connection.getInputStream();
//                        int status = connection.getResponseCode();
//                        Log.e(TAG, "回應碼 Response Code : " + String.valueOf(status));
                        if (inputStream != null) {
                            InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
                            BufferedReader in = new BufferedReader(reader);
                            String line = "";
                            while ((line = in.readLine()) != null) {
                                result += line;
                            }
                            Log.e(TAG, "回應內容 " + result);
                            result = GlobalVariables.isEnclypted? Utility_AES.Decrypt(result):result;
                            if (!result.equals("")) {
                                try {
                                    JSONObject resultjson  = new JSONObject(result);
                                    String resultstatus = resultjson.getString("status");
                                    if (resultstatus.equals("success")) {
                                        mHttpPostCallback.onComplete(result);
                                    } else if (resultstatus.equals("fail")){
                                        mHttpPostCallback.onComplete(result);
                                    } else if (resultstatus.equals("exception")) {
                                        mHttpPostCallback.onFail(result);
                                    }
                                } catch (JSONException e) {
                                    Log.e(TAG, "JSON Error" + e.toString());
                                }
                                Log.e(TAG, "response is not empty");
                            } else {
                                JSONObject responseempty = new JSONObject();
                                responseempty.put("status", "exception");
                                responseempty.put("result", "response empty string");
                                mHttpPostCallback.onFail(responseempty.toString());
                            }
                        } else {
                            JSONObject responsenull = new JSONObject();
                            responsenull.put("status", "exception");
                            responsenull.put("result", "response null");
                            mHttpPostCallback.onFail(responsenull.toString());
                        }
                    } catch (java.net.SocketTimeoutException e) {    // 連線逾時
                        JSONObject responseexception = new JSONObject();
                        try {
                            responseexception.put("status", "exception");
                            responseexception.put("result", "Connection Timeout : " + e.getLocalizedMessage());
                            mHttpPostCallback.onFail(responseexception.toString());
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Exception : " +  e.toString());
                        e.printStackTrace();
                        JSONObject responseexception = new JSONObject();
                        try {
                            responseexception.put("status", "exception");
                            responseexception.put("result", e.toString() + "\n" + "Please Check Server Connection Status");
                            mHttpPostCallback.onFail(responseexception.toString());
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
            thread.start();
        }
    }

    /******************************************************************************
     * Converts the contents of an InputStream to a String.
     */
    public String readStream(InputStream stream, int maxReadSize)
            throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] rawBuffer = new char[maxReadSize];
        int readSize;
        StringBuffer buffer = new StringBuffer();
        while (((readSize = reader.read(rawBuffer)) != -1) && maxReadSize > 0) {
            if (readSize > maxReadSize) {
                readSize = maxReadSize;
            }
            buffer.append(rawBuffer, 0, readSize);
            maxReadSize -= readSize;
        }
        return buffer.toString();
    }       // readStream

    public void startPostWithFile(Context context, String urlconnect, Map<String, String> fields, Map<String, String> headers
            , String fieldName, String fileName, FileInputStream fileInputStream) {
        if (!isNetworkConnect(context)){    //網路沒有連線
            try {
                JSONObject responsenull = new JSONObject();
                responsenull.put("status", "exception");
                responsenull.put("result", "網路沒有連線，請檢查設備的網路通訊功能");
                mHttpPostCallback.onFail(responsenull.toString());          //  將exception
            } catch (JSONException e) {
                Log.e(TAG, "JSONException");
            }
        } else {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String result = "";
                    String LINE_FEED = "\r\n";
                    HttpURLConnection httpConn;
                    String charset;
                    OutputStream outputStream;
                    BufferedWriter writer;

                    try {
                        String boundary = UUID.randomUUID().toString();
                        URL url = new URL(urlconnect);
                        httpConn = (HttpURLConnection) url.openConnection();
                        httpConn.setUseCaches(false);
                        httpConn.setDoOutput(true);    // indicates POST method
                        httpConn.setDoInput(true);
                        httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                        if (headers != null && headers.size() > 0) {
                            Iterator<String> it = headers.keySet().iterator();
                            while (it.hasNext()) {
                                String key = it.next();
                                String value = headers.get(key);
                                httpConn.setRequestProperty(key, value);
                            }
                        }
                        outputStream = httpConn.getOutputStream();
                        writer = new BufferedWriter(new OutputStreamWriter(outputStream, "utf-8"));

                        //set filed
                        if (fields != null && fields.size() > 0) {
                            Iterator<String> it = fields.keySet().iterator();
                            while (it.hasNext()) {
                                String name = it.next();
                                String value = fields.get(name);

                                Log.d(TAG, "run: name:" + name + " value:" + value);

                                writer.append("--" + boundary).append(LINE_FEED);
                                writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append(LINE_FEED);
                                writer.append("Content-Type: text/plain; charset=utf-8").append(LINE_FEED);
                                writer.append(LINE_FEED);
                                writer.append(value).append(LINE_FEED);
                                writer.flush();
                            }
                        }

                        //set file
                        writer.append("--" + boundary).append(LINE_FEED);
                        writer.append("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"").append(LINE_FEED);
                        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
                        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
                        writer.append(LINE_FEED);
                        writer.flush();

                        byte[] buffer = new byte[4096];
                        int bytesRead = -1;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        outputStream.flush();
                        fileInputStream.close();
                        writer.append(LINE_FEED);
                        writer.flush();

                        String response = "";
                        writer.flush();
                        writer.append("--" + boundary + "--").append(LINE_FEED);
                        writer.close();

                        InputStream inputStream = httpConn.getInputStream();

                        if (inputStream != null) {
                            InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
                            BufferedReader in = new BufferedReader(reader);
                            String line = "";
                            while ((line = in.readLine()) != null) {
                                result += line;
                            }

                            result = GlobalVariables.isEnclypted? Utility_AES.Decrypt(result):result;
                            if (!result.equals("")) {
                                try {
                                    JSONObject resultjson  = new JSONObject(result);
                                    String resultstatus = resultjson.getString("status");
                                    if (resultstatus.equals("success")) {
                                        mHttpPostCallback.onComplete(result);
                                    } else if (resultstatus.equals("fail")){
                                        mHttpPostCallback.onComplete(result);
                                    } else if (resultstatus.equals("exception")) {
                                        mHttpPostCallback.onFail(result);
                                    }
                                } catch (JSONException e) {
                                    Log.e(TAG, "JSON Error" + e.toString());
                                }
                                Log.e(TAG, "response is not empty");
                            } else {
                                JSONObject responseempty = new JSONObject();
                                responseempty.put("status", "exception");
                                responseempty.put("result", "response empty string");
                                mHttpPostCallback.onFail(responseempty.toString());
                            }
                        } else {
                            JSONObject responsenull = new JSONObject();
                            responsenull.put("status", "exception");
                            responsenull.put("result", "response null");
                            mHttpPostCallback.onFail(responsenull.toString());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        JSONObject responseexception = new JSONObject();
                        try {
                            responseexception.put("status", "exception");
                            responseexception.put("result", e.toString());
                            mHttpPostCallback.onFail(responseexception.toString());
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
            thread.start();
        }
    }
}
