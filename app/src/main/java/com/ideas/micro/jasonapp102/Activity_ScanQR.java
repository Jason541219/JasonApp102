package com.ideas.micro.jasonapp102;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class Activity_ScanQR extends AppCompatActivity {
    private SurfaceView surfaceView;
    private TextView txt_scanqr;
    private CameraSource cameraSource;      // 宣告相機
    private BarcodeDetector barcodeDetector;        // 宣告Google 的 Vision 套件

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanqr);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        txt_scanqr = (TextView) findViewById(R.id.txt_scanqr);
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(300, 300)
                .setAutoFocusEnabled(true)
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED)
                    return;
                try{
                    cameraSource.start(holder);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                cameraSource.stop();    // 關閉的同時就會關閉相機
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();
                if (qrCodes.size() !=0) {
                    txt_scanqr.post(new Runnable() {
                        @Override
                        public void run() {
                            txt_scanqr.setText(qrCodes.valueAt(0).displayValue);
                        }
                    });
                }
            }
        });
    }
}