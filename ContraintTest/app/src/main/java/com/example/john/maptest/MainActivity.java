package com.example.john.maptest;

import android.Manifest;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.location.Location;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.BroadcastReceiver;
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

/*
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.common.GeoPosition;
*/

import java.lang.ref.WeakReference;

//  import java.io.FileNotFoundException;
//  import java.io.PrintWriter;

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
    private Thread runner;
    private File f;
    private int threadCount = 0;
    private int crssi = 0;
    private int rssiCount = 0;
    private int cycle = 0;
    private TimingLogger timer;
    private long timeStart;
    private long timeStop;
    private int written = 0;
    private ArrayList<String> list = new ArrayList<String>();
    private ArrayList<String> apMac = new ArrayList<String>();
    private ArrayList<ArrayList<Integer>> aprssi = new ArrayList<ArrayList<Integer>>();
    private int samples = 0;
    private int floor = 1;
    private LocationManager lm;
    private Location loc;
    public String bestProvider;
    private LocationListener lLoc;
    private int flr = 1;
    private BroadcastReceiver brd;
    private Context mContext;

    //  private PositioningManager pm;
    //  private PositioningManager.OnPositionChangedListener pl;
    //  private GeoPosition geo;

    /* Distance Method

    public double calculateDistance(double signalLevelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }

    */

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
            bestProvider = String.valueOf(lm.GPS_PROVIDER);
            //TextView x = findViewById(R.id.textView6);
            //TextView y = findViewById(R.id.textView7);
            ImageView user = findViewById(R.id.user);
            ImageView map = findViewById(R.id.imageView);
            if ( ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED
                    && ( ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED )) {
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                lLoc = new LocationListener() {
                    @Override
                    public void onLocationChanged(android.location.Location location) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        String msg = "New Latitude: " + latitude + "New Longitude: " + longitude;
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                };
                long start = System.currentTimeMillis();
                while (runner.isAlive()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            100,
                            1, lLoc);
                    loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if (loc.getLatitude() > maxLat) {
                        user.setY((float) (690));
                    } else if (loc.getLatitude() < minLat) {
                        user.setY((float) (0));
                    } else {
                        user.setY((float) (Math.abs((loc.getLatitude() - maxLat) / maxLat * 680 * 1000000 % 1000))); // 690
                    }

                    if (loc.getLongitude() < maxLong) { //  The farther west the lower the value
                        user.setX((float) (0));
                    } else if (loc.getLongitude() > minLong) {
                        user.setX((float) (990));
                    } else {
                        user.setX((float) (Math.abs((loc.getLongitude() - maxLong) / maxLong * 980 * 1000000 % 1000)));  // 990
                        //x.setText("" + (Math.abs((loc.getLongitude() - maxLong) / maxLong * 980 * 1000000 % 1000)));
                        //y.setText("" + (Math.abs((loc.getLatitude() - maxLat) / maxLat * 680 * 1000000 % 1000)));
                    }
                    WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    String ssidString = wifiInfo.getBSSID();
                    TextView s = findViewById(R.id.filename);
                    if (isNetworkAvailable() && System.currentTimeMillis() - start <= 60000) {
                        //calculateDistance(wifiInfo.getFrequency());
                        getScan();
                        TextView ssidText = findViewById(R.id.ssid);
                        File f1 = mContext.getExternalFilesDir(null);
                        TextView m = findViewById(R.id.path);
                        m.setText("" + f1.getPath());
                        ssidText.setText("Current SSID: " + ssidString);
                    }else if(written == 0){
                        if(!f.exists()){
                            try {
                                f = new File(mContext.getExternalFilesDir(null) + "/" + "scan2.txt");
                                f.createNewFile();
                                try {
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
                            }catch (IOException e) {
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
        //  pm = PositioningManager.getInstance();
        //  pm.start(PositioningManager.LocationMethod.GPS_NETWORK);
        //  PositioningManager.getInstance().addListener(new WeakReference<PositioningManager.OnPositionChangedListener>(pl));
        mContext = this;
        ImageView user = findViewById(R.id.user);
        user.setY(345);
        user.setX(445);

        Button button1 = findViewById(R.id.Button2); // Down
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView iv = findViewById(R.id.imageView);
                if (flr == 1){
                    flr = 0;
                    iv.setImageResource(R.drawable.benton0);
                } else if (flr == 2){
                    flr = 1;
                    iv.setImageResource(R.drawable.benton1);
                }
                // Code here executes on main thread after user presses button
            }
        });

        Button button2 = findViewById(R.id.Button1); // Up
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView iv = findViewById(R.id.imageView);
                if (flr == 1){
                    flr = 2;
                    iv.setImageResource(R.drawable.benton2);
                } else if (flr == 0){
                    flr = 1;
                    iv.setImageResource(R.drawable.benton1);
                }
                // Code here executes on main thread after user presses button
            }
        });

        Button button3 = findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                written = 0;
                bestProvider = String.valueOf(lm.GPS_PROVIDER);
                ImageView user = findViewById(R.id.user);
                ImageView map = findViewById(R.id.imageView);
                TextView path = findViewById(R.id.path);
                f = mContext.getFilesDir();
                path.setText("" + f.getPath());
                int width = map.getWidth();
                int height = map.getHeight();
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

    private double getSignalLevel(){
        if ( ContextCompat.checkSelfPermission( getApplicationContext(), Manifest.permission.ACCESS_WIFI_STATE ) == PackageManager.PERMISSION_GRANTED) {
            WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssidString = wifiInfo.getBSSID();
            /*try {
                PrintWriter writer = new PrintWriter("data.txt");
                writer.println("Contents: " + wifiInfo.describeContents());
                writer.println("Freq: " + wifiInfo.getFrequency());
                writer.close();
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }*/
            //  System.out.println("Contents: " + wifiInfo.describeContents());
            //  System.out.println("Freq: " + wifiInfo.getFrequency());
            List<ScanResult> wifiList = wifiManager.getScanResults();
            double level = 0.0;
            double signalLevel;
            TextView sig = findViewById(R.id.signalStr);
            for (ScanResult scanResult : wifiList) {
                signalLevel = WifiManager.calculateSignalLevel(scanResult.level, 5);
                sig.setText("Strength: " + scanResult.level);
                crssi += scanResult.level;
                level += signalLevel;
                rssiCount++;
            }
            level /= wifiList.size();
            crssi /= rssiCount;
            sig.setText("Strength: " + crssi);
            crssi = 0;
            rssiCount = 0;
            return level;
        }
        return -1;
    }

    /*public double calculateDistance(int freq){
        double signalLevelInDb = getSignalLevel();
        if(signalLevelInDb != -1) {
            //time of flight distance
            double distTof = tof()*c/2;

            //free space path loss distance
            double exp1 = (27.55 - (20 * Math.log10(freq)) + Math.abs(signalLevelInDb)) / 20.0;
            double exp2 = Math.pow(10.0, exp1);

            dist.setText("" + exp2);
            check.setText("" + distTof);
            return exp2;
        }else{
            dist.setText("<ERROR>");
            check.setText("<ERROR>");
            return -1;
        }
    }*/

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void onReceive(Context mContext, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
            if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)){
                //do stuff
            } else {
                // wifi connection was lost
            }
        }
    }

    private long tof(){
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        timeStart = System.nanoTime();
        String ssidString = wifiInfo.getBSSID();
        timeStop = System.nanoTime();
        return timeStop - timeStart;
    }

    private void getScan(){
        if ( ContextCompat.checkSelfPermission( getApplicationContext(), Manifest.permission.ACCESS_WIFI_STATE ) == PackageManager.PERMISSION_GRANTED) {
            WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssidString = wifiInfo.getBSSID();
            List<ScanResult> wifiList = wifiManager.getScanResults();
            double signalLevel;
            int count = 0;
            int access;
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
