package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.weatherapp.Common.Common;
import com.example.weatherapp.Helper.Helper;
import com.example.weatherapp.Model.OpenWeatherMap;
import com.example.weatherapp.Model.Weather;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class MainActivity extends AppCompatActivity implements LocationListener {


    TextView txtcity, txtLastUpdate, txtDescription, txtHumidity, txtTime, txtCelsius;
    ImageView imageView;
    Button button;
    LocationManager locationManager;
    String provider;
    static double lat, lng;
    OpenWeatherMap openWeatherMap = new OpenWeatherMap();
    int MY_PERMISSION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtcity = (TextView) findViewById(R.id.txtcity);
        txtLastUpdate = (TextView) findViewById(R.id.txtLastUpdate);
        txtDescription = (TextView) findViewById(R.id.txtDescription);
        txtHumidity = (TextView) findViewById(R.id.txtHumidity);
        txtTime = (TextView) findViewById(R.id.txtTime);
        txtCelsius = (TextView) findViewById(R.id.txtCelsius);
        imageView = (ImageView) findViewById(R.id.imageView);



        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);


        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.SYSTEM_ALERT_WINDOW,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            },MY_PERMISSION);
        }
        Location location = locationManager.getLastKnownLocation(provider);
        if (location == null)
            Log.e("TAG", "No Location");



    }


    @Override
    protected void onPause() {
        super.onPause();

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.SYSTEM_ALERT_WINDOW,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            },MY_PERMISSION);
        }
        locationManager.removeUpdates(this);


    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.SYSTEM_ALERT_WINDOW,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            },MY_PERMISSION);
        }
        locationManager.requestLocationUpdates("gps", 400, 1, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        lat=location.getLatitude();
        lng=location.getLongitude();

        new getWeather().execute(Common.apiRequest(String.valueOf(lat),String.valueOf(lng)));

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private class getWeather extends AsyncTask<String,Void,String>{

        ProgressDialog pd= new ProgressDialog(MainActivity.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setTitle("Please Wait");
            pd.show();
        }


        @Override
        protected String doInBackground(String... params) {
            String stream=null;
            String urlstring=params[0];
            Helper http= new Helper();
            stream=http.getHTTPData(urlstring);
            return stream;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(s.contains("Error: Not found city")){
                pd.dismiss();
                return;
            }
            Gson gson=new Gson();
            Type mtype=new TypeToken<OpenWeatherMap>(){}.getType();
            openWeatherMap=gson.fromJson(s,mtype);
            pd.dismiss();

            txtcity.setText(String.format("%s,%s",openWeatherMap.getName(),openWeatherMap.getSys().getCountry()));
            txtLastUpdate.setText(String.format("Last Updated:%s", Common.getDateNow()));
            txtDescription.setText(String.format("%s",openWeatherMap.getWeather().get(0).getDescription()));
            txtHumidity.setText(String.format("%d%%",openWeatherMap.getMain().getHumidity()));
            txtTime.setText(String.format("%s/%s",Common.unixTimeStampToDateTime(openWeatherMap.getSys().getSunrise()),Common.unixTimeStampToDateTime(openWeatherMap.getSys().getSunset())));
            txtCelsius.setText(String.format("%.2f°C",openWeatherMap.getMain().getTemp()));
            Picasso.with(MainActivity.this)
                    .load(Common.getImage(openWeatherMap.getWeather().get(0).getIcon()))
                    .into(imageView);


        }
    }
}
