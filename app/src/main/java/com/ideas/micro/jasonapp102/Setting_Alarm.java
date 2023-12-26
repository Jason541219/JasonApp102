package com.ideas.micro.jasonapp102;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Setting_Alarm extends AppCompatActivity {
    private final String TAG ="Alarm";
    private EditText input_alarmtitle;
    private EditText input_alarmtime;
    private Button btn_confirmalarm;
    private TextView tvTitle;
    TimePickerDialog timepicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_alarm);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.setting_alarm_title);
        Log.e(TAG, "onCreate");
        input_alarmtitle = (EditText) findViewById(R.id.input_alarmtitle);
        input_alarmtime = (EditText) findViewById(R.id.input_alarmtime);
        // 點選生日 EditText 彈出日期選擇器
        input_alarmtime.setInputType(InputType.TYPE_NULL);
        input_alarmtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int hour = cldr.get(Calendar.HOUR_OF_DAY);
                int minu = cldr.get(Calendar.MINUTE);
                // time picker dialog
                timepicker = new TimePickerDialog(Setting_Alarm.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int h, int m) {
                                String hh = h < 10 ? ("0" + h) : String.valueOf(h);
                                String mm = m < 10 ? ("0" + m) : String.valueOf(m);
                                input_alarmtime.setText(hh + ":" + mm);    // 這是設定過後的字樣內容
                            }
                        },
                        hour, minu, true);
                timepicker.show();
            }
        });

        btn_confirmalarm = (Button) findViewById(R.id.btn_confirmalarm);
        btn_confirmalarm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (input_alarmtitle.getText().toString().equals("")){
                    Utility.showAlert(Setting_Alarm.this, R.string.dialog_msg_alerttitleempty);
                } else if (input_alarmtime.getText().toString().equals("")){
                    Utility.showAlert(Setting_Alarm.this, R.string.dialog_msg_alerttimeempty);
                } else {
                    // <uses-permission android:name="com.android.alarm.permission.SET_ALARM"/>
                    Log.e(TAG, "設定時間 = " + input_alarmtime.getText().toString());
                    String alarmtime = input_alarmtime.getText().toString();
                    int h = Integer.parseInt(alarmtime.substring(0, 2));
                    int m = Integer.parseInt(alarmtime.substring(3));
                    Log.e(TAG, "時" + h);
                    Log.e(TAG, "分" + m);
                    Intent intentalarm = new Intent(AlarmClock.ACTION_SET_ALARM);
                    intentalarm.putExtra(AlarmClock.EXTRA_MESSAGE, input_alarmtitle.getText().toString());
                    intentalarm.putExtra(AlarmClock.EXTRA_HOUR, h);
                    intentalarm.putExtra(AlarmClock.EXTRA_MINUTES, m);
                    startActivity(intentalarm);
                }
            }
        });
    }
}