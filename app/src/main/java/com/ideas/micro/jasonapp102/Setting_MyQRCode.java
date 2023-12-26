package com.ideas.micro.jasonapp102;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class Setting_MyQRCode extends AppCompatActivity implements onHttpPostCallback {
    private final String TAG="QRCode";
    private TextView tvTitle;
    private ImageView me_qrcode;
    private TextView label_myqrcodecontent1;
    private TextView label_myqrcodecontent2;
    SingleUser user;
    private String postkey;
    private String qrcodecontent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.setting_myqrcode);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.setting_myqrcode_title);
        me_qrcode = (ImageView) findViewById(R.id.me_qrcode);
        label_myqrcodecontent1 = (TextView) findViewById(R.id.label_myqrcodecontent1);
        label_myqrcodecontent2 = (TextView) findViewById(R.id.label_myqrcodecontent2);
        user = SingleUser.getInstance();
        qrcodecontent = user.getUserqrcode();
        Log.e(TAG, "QRCode = " + qrcodecontent);

        if (qrcodecontent.equals("")){          // 如果沒有設定QRCode
            qrcodecontent = Utility.GetRandomString(20);
            Log.e(TAG, "新QRCode = " + qrcodecontent);
            user.setUserqrcode(qrcodecontent);
            JSONObject jqljson = new JSONObject();
            try {
                jqljson.put("command", "UpdateQRCode");
                jqljson.put("clinicnamewi", GlobalVariables.Login_Clinic);
                jqljson.put("id", user.getUserID());
                jqljson.put("qrcode", qrcodecontent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            postkey = "UpdateQRCode";
            HttpPost httpPost = new HttpPost(Setting_MyQRCode.this);
            httpPost.startPost(Setting_MyQRCode.this,  GlobalVariables.http_url, jqljson.toString());
        }
        label_myqrcodecontent1.setText(qrcodecontent.substring(0, 10));
        label_myqrcodecontent2.setText(qrcodecontent.substring(10));

        //String qrcodecontent = "ACBDERNNDCPQRQR";
        Bitmap qrcodeimg = Utility.GetQRCodeImage(qrcodecontent, 512, 512);
        me_qrcode.setImageBitmap(qrcodeimg);
    }

    protected void onStart() {
        super.onStart();
        LogInOut.log("Setting_MyQRCode", true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogInOut.log("Setting_MyQRCode", false);
    }

    @Override
    public void onComplete(String response) {

    }

    @Override
    public void onFail(String err) {

    }
}