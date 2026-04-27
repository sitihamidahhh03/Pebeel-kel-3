package com.example.monika.konten_dashboard;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.monika.R;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherManager {

    private final Activity activity;
    private final TextView tvLokasi, tvSuhu, tvDeskripsi, tvRentang, tvKelembaban, tvFeelsLike;
    private final ImageView ivIcon;
    
    private final String API_KEY = "6603e1c52e3779e26850b960a9de282b";
    private final String DEFAULT_CITY = "Benua Raya,ID";
    
    private final ExecutorService executor;
    private final Handler mainHandler;

    public WeatherManager(View rootView) {
        this.activity = (Activity) rootView.getContext();
        
        tvLokasi = rootView.findViewById(R.id.tvLokasiCuaca);
        tvSuhu = rootView.findViewById(R.id.tvSuhuUtama);
        tvDeskripsi = rootView.findViewById(R.id.tvDeskripsiCuaca);
        tvRentang = rootView.findViewById(R.id.tvRentangSuhu);
        tvKelembaban = rootView.findViewById(R.id.tvKelembabanCuaca);
        tvFeelsLike = rootView.findViewById(R.id.tvFeelsLike);
        ivIcon = rootView.findViewById(R.id.ivIconCuacaUtama);
        
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        
        startWeatherProcess();
    }

    private void startWeatherProcess() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            fetchWeatherData(null); 
        } else {
            getLocationAndFetch();
        }
    }

    private void getLocationAndFetch() {
        try {
            LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager == null) return;

            Location location = null;
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                
                if (location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }

                if (location != null && isLocationValid(location)) {
                    fetchWeatherData(location);
                } else {
                    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                        @Override public void onLocationChanged(@NonNull Location loc) {
                            if (isLocationValid(loc)) fetchWeatherData(loc);
                        }
                        @Override public void onStatusChanged(String p, int s, Bundle e) {}
                        @Override public void onProviderEnabled(@NonNull String p) {}
                        @Override public void onProviderDisabled(@NonNull String p) {}
                    }, Looper.getMainLooper());
                    
                    fetchWeatherData(null);
                }
            }
        } catch (Exception e) {
            fetchWeatherData(null);
        }
    }

    private boolean isLocationValid(Location loc) {
        return loc.getLatitude() != 0 && loc.getLongitude() != 0 && Math.abs(loc.getLatitude()) > 0.1;
    }

    public void fetchWeatherData(Location location) {
        executor.execute(() -> {
            try {
                String apiUrl;
                String displayCityName = "";

                if (location != null) {
                    apiUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" + location.getLatitude() + "&lon=" + location.getLongitude() + "&units=metric&appid=" + API_KEY + "&lang=id";
                    
                    try {
                        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            String locality = addresses.get(0).getLocality();
                            String subAdmin = addresses.get(0).getSubAdminArea(); // Corrected method name
                            displayCityName = (locality != null) ? locality : subAdmin;
                        }
                    } catch (Exception ignored) {}
                } else {
                    apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + DEFAULT_CITY + "&units=metric&appid=" + API_KEY + "&lang=id";
                    displayCityName = "Benua Raya";
                }

                HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
                if (connection.getResponseCode() == 200) {
                    InputStream responseBody = connection.getInputStream();
                    JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(responseBody, StandardCharsets.UTF_8)).getAsJsonObject();
                    
                    final String finalName = displayCityName == null || displayCityName.isEmpty() ? jsonObject.get("name").getAsString() : displayCityName;
                    JsonObject main = jsonObject.getAsJsonObject("main");
                    double temp = main.get("temp").getAsDouble();
                    double tempMin = main.get("temp_min").getAsDouble();
                    double tempMax = main.get("temp_max").getAsDouble();
                    double feelsLike = main.get("feels_like").getAsDouble();
                    int humidity = main.get("humidity").getAsInt();
                    
                    JsonObject weather = jsonObject.getAsJsonArray("weather").get(0).getAsJsonObject();
                    String desc = weather.get("description").getAsString();
                    String condition = weather.get("main").getAsString();

                    mainHandler.post(() -> {
                        if (finalName != null && (finalName.equalsIgnoreCase("Pulau Biruang") || finalName.contains("Ocean"))) {
                            if (tvLokasi != null) tvLokasi.setText("📍 Benua Raya");
                        } else {
                            if (tvLokasi != null) tvLokasi.setText("📍 " + finalName);
                        }
                        updateUI(temp, tempMin, tempMax, desc, humidity, feelsLike, condition);
                    });
                }
            } catch (Exception e) {
                Log.e("WeatherError", "Gagal mengambil data: " + e.getMessage());
            }
        });
    }

    private void updateUI(double temp, double min, double max, String desc, int hum, double feels, String condition) {
        if (tvSuhu != null) tvSuhu.setText(activity.getString(R.string.temp_format, Math.round(temp)));
        if (tvDeskripsi != null) tvDeskripsi.setText(capitalize(desc));
        
        if (tvRentang != null) {
            long minVal = Math.round(min);
            long maxVal = Math.round(max);
            if (minVal == maxVal) {
                tvRentang.setText(activity.getString(R.string.temp_range_format, minVal - 1, maxVal + 2));
            } else {
                tvRentang.setText(activity.getString(R.string.temp_range_format, minVal, maxVal));
            }
        }
        
        if (tvKelembaban != null) tvKelembaban.setText(activity.getString(R.string.humidity_format, hum));
        if (tvFeelsLike != null) tvFeelsLike.setText(activity.getString(R.string.feels_like_format, Math.round(feels)));
        
        if (ivIcon != null) {
            if (condition.equalsIgnoreCase("Rain")) ivIcon.setImageResource(R.drawable.ic_water_drop);
            else if (condition.equalsIgnoreCase("Clouds")) ivIcon.setImageResource(R.drawable.ic_cloud);
            else if (condition.equalsIgnoreCase("Clear")) ivIcon.setImageResource(R.drawable.ic_sun);
            else ivIcon.setImageResource(R.drawable.ic_cloud);
            ivIcon.setImageTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.syam_green_medium)));
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
