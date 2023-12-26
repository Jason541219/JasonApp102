package com.ideas.micro.jasonapp102;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class Setting_RememberLogin extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    private TextView tvTitle;
    CheckBox checkbox_rememberaccount;
    Button btn_rememberconfirm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_rememberlogin);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.setting_rememberlogin_title);
        checkbox_rememberaccount = (CheckBox) findViewById(R.id.checkbox_rememberaccount);
        btn_rememberconfirm = (Button) findViewById(R.id.btn_rememberconfirm);
        sharedPreferences = getSharedPreferences("meridianSharedPreferences", MODE_PRIVATE);
        // 讀取上次紀錄，預設為 false
        checkbox_rememberaccount.setChecked(sharedPreferences.getBoolean("memberaccount", false));

        btn_rememberconfirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                sharedPreferences.edit()
                        .putBoolean("memberaccount", checkbox_rememberaccount.isChecked())
                        .commit();
                        Intent intent = new Intent (Setting_RememberLogin.this, Activity_Setting.class);
                        startActivity(intent);
            }
        });

    }
}