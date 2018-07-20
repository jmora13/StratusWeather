package com.strat.jose.stratusweather;

import android.Manifest;

import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.strat.jose.stratusweather.R;
import com.strat.jose.stratusweather.data.WeatherOutputData;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class MainActivity2 extends AppCompatActivity {
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private Boolean mLocationPermissionsGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    public Button sendButton;
    TextView  description, tempTextView, cityNameTextView, humidityTextView, pressureTextView, windSpeedTextView;
    public double lat, lon;

    ImageView weatherIcon;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "fonts/Futura Light font.ttf");

        sendButton = findViewById(R.id.sendButton);
        pressureTextView = findViewById(R.id.pressureTextView);
        windSpeedTextView = findViewById(R.id.windSpeedTextView);
        humidityTextView = findViewById(R.id.humidityTextView);
        cityNameTextView = findViewById(R.id.cityNameTextView);
        description = findViewById(R.id.descriptionTextView);
        tempTextView = findViewById(R.id.tempTextView);

        weatherIcon=findViewById(R.id.weatherIcon);

        pressureTextView.setTypeface(custom_font);
        windSpeedTextView.setTypeface(custom_font);
        windSpeedTextView.setTypeface(custom_font);
        humidityTextView.setTypeface(custom_font);
        cityNameTextView.setTypeface(custom_font);
        description.setTypeface(custom_font);
        tempTextView.setTypeface(custom_font);

        getLocationPermission();
        getDeviceLocation();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.openweathermap.org")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        final WeatherService service = retrofit.create(WeatherService.class);

        final WeatherForcast forcast = retrofit.create(WeatherForcast.class);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Call<WeatherOutputData> callRequest = service.getWeatherInfo(Double.toString(lat), Double.toString(lon), "imperial", "c79605804bcd8ab8a364b0ef5310c183");
                Call<WeatherOutputData> callForcastRequest = forcast.getWeatherInfo(Double.toString(lat), Double.toString(lon), "imperial", "c79605804bcd8ab8a364b0ef5310c183");
                callRequest.enqueue(new Callback<WeatherOutputData>() {
                    @Override
                    public void onResponse(Call<WeatherOutputData> call, Response<WeatherOutputData> response) {
                        WeatherOutputData data = response.body();

                        cityNameTextView.setText(data.getName());
                        tempTextView.setText(data.getMain().getTemp() + "°F");
                        description.setText("Clouds: " + data.getWeather().get(0).getDescription());
                        humidityTextView.setText("Humidity: " + data.getMain().getHumidity() + "%");
                        pressureTextView.setText("Pressure: " + data.getMain().getPressure() + "ppi");
                        windSpeedTextView.setText("Wind Speed: " + data.getWind().getSpeed() + " MPH");

                        String icon = data.getWeather().get(0).getIcon();
                        String iconUrl = "http://openweathermap.org/img/w/" + icon + ".png";
                        Picasso.get().load(iconUrl).into(weatherIcon);
                    }

                    @Override
                    public void onFailure(Call<WeatherOutputData> call, Throwable t) {
                    }
                });
            }
        });
    }

        private void getDeviceLocation(){
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            try{
                if(mLocationPermissionsGranted){
                    final Task location = mFusedLocationProviderClient.getLastLocation();
                    location.addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                Location currentLocation = (Location) task.getResult();
                                lat =  currentLocation.getLatitude();
                                lon =  currentLocation.getLongitude();
                            }else{
                                Log.d("null", "onComplete: current location is null");
                            }
                        }
                    });
                }
            }catch (SecurityException e){
                Log.e("tag", "getDeviceLocation: SecurityException: " + e.getMessage() );
            }
        }

    private void getLocationPermission(){
        Log.d("tag", "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;

            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("tag", "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d("tag", "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d("tag", "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                }
            }
        }
    }
}
