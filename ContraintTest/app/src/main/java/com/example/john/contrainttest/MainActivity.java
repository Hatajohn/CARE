package com.example.john.contrainttest;

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

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity {
    final double[] topLeft = {39.510926, -84.733822};
    final double[] topRight = {39.510919, -84.733213};
    final double[] botLeft = {39.510617, -84.733829};
    final double[] botRight = {39.510609, -84.733221};
    final double maxLat = 39.510926;
    final double minLat = 39.510609;
    final double maxLong = -84.733829;
    final double minLong = -84.733213;
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 11;
    private static final int MY_PERMISSION_ACCESS_WIFI_STATE = 11;
    private Thread runner;
    private int threadCount = 0;
    private int floor = 1;
    private LocationManager lm;
    private Location loc;
    public String bestProvider;
    private LocationListener lLoc;
    Context mContext;

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
            TextView lat = findViewById(R.id.textView3);
            TextView lon = findViewById(R.id.textView4);
            TextView x = findViewById(R.id.textView6);
            TextView y = findViewById(R.id.textView7);
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
                    while (runner.isAlive()) {
                        try{
                            runner.sleep(10000);
                        }catch (InterruptedException e) {
                            System.out.println("Interrupted.");
                        }
                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            100,
                            1, lLoc);
                        loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        lat.setText("" + loc.getLatitude());
                        lon.setText("" + loc.getLongitude());

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
                            x.setText("" + (Math.abs((loc.getLongitude() - maxLong) / maxLong * 980 * 1000000 % 1000)));
                            y.setText("" + (Math.abs((loc.getLatitude() - maxLat) / maxLat * 680 * 1000000 % 1000)));
                        }
                    }
                } else{
                    lat.setText("- -");
                    lon.setText("- -");
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

        mContext = this;
        ImageView user = findViewById(R.id.user);
        user.setY(345);
        user.setX(445);
        TextView lat = findViewById(R.id.textView3);
        TextView lon = findViewById(R.id.textView4);
        lat.setText("- -");
        lon.setText("- -");

        Button button1 = findViewById(R.id.Button2); // Down
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView iv = findViewById(R.id.imageView);
                TextView up = findViewById(R.id.textView2);
                if (up.getText().equals("1")){
                    up.setText("0");
                    iv.setImageResource(R.drawable.benton0);
                } else if (up.getText().equals("2")){
                    up.setText("1");
                    iv.setImageResource(R.drawable.benton1);
                }
                // Code here executes on main thread after user presses button
            }
        });

        Button button2 = findViewById(R.id.Button1); // Up
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView iv = findViewById(R.id.imageView);
                TextView up = findViewById(R.id.textView2);
                if (up.getText().equals("1")){
                    up.setText("2");
                    iv.setImageResource(R.drawable.benton2);
                } else if (up.getText().equals("0")){
                    up.setText("1");
                    iv.setImageResource(R.drawable.benton1);
                }
                // Code here executes on main thread after user presses button
            }
        });

        Button button3 = findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                bestProvider = String.valueOf(lm.GPS_PROVIDER);
                TextView lat = findViewById(R.id.textView3);
                TextView lon = findViewById(R.id.textView4);
                ImageView user = findViewById(R.id.user);
                ImageView map = findViewById(R.id.imageView);
                int width = map.getWidth();
                int height = map.getHeight();
                calculateDistance();
                if(threadCount < 2) { // max 2 threads
                    runThread thr = new runThread("locate");
                }
                // Code here executes on main thread after user presses button
            }
        });


        Button button4 = findViewById(R.id.fix); // Up
        button4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView user = findViewById(R.id.user);
                ImageView iv = findViewById(R.id.imageView);
                TextView up = findViewById(R.id.textView2);
                iv.setImageResource(R.drawable.benton1);
                TextView lat = findViewById(R.id.textView3);
                TextView lon = findViewById(R.id.textView4);
                lat.setText("- -");
                lon.setText("- -");
                up.setText("1");
                user.setY(345);
                user.setX(445);
                // Code here executes on main thread after user presses button
            }
        });
    }

    private int getSignalLevel(){
        TextView check = findViewById(R.id.checkE);
        check.setText(""+0);
        if ( ContextCompat.checkSelfPermission( getApplicationContext(), Manifest.permission.ACCESS_WIFI_STATE ) == PackageManager.PERMISSION_GRANTED) {
            WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            int numberOfLevels = 5;
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
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
            int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
            check.setText(""+1);
            return level;
        }
        return -1;
    }

    public double calculateDistance(){
        TextView dist = findViewById(R.id.dist1);
        TextView check = findViewById(R.id.checkE);
        double signalLevelInDb = getSignalLevel();
        if(signalLevelInDb != -1) {
            double exp1 = (27.55 - (20 * Math.log10(2400000)) + Math.abs(signalLevelInDb)) / 20.0;
            double exp2 = (27.55 - (20 * Math.log10(5000000)) + Math.abs(signalLevelInDb)) / 20.0;
            double exp = (Math.pow(10.0, exp1) + Math.pow(10.0, exp2)) / 2.0;
            dist.setText("" + exp);
            check.setText(""+2);
            return exp;
        }else{
            dist.setText("<ERROR>");
            return -1;
        }
    }

}
