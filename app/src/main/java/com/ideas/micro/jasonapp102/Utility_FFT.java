package com.ideas.micro.jasonapp102;

import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility_FFT  extends AppCompatActivity {
    private static final String TAG="Utility_FFT";

    public static int findIndexOfNthChar (String str, String sep, int n){
        int x = str.indexOf(sep);
        if (n == 1) {
            return x;
        } else {
            for (int i=1; i<n; i++){
                x = str.indexOf(sep, x+1);
            }
            return x;
        }
    }

}
