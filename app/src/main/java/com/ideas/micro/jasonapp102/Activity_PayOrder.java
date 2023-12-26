package com.ideas.micro.jasonapp102;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class Activity_PayOrder extends AppCompatActivity implements onHttpPostCallback {
    private final String TAG = "PayOrder";
    private String postkey = "";
    private String EPOS = "https://sslpayment.uwccb.com.tw/EPOSService/Payment/";
    private String OrderInitPage = "OrderInitial.aspx";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payorder);
        setOrder();
    }

    private void setOrder(){
        Log.e(TAG, "線上金流");
        JSONObject jqljson = new JSONObject();
        String pid = GlobalVariables.Login_Role.equals("M")?SingleUser.getInstance().getUserpid():SingleAdmin.getInstance().getAdminpid();
        Log.e(TAG, "PID = " + pid);
        try {
            jqljson.put("command", "GetOrderXML");
            jqljson.put("amount", 1);
            jqljson.put("language", "ZH-TW");
            jqljson.put("date", Utility.getDateTimeNow());
            jqljson.put("pid", pid);
            jqljson.put("clinicnamewi", "RootDB");
            jqljson.put("cube", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        Log.e(TAG, jqljson.toString());
        postkey = "GetOrderXML";
        HttpPost httpPost = new HttpPost(Activity_PayOrder.this);
        httpPost.startPost(Activity_PayOrder.this,  GlobalVariables.http_url, jqljson.toString());
    }

    private void sendRequestByHTML(String orderxml){
        /*
        可以使用data:html/text;+Html的URL ENCODE字串轉換成Uri,給外部瀏覽器使用。
        利用html的form TAG帶入要POST參數內容，利用window.onload開啟時,自動執行submit。
        這樣就可以post方式讓外部瀏覽器,去開啟一個網頁.
         */
        StringBuilder htmlUrl = new StringBuilder();
            htmlUrl.append("<html><body onload=\"document.getElementById('form').submit()\" >");
            htmlUrl.append("<form id=\"form\" method=\"post\" action=\"" + EPOS + OrderInitPage +  "\">");
            htmlUrl.append("<input type=\"hidden\" name=\"strRqXML\" value=\"" + orderxml +  "\">");
            htmlUrl.append("</form>");
            htmlUrl.append("</body></html>");
            Log.e(TAG, "HTML = " + htmlUrl);
        Uri uri = null;
        try {
            uri = Uri.parse("data:html/text,"
                    + URLEncoder.encode(htmlUrl.toString(),"utf-8").replaceAll("\\+","%20"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Intent it = new Intent();
        it.setComponent(getDefaultBrowserComponent(Activity_PayOrder.this));
        it.setAction(Intent.ACTION_VIEW); // = new Intent(Intent.ACTION_VIEW, uri);
        it.setData(uri);
        startActivity(it);  // 變成下載類型 而非網頁
    }

    public static ComponentName getDefaultBrowserComponent(Context context) {
        Intent i = new Intent()
                .setAction(Intent.ACTION_VIEW)
                .setData(new Uri.Builder()
                        .scheme("http")
                        .authority("x.y.z")
                        .appendQueryParameter("q", "x")
                        .build()
                );
        PackageManager pm = context.getPackageManager();
        ResolveInfo default_ri = pm.resolveActivity(i, 0); // may be a chooser
        ResolveInfo browser_ri = null;
        // 	List list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        List<ResolveInfo> rList = pm.queryIntentActivities(i, 0);
        for (ResolveInfo ri : rList) {
            if (ri.activityInfo.packageName.equals(default_ri.activityInfo.packageName)
                    && ri.activityInfo.name.equals(default_ri.activityInfo.name)) {
                return ri2cn(default_ri);
            } else if ("com.android.browser".equals(ri.activityInfo.packageName)) {
                browser_ri = ri;
            }
        }
        if (browser_ri != null) {
            return ri2cn(browser_ri);
        } else if (rList.size() > 0) {
            return ri2cn(rList.get(0));
        } else if (default_ri == null) {
            return null;
        } else {
            return ri2cn(default_ri);
        }
    }

    private static ComponentName ri2cn(ResolveInfo ri) {
        return new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name);
    }

    @Override
    public void onComplete(String response) {
        Log.e(TAG, "repsonse = " +  response);
        if (postkey.equals("GetOrderXML")) {    // get XML  of OrderInfomation
            try {
                JSONObject xmljson = new JSONObject(response);
                final String orderxml = xmljson.getString("orderxml");
                if (!orderxml.equals("")) {  //  got xml
//                    sendRequestByHTML(orderxml);
//                    goPay(orderxml);
//                    goUrlConnection(orderxml);
                    testpost(orderxml);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFail(String err) {

    }

    // compile 'com.squareup.okhttp3:okhttp:3.10.0'
    private final OkHttpClient client = new OkHttpClient();
    private void makePost(String orderxml){
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("strRqXML", orderxml)
//                .addFormDataPart("name", "your-name")
                .build();

        okhttp3.Request request =  new okhttp3.Request.Builder()
                .url(EPOS + OrderInitPage)
                .post(requestBody)
                .build();


//
//        try (okhttp3.Response response = client.newCall(request).execute()) {
//            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//
//            Headers responseHeaders = response.headers();
//            for (int i = 0; i < responseHeaders.size(); i++) {
//                System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
//            }
//
//            System.out.println(response.body().string());
//        }
    }

    private void gogopay(String orderxml){
        Log.e(TAG, "gogopay " + orderxml);
        final String order_xml = orderxml;
        String url = EPOS + OrderInitPage;
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //This code is executed if the server responds, whether or not the response contains data.
                //The String 'response' contains the server's response.
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            } //Create an error listener to handle errors appropriately.
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("strRqXML", order_xml);           //Add the data you'd like to send to the server.
                return MyData;
            }
        };
        MyRequestQueue.add(MyStringRequest);
    }

    private void goPay(String orderxml) {

//        Intent i = new Intent();
//        i.setAction(Intent.ACTION_VIEW);
//        i.setData(Uri.parse(dataUri));
//        startActivity(i);
//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(EPOS + OrderInitPage));
//        startActivity(intent);
        String uri = Uri.parse(EPOS + OrderInitPage)
                .buildUpon()
                .appendQueryParameter("strRqXML", orderxml)
                .build().toString();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.valueOf(uri)));
        startActivity(browserIntent);   // 系統忙碌中 本筆訂單無法進行付款作業
    }

    private void goUrlConnection(String orderxml){
        try {
            URL url = null;
//            String query = String.format("strRqXML=%s", URLEncoder.encode(orderxml.replaceAll("\\\\", ""), "UTF-8"));
//            String query = String.format("strRqXML=%s", orderxml); //.replaceAll("\\\\", ""));
            String query = "strRqXML=" + orderxml;
            url = new URL(EPOS + OrderInitPage);
            HttpURLConnection urlConn = null;
            urlConn = (HttpURLConnection) url.openConnection();
            // Let the run-time system (RTS) know that we want input.
            urlConn.setDoInput (true);
            // Let the RTS know that we want to do output.
            urlConn.setDoOutput (true);
            // No caching, we want the real thing.
            urlConn.setUseCaches (false);
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("Accept-Charset", "UTF-8");
//            urlConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            // text/plain, text/xml  無效
//            urlConn.setRequestProperty("Content-Length", "" + orderxml.length());

            urlConn.connect();
            DataOutputStream output = null;
            output = new DataOutputStream(urlConn.getOutputStream());
//            OutputStream output = urlConn.getOutputStream();
//            String data = "strRqXML=" + orderxml.replaceAll("\\\\", "");
            Log.e(TAG, query);
            output.writeBytes (query);
            output.flush();
            output.close();
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(java.lang.String.valueOf(url)));
            startActivity(browserIntent);
        } catch (MalformedURLException ex) {
//            String data = "data=Hello+World!";
//            URL url = new URL("http://localhost:8084/WebListenerServer/webListener");
//            HttpURLConnection con = (HttpURLConnection) url.openConnection();
//            con.setRequestMethod("POST");
//            con.setDoOutput(true);
//            con.getOutputStream().write(data.getBytes("UTF-8"));
//            con.getInputStream();
        } catch (IOException ex) {

        }

    }

    private void apacheClient(String orderxml){
//        PostMethod post = new PostMethod(url);
//        RequestEntity entity = new FileRequestEntity(inputFile, "text/xml; charset=ISO-8859-1");
//        post.setRequestEntity(entity);
//        HttpClient httpclient = new HttpClient();
//        int result = httpclient.executeMethod(post);
    }

    private void testpost(String orderxml) {
        Log.e(TAG, "testpost(" + orderxml + ")");
        URL url = null;
        try {
            url = new URL(EPOS + OrderInitPage);
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("strRqXML", orderxml);
            StringBuilder postData = new StringBuilder();
//            postData.append("strRqXML=");
//            postData.append(orderxml);
//            for (Map.Entry<String, Object> param : params.entrySet()) {
//                if (postData.length() != 0) postData.append('&');
////                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
////                postData.append('=');
////                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
//                postData.append(param);
//                postData.append('=');
//                postData.append(param.getValue());
//            }
            postData.append(URLEncoder.encode("strRqXML", "UTF-8"));
            postData.append("=");
            postData.append(URLEncoder.encode(String.valueOf(orderxml), "UTF-8"));
            Log.e(TAG, postData.toString());
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);

            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0;)
                sb.append((char)c);
            String response = sb.toString();
            Log.e(TAG, response);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(java.lang.String.valueOf(url)));
            startActivity(browserIntent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}