package com.example.john.maptest;

import android.Manifest;
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
import android.net.wifi.ScanResult;
import android.util.TimingLogger;

import 	android.os.Environment;

import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import android.media.MediaScannerConnection;


public class MainActivity extends AppCompatActivity {
    // Lat/Long values that correspond with the floor plans for reference; was more relevant during the first iteration.
    /*final double[] topLeft = {39.510926, -84.733822};
    final double[] topRight = {39.510919, -84.733213};
    final double[] botLeft = {39.510617, -84.733829};
    final double[] botRight = {39.510609, -84.733221};

    final double maxLat = 39.510926;    //  this data was used in order to scale to a map interface before
    final double minLat = 39.510609;    //  the application became more focused on gathering rssi values
    final double maxLong = -84.733829;
    final double minLong = -84.733213;*/

    private Timer mTimer1;  //  originally opted to use threads to process scans, but it was not useful.
    private TimerTask mTt1; //  timer tasks were an easier implementation for repeatedly running code.
    private Handler mTimerHandler = new Handler();

    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11; //  Application requires a number of permissions to run
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 11;
    private static final int MY_PERMISSION_ACCESS_WIFI_STATE = 11;
    private static final int MY_PERMISSION_WRITE_EXTERNAL_STORAGE = 11;
    private static final int MY_PERMISSION_READ_EXTERNAL_STORAGE = 11;
    private int threadCount = 0;    // simple counter used to prevent more than one timer being run at a time
    private File f;
    private double start;   //  start time of scans
    private TimingLogger timer;
    private long timeStart;
    private long timeStop;
    private int written = 0;
    private ArrayList<String> apMac = new ArrayList<String>();  //  data is stored using an ArrayList of ArrayLists
    private ArrayList<ArrayList<Integer>> aprssi = new ArrayList<ArrayList<Integer>>();
    private int samples = 0;    //  keeps track of number of samples collected during use of app
    public String bestProvider;
    private Context mContext;

    private void stopTimer(){
        if(mTimer1 != null){
            if(written == 0){   //  if not written already will automatically attempt to save data
                TextView ssidText = findViewById(R.id.ssid);    // gets current access point the device is connected to if connected.
                File f1 = mContext.getExternalFilesDir(null);
                TextView m = findViewById(R.id.path);   //  the path used is an internal storage space, which requires permissions
                m.setText("" + f1.getPath());
                saveData(); //  sets written to 1 in saveData when written automatically or manually
            }
            mTimer1.cancel();   //  clears timers and resets the count so that new scans may be accomplished
            mTimer1.purge();
            threadCount = 0;
        }
    }

    private void startTimer(){  //  Begins timer task for scanning
        mTimer1 = new Timer();
        mTt1 = new TimerTask() {
            public void run() {
                mTimerHandler.post(new Runnable() {
                    public void run(){
                        getScan();  //  scans access points in range of phone.
                    }
                });
            }
        };
        mTimer1.schedule(mTt1, 0, 100); //  100ms delay to increase frequency of probes
    }

    private void saveData(){    //  method that write data to a .txt file when called
        try {   //  attempts to write to a .txt file that can be parsed later and examined.
            f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),((TextView)findViewById(R.id.filename)).getText().toString()+".txt");
            PrintWriter p = new PrintWriter(f);
            for(int i = 0; i < apMac.size(); i++){
                p.print("<" + apMac.get(i) + "> : ");
                for(int j : aprssi.get(i)){
                    p.print(j + ", ");
                }
                p.print("\n");
            }
            TextView path = findViewById(R.id.path);    //  shows path to user
            path.setText(f.getAbsolutePath());
            p.close();
            written = 1;    //  if file already exists the data will not be automatically saved when timer stops
            MediaScannerConnection.scanFile(mContext, new String[]{f.toString()}, null, null);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {  //  A variety of permissions are required to perform
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

        Button button3 = findViewById(R.id.scan);
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                written = 0;
                TextView path = findViewById(R.id.path);
                f = mContext.getFilesDir();
                path.setText("" + f.getPath());
                aprssi.clear();
                apMac.clear();
                TextView samples = findViewById(R.id.signalStr);
                samples.setText("Results: " + 0);
                if(threadCount < 1) { // max 1 timer
                    threadCount++;
                    start = System.currentTimeMillis();
                    startTimer();
                }
                // Code here executes on main thread after user presses button
            }
        });

        Button button5 = findViewById(R.id.save); // Up
        button5.setOnClickListener(new View.OnClickListener() { //  By pressing the "save" button the file can be overwritten.
            public void onClick(View v) {
                saveData();
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void getScan() {
        TextView runTime = findViewById(R.id.runTime);
        runTime.setText("" + (System.currentTimeMillis() - start)/1000);
        if (System.currentTimeMillis() - start < 60000) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
                WifiManager wifiManager = null;
                wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                TextView ssidText = findViewById(R.id.ssid);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ssidString = wifiInfo.getBSSID();
                ssidText.setText("Current SSID: " + ssidString);
                List<ScanResult> wifiList = null;
                wifiManager.startScan();
                wifiList = wifiManager.getScanResults();

                TextView sig = findViewById(R.id.signalStr);
                TextView ap = findViewById(R.id.ssidList);

                for (ScanResult scanResult : wifiList) {    //  This is the loop that accomplishes the scanning and stores in the ArrayList
                    if (apMac.contains(scanResult.BSSID)) { //  Will add an ArrayList for a new AP or add to an existing one
                        aprssi.get(apMac.indexOf(scanResult.BSSID)).add(scanResult.level);
                    } else {
                        apMac.add(scanResult.BSSID);
                        aprssi.add(new ArrayList<Integer>());
                        aprssi.get(apMac.indexOf(scanResult.BSSID)).add(scanResult.level);
                    }
                    samples++;  //  increases the number of samples
                }

                ap.setText("APS: " + wifiList.size());
                sig.setText("Total: " + apMac.size() + "; Samples: " + samples);
            }
        } else {
            stopTimer();
        }
    }

}
