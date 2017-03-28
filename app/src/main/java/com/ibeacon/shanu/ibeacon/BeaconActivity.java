package com.ibeacon.shanu.ibeacon;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.RangedBeacon;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static android.R.id.list;

public class BeaconActivity extends AppCompatActivity implements BeaconConsumer,View.OnClickListener {
    BeaconManager beaconManager;
    Button register;
    Button verify;
    EditText t1;
    Map<String,Double> uuids=new HashMap<>();
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean permission=checkPermission();
        if(permission==false)
            requestPermission();
        setContentView(R.layout.activity_beacon);
        listView= (ListView) findViewById(R.id.showdistance);
        register= (Button) findViewById(R.id.button);
        verify= (Button) findViewById(R.id.button2);
        t1= (EditText) findViewById(R.id.editText);
        register.setOnClickListener(this);
        verify.setOnClickListener(this);
/*--------------------------------------------------scheduler for clearing map data---------------------------------------------------------------------*/
       /* Calendar calendar1 = Calendar.getInstance();
        calendar1.set(Calendar.HOUR_OF_DAY, 10);
        calendar1.set(Calendar.MINUTE, 51);
        calendar1.set(Calendar.SECOND, 0);
        Date midtime1 = calendar1.getTime();
        Timer time1=new Timer();
        time1.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("message","UUID clear");
                uuids.clear();
            }
        },midtime1);*/
/*----------------------------------------------------schedule for verification-------------------------------------------------------------------------*/
       /* Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 24);
        calendar.set(Calendar.SECOND, 0);
        Date midtime = calendar.getTime();
        Timer time=new Timer();
        time.schedule(new TimerTask() {
            @Override
            public void run() {
               sendData();
            }
        },midtime);*/

        Thread t1=new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(3000);
                        studentOutOfRange("", 0,0.00);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
       t1.start();
        beaconManager=BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
    /*  beaconManager.setDebug(true);*/
        RangedBeacon.setSampleExpirationMilliseconds(5000);
        beaconManager.setBackgroundBetweenScanPeriod(20000L);
        beaconManager.setBackgroundScanPeriod(3000L);
        beaconManager.bind(this);
        Log.d("message", "bind beacon manager");
    }

    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED){

            return true;

        } else {

            return false;

        }
    }
    private void requestPermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){

            Toast.makeText(this,"GPS permission allows us to access location data. Please allow in App Settings for additional functionality.",Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this,"Permission Granted, Now you can access location data.",Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(this,"Permission Denied, You cannot access location data.",Toast.LENGTH_LONG).show();

                }
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.d("message","properly bind");
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                for(Beacon beacon:beacons) {
                    Log.i("Beacons", "id="+beacon.getId1()+" beacon I see is about "+beacon.getDistance()+" meters away."+"mac="+beacon.getBluetoothAddress());
                    uuids.put(beacon.getBluetoothAddress(),beacon.getDistance());
                    if(beacons.iterator().next().getDistance()<0.40) {
                      /*  sendDataToStudent(beacons.iterator().next().getBluetoothAddress());*/
                    }
                    /*---------------------------------we have to call this function into timerTask------------------*/
                    /*studentOutOfRange(beacons.iterator().next().getId1().toString(), 0,beacons.iterator().next().getDistance());*/
                   /* if(beacons.iterator().next().getDistance()>4.00)
                        studentOutOfRange(beacons.iterator().next().getId1().toString());*/
                   }
            }
        });


        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void sendData()
    {
        Log.d("message", "verify button calling");

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://192.168.1.101:8080/OpenSIS/verifyUUID.html");

        try {
            for (Map.Entry<String,Double> s : uuids.entrySet()) {
                Log.d("UUID", s.getKey());
                if(s.getValue()>4.00) {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                    nameValuePairs.add(new BasicNameValuePair("UUID", s.getKey()));
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpClient.execute(httpPost);
                }
            }
            uuids.clear();
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }catch(ClientProtocolException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void sendDataToStudent(String s)
    {
        Log.d("message", "verify button calling"+s);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://192.168.1.101:8080/OpenSIS/studentUUID.htm");

        try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("UUID", s));
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpClient.execute(httpPost);
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }catch(ClientProtocolException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void studentOutOfRange(String s,Integer i,Double d)
    {
        Log.d("message", "verify button calling"+s+"int="+i);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        for (Map.Entry<String, Double> stu : uuids.entrySet()) {
            if (stu.getValue() > 2.00) {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://52.38.90.246:8080/OpenSIS/studentOutOfRange.htm");

                try {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                    nameValuePairs.add(new BasicNameValuePair("UUID",stu.getKey()));
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpClient.execute(httpPost);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://52.38.90.246:8080/OpenSIS/studentInRange.htm");

                try {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                    nameValuePairs.add(new BasicNameValuePair("UUID", stu.getKey()));
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpClient.execute(httpPost);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
            switch (v.getId()) {
                case R.id.button:
                    WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
                    WifiInfo winfo = wm.getConnectionInfo();
                    String mac = winfo.getMacAddress();
                    String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                    String name = t1.getText().toString();
                    Log.d("message", "clicked....." + name);
                    HttpClient httpclient = new DefaultHttpClient();
                    Log.d("message", "c1");
                    HttpPost httppost = new HttpPost("http://52.38.90.246:8080/OpenSIS/addBeaconDevice.htm");
                    try {
                        // Add your data
                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                        nameValuePairs.add(new BasicNameValuePair("name", name));
                        nameValuePairs.add(new BasicNameValuePair("ip", ip));
                        nameValuePairs.add(new BasicNameValuePair("mac", mac));
                        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                        Log.d("message", "c3");
                        // Execute HTTP Post Request
                        HttpResponse response = httpclient.execute(httppost);
                        Log.d("message", response.toString() + "posted on url");
                        Toast.makeText(BeaconActivity.this, "Device Registered", Toast.LENGTH_SHORT).show();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                case R.id.button2:
                   String[] arrayString=new String[uuids.size()];
                    int i=0;
                    for (Map.Entry<String, Double> stu : uuids.entrySet()) {
                        arrayString[i]=stu.getKey()+"  Distance="+stu.getValue();
                        Log.d("message",stu.getKey()+"  Distance="+stu.getValue());
                        i++;
                    }
                    ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(BeaconActivity.this,android.R.layout.simple_list_item_1,arrayString);
                    listView.setAdapter(arrayAdapter);
            break;
        }
    }
}

