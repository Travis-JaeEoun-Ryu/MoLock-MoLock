package com.example.molockmolock;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.example.molockmolock.data.GridUtil;
import com.example.molockmolock.data.WeatherItem;
import com.example.molockmolock.data.WeatherResult;

import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Weather extends AppCompatActivity implements OnRequestListener,MyApplication.OnResponseListener{
    private static final String TAG = "Weather";
    Location currentLocation;
    GPSListener gpsListener;
    int locationCount=0;
    String currentWeather;
    String currentAddress;
    String currentDateString;
    String currentTemp;
    String currentRainRate;
    String currentWind;
    Date currentDate;
    TextView tvLocation;
    TextView tvUpdatedate;
    TextView tvWeather;
    TextView tvRainRate;
    TextView tvTemp;
    TextView tvWind;
    ImageView ivWind;
    ImageView ivRain;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather);
        tvLocation=findViewById(R.id.address);
        tvUpdatedate=findViewById(R.id.updated_at);
        tvWeather=findViewById(R.id.status);
        tvTemp=findViewById(R.id.temp);
        tvRainRate=findViewById(R.id.rain);
        tvWind=findViewById(R.id.wind);
        ivWind=findViewById(R.id.ivWind);
        ivRain=findViewById(R.id.Ivrain);
        Button btnback=findViewById(R.id.btnBack);
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                String weather=tvWeather.getText().toString();
                intent.putExtra("CurrentWeather",weather);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        onRequest("getCurrentLocation");
    }


    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void onRequest(String command) {
        if (command != null) {
            if (command.equals("getCurrentLocation")) {
                getCurrentLocation();
            }
        }
    }

    public void getCurrentLocation() {
        // set current time
        currentDate = new Date(); //-현재 일자 확인
        currentDateString = AppConstants.dateFormat3.format(currentDate);
        if (currentDateString != null) {
            tvUpdatedate.setText(currentDateString); //fragment2에 setDateString 메소드를 이용해서 현재 일자를 세팅
        }


        //- LocationManager에 현재 위치를 요청
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            currentLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (currentLocation != null) {
                double latitude = currentLocation.getLatitude();
                double longitude = currentLocation.getLongitude();
                String message = "Last Location -> Latitude : " + latitude + "Longitude:" + longitude;
                println(message);
                //-위치 확인 되면 메서드호출
                getCurrentWeather();//-현재위치를 이용해서 날씨확인하는 메서드
                getCurrentAddress();//-현재위치를 이용해서 주소를 확인하는 메서드
            }

            //-GPS리스너 객체는 요청된 위치를 수신하려고 만듬
            gpsListener = new GPSListener();
            long minTime = 10000;
            float minDistance = 0;

            manager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTime, minDistance, gpsListener);

            println("Current location requested.");

        } catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    public void stopLocationService() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            manager.removeUpdates(gpsListener);

            println("Current location requested.");

        } catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    class GPSListener implements LocationListener {
        public void onLocationChanged(Location location) {
            currentLocation = location;

            locationCount++;

            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            String message = "Current Location -> Latitude : "+ latitude + "\nLongitude:"+ longitude;
            println(message);

            getCurrentWeather();
            getCurrentAddress();
        }

        public void onProviderDisabled(String provider) { }

        public void onProviderEnabled(String provider) { }

        public void onStatusChanged(String provider, int status, Bundle extras) { }
    }

    public void getCurrentAddress() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;

        try {
            //-현재 GPS를 통해서 받아온 위도,경도 값을 통해서 현재 주소를 추출
            addresses = geocoder.getFromLocation(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude(),
                    1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (addresses != null && addresses.size() > 0) {
            currentAddress = null; //- currentAddress에 저장된 주소 초기화
            Address address = addresses.get(0);
            if (address.getLocality() != null) {
                currentAddress = address.getLocality(); //-getLocality()메소드를 통해서 도시명(~시)가 추출됨
            }

            //- 주소에 동이 포함되 있다면 주소에 동을 추가
            if (address.getSubLocality() != null) {
                if (currentAddress != null) {
                    currentAddress +=  " " + address.getSubLocality();
                } else {
                    currentAddress = address.getSubLocality();
                }
            }

            String adminArea = address.getAdminArea();//- 시 추출
            String country = address.getCountryName(); //- 국가명 추출
            println("Address : " + country + " " + adminArea + " " + currentAddress);

            if (currentAddress != null) {
                tvLocation.setText(currentAddress);
            }
        }
    }

    public void getCurrentWeather() {
        //-현재 위치의 위도와 경도를 이용해서 GridUtil객체에 만들어서 Map형식으로 저장
        Map<String, Double> gridMap = GridUtil.getGrid(currentLocation.getLatitude(), currentLocation.getLongitude());
        double gridX = gridMap.get("x");
        double gridY = gridMap.get("y");
        println("x -> " + gridX + ", y -> " + gridY);

        sendLocalWeatherReq(gridX, gridY);

    }

    //- 현재위치의 위도 경도를 받아서 double형으로 바꾼후 round(반올림)을 시켜서 url에 추가를시키면 현재 위치의 날씨의 정보들을 MyApplication에서 처리한다.
    public void sendLocalWeatherReq(double gridX, double gridY) {
        String url = "http://www.kma.go.kr/wid/queryDFS.jsp";
        url += "?gridx=" + Math.round(gridX);
        url += "&gridy=" + Math.round(gridY);

        Map<String,String> params = new HashMap<String,String>();

        MyApplication.send(AppConstants.REQ_WEATHER_BY_GRID, Request.Method.GET, url, params, this);
    }


    public void processResponse(int requestCode, int responseCode, String response) {
        if (responseCode == 200) {
            if (requestCode == AppConstants.REQ_WEATHER_BY_GRID) {
                // Grid 좌표를 이용한 날씨 정보 처리 응답
                //println("response -> " + response);

                XmlParserCreator parserCreator = new XmlParserCreator() {
                    @Override
                    public XmlPullParser createParser() {
                        try {
                            return XmlPullParserFactory.newInstance().newPullParser();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                };

                GsonXml gsonXml = new GsonXmlBuilder()
                        .setXmlParserCreator(parserCreator)
                        .setSameNameLists(true)
                        .create();

                WeatherResult weather = gsonXml.fromXml(response, WeatherResult.class);

                // 현재 기준 시간
                try {
                    Date tmDate = AppConstants.dateFormat.parse(weather.header.tm);
                    String tmDateText = AppConstants.dateFormat2.format(tmDate);
                    println("기준 시간 : " + tmDateText);

                    for (int i = 0; i < weather.body.datas.size(); i++) {
                        WeatherItem item = weather.body.datas.get(i);
                        println("#" + i + " 시간 : " + item.hour + "시, " + item.day + "일째");
                        println("  날씨 : " + item.wfKor);
                        println("  기온 : " + item.temp + " C");
                        println("  강수확률 : " + item.pop + "%");

                        println("debug 1 : " + (int)Math.round(item.ws * 10));
                        float ws = Float.valueOf(String.valueOf((int)Math.round(item.ws * 10))) / 10.0f;
                        println("  풍속 : " + ws + " m/s");
                    }

                    // set current weather
                    WeatherItem item = weather.body.datas.get(0);
                    currentWeather = item.wfKor;
                    currentTemp=Double.toString(item.temp);
                    currentWind=Float.toString(Float.valueOf(String.valueOf((int)Math.round(item.ws * 10))));
                    currentRainRate=Integer.toString(item.pop);
                    if (currentWeather != null) {
                        tvWeather.setText(item.wfKor);
                    }
                    if(currentTemp!=null){
                        tvTemp.setText(currentTemp+" C");
                    }
                    if(currentWind!=null){
                        tvWind.setText(currentWind+" m/s");
                    }
                    if(currentRainRate!=null){
                        tvRainRate.setText(currentRainRate+" %");
                    }



                    // stop request location service after 2 times
                    if (locationCount > 1) {
                        stopLocationService();
                    }

                } catch(Exception e) {
                    e.printStackTrace();
                }


            } else {
                // Unknown request code
                println("Unknown request code : " + requestCode);

            }

        } else {
            println("Failure response code : " + responseCode);

        }

    }

    private void println(String data) {
        Log.d(TAG, data);
    }

}












