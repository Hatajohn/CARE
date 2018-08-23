package com.example.john.maptest;

import android.Manifest;
import android.location.LocationManager;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.TextView;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.util.TimingLogger;

import 	android.os.Environment;

import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import android.media.MediaScannerConnection;


public class MainActivity extends AppCompatActivity {
    // Lat/Long values that correspond with the floor plans for reference
    final double[] topLeft = {39.510926, -84.733822};
    final double[] topRight = {39.510919, -84.733213};
    final double[] botLeft = {39.510617, -84.733829};
    final double[] botRight = {39.510609, -84.733221};

    final double maxLat = 39.510926;
    final double minLat = 39.510609;
    final double maxLong = -84.733829;
    final double minLong = -84.733213;

    private final double c = 0.299792458;   // sol in nanoseconds

    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 11;
    private static final int MY_PERMISSION_ACCESS_WIFI_STATE = 11;
    private static final int MY_PERMISSION_WRITE_EXTERNAL_STORAGE = 11;
    private static final int MY_PERMISSION_READ_EXTERNAL_STORAGE = 11;
    private Thread runner;
    private File f;
    private int threadCount = 0;
    private TimingLogger timer;
    private long timeStart;
    private long timeStop;
    private int written = 0;
    private ArrayList<String> apMac = new ArrayList<String>();
    private ArrayList<ArrayList<Integer>> aprssi = new ArrayList<ArrayList<Integer>>();
    private int samples = 0;
    public String bestProvider;
    private Context mContext;

    public class runThread implements Runnable{
        public runThread(){

        }
        public runThread(String threadName) {
            runner = new Thread(this, threadName); // (1) Create a new thread.
            System.out.println(runner.getName());
            runner.start();
        }

        @Override
        public void run() {
            Looper.prepare();
            if ( ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED
                    && ( ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED )) {
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                long start = System.currentTimeMillis();
                while (runner.isAlive()) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    String ssidString = wifiInfo.getBSSID();
                    TextView s = findViewById(R.id.filename);
                    TextView runTime = findViewById(R.id.runTime);
                    runTime.setText("" + (System.currentTimeMillis() - start)/60);
                    if (isNetworkAvailable() && (System.currentTimeMillis() - start) < 60000) {
                        getScan();
                    }else if(written == 0){
                        TextView ssidText = findViewById(R.id.ssid);
                        File f1 = mContext.getExternalFilesDir(null);
                        TextView m = findViewById(R.id.path);
                        m.setText("" + f1.getPath());
                        ssidText.setText("Current SSID: " + ssidString);
                        if(!f.exists()){
                            try {
                                f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),((TextView)findViewById(R.id.filename)).getText().toString()+".txt");
                                    PrintWriter p = new PrintWriter(f);
                                    for(int i = 0; i < apMac.size(); i++){
                                        p.print("<" + apMac.get(i) + "> : ");
                                        for(int j : aprssi.get(i)){
                                            p.print(j + ", ");
                                        }
                                        p.print("\n");
                                    }
                                    TextView path = findViewById(R.id.path);
                                    path.setText(f.getAbsolutePath());
                                    p.close();
                                    written = 1;
                                    MediaScannerConnection.scanFile(mContext, new String[]{f.toString()}, null, null);
                                } catch(IOException e){
                                    e.printStackTrace();
                                }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    MY_PERMISSION_ACCESS_COARSE_LOCATION );
        }
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                    MY_PERMISSION_ACCESS_FINE_LOCATION );
        }
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_WIFI_STATE ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.ACCESS_WIFI_STATE  },
                    MY_PERMISSION_ACCESS_WIFI_STATE );
        }
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.WRITE_EXTERNAL_STORAGE  },
                    MY_PERMISSION_WRITE_EXTERNAL_STORAGE );
        }
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.READ_EXTERNAL_STORAGE  },
                    MY_PERMISSION_READ_EXTERNAL_STORAGE );
        }
        mContext = this;

        Button button3 = findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                written = 0;
                TextView path = findViewById(R.id.path);
                f = mContext.getFilesDir();
                path.setText("" + f.getPath());
                if(threadCount < 1) { // max 2 threads
                    threadCount++;
                    runThread thr = new runThread("locate");
                }
                // Code here executes on main thread after user presses button
            }
        });

        Button button5 = findViewById(R.id.save); // Up
        button5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView t = findViewById(R.id.filename);
                f = new File(t.toString());
                // Code here executes on main thread after user presses button
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void getScan(){
        if ( ContextCompat.checkSelfPermission( getApplicationContext(), Manifest.permission.ACCESS_WIFI_STATE ) == PackageManager.PERMISSION_GRANTED) {
            WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            List<ScanResult> wifiList = wifiManager.getScanResults();
            int count = 0;
            TextView sig = findViewById(R.id.signalStr);
            TextView ap = findViewById(R.id.ssidList);
            for (ScanResult scanResult : wifiList) {
                if(apMac.contains(scanResult.BSSID)){
                    aprssi.get(apMac.indexOf(scanResult.BSSID)).add(scanResult.level);
                }else{
                    apMac.add(scanResult.BSSID);
                    aprssi.add(new ArrayList<Integer>());
                    aprssi.get(apMac.indexOf(scanResult.BSSID)).add(scanResult.level);
                    count++;
                }
                samples++;
            }
            ap.setText("APS: " + wifiList.size());
            sig.setText("Current: " + count + "; Total: " + apMac.size() + "; Samples: " + samples);
        }
    }

}
