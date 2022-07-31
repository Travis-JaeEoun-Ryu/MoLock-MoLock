package com.example.molockmolock;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private FragmentManager fragmentManager;
    private MapFragment mapFragment;
    private GoogleMap googleMap;
    Button button2;
    GoogleMap mMap;
    private final String DEFAULT = "DEFAULT";
    private static final String TAG_RESULTS = "result";
    private static final String TAG_latitude = "latitude";
    private static final String TAG_longitude = "longitude";
    private String lat = "37.5283169";
    private String lng = "126.9294254";
    public String myJSON;
    public ArrayList<HashMap<String, String>> personList;
    JSONArray peoples = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        personList = new ArrayList<HashMap<String, String>>();
        getData("http://3.144.90.77/location.php");
        Button button = (Button) findViewById(R.id.btn_1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Lock.class);
                startActivity(intent);
            }
        });
        Button button1 = (Button) findViewById(R.id.btn_2);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Alram1.class);
                startActivity(intent);
            }
        });
        button2 = findViewById(R.id.btn_3);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Weather.class);
                startActivity(intent);
            }
        });
    }

    public void onButton(View view)
    {
        Intent intent = new Intent(getApplicationContext(),Alram1.class);//
        startActivity(intent);

    }


    public void showList(){
        try{
            JSONObject jsonObj = new JSONObject(myJSON);
            peoples = jsonObj.getJSONArray(TAG_RESULTS);

            for(int i=0; i<peoples.length();i++){
                JSONObject c =peoples.getJSONObject(i);
                String latitude = c.getString(TAG_latitude);
                String longitude = c.getString(TAG_longitude);

                HashMap<String,String>persons=new HashMap<>();
                persons.put(TAG_latitude,latitude);
                persons.put(TAG_longitude,longitude);

                personList.add(persons);
            }
        }   catch (JSONException e){
            e.printStackTrace();
        }

    }


    public void getData(String url){
        class GetDataJSON extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String...params){
                String uri = params[0];

                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();

                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }

                    return sb.toString().trim();
                } catch(Exception e){
                    return null;
                }

            }
            @Override
            protected void onPostExecute(String result){
                myJSON = result;
//                showList();
                fragmentManager = getFragmentManager();
                mapFragment = (MapFragment)fragmentManager.findFragmentById(R.id.googleMap);
                mapFragment.getMapAsync(MainActivity.this);
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(MainActivity.this);
            }

        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);
    }





    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        String weather=intent.getStringExtra("CurrentWeather");
        button2.setText("현재 날씨는 :"+weather);

        super.onNewIntent(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng location = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,16));


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        } else {
//            checkLocationPermissionWithRationale();
        }
        double latitude = 0.;
        double longitude = 0.;
        try{
            JSONObject jsonObj = new JSONObject(myJSON);
            peoples = jsonObj.getJSONArray(TAG_RESULTS);

            JSONObject c =peoples.getJSONObject(peoples.length()-1);
            latitude = Double.parseDouble(c.getString(TAG_latitude));
            longitude = Double.parseDouble(c.getString(TAG_longitude));
        }   catch (JSONException e) {
            e.printStackTrace();
        }

        MarkerOptions marker = new MarkerOptions();
        marker.position(new LatLng(latitude, longitude))
                .title("내 모빌리티 위치")
                .snippet(String.valueOf(latitude) + ',' + String.valueOf(longitude));
        googleMap.addMarker(marker).showInfoWindow();
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermissionWithRationale() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("위치정보")
                        .setMessage("이 앱을 사용하기 위해서는 위치정보에 접근이 필요합니다. 위치정보 접근을 허용하여 주세요.")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        }).create().show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        googleMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
}