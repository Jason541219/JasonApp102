package com.ideas.micro.jasonapp102;

import android.widget.Adapter;
import android.widget.ArrayAdapter;

public class MedGlobalStates {
    private static MedGlobalStates instance;
    private int data = 0;
    private String clinicName = "";
    private String usr = "";
    private String pas = "";
    private ArrayAdapter arrayAdapter = null;

    public void setData(int d){
        this.data = d;
    }
    public int getData(){
        return this.data;
    }

//    public void setArrayAdapter(ArrayAdapter arrayAdapter)
//    {
//        this.arrayAdapter = arrayAdapter;
//    }
//    public ArrayAdapter getArrayAdapter()
//    {
//        return this.arrayAdapter;
//    }

    public void setClinicName(String clinicName)
    {
        this.clinicName = clinicName;
    }
    public String getClinicName()
    {
        return this.clinicName;
    }

    public void setUsrPas(String usr, String pas )
    {
        this.usr = usr;
        this.pas = pas;
    }

    public String getUsr()
    {
        return this.usr;
    }

    public String getPas()
    {
        return this.pas;
    }

    public static synchronized MedGlobalStates getInstance(){
        if(instance==null){
            instance=new MedGlobalStates();
        }
        return instance;
    }

    private MedGlobalStates() { }
}

