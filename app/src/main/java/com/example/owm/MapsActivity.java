package com.example.owm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    FrameLayout fragment_container;
    private Marker currentMarker;
    WeatherListAdapter weatherListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
         fragment_container= findViewById(R.id.fragment_container);
        setSupportActionBar(toolbar);

        // Set up map view
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_view);
        mapFragment.getMapAsync(this);

        // Set up fused location provider client
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getCurrentLocation();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_current_location) {
            getCurrentLocation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }

        // Set up on click listener for map markers

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // Remove the current marker if it exists
                if (currentMarker != null) {
                    currentMarker.remove();
                }

                // Get current weather for clicked location and add marker
                Toast.makeText(getApplicationContext(), "Getting weather for " + latLng.latitude +
                        ", " + latLng.longitude, Toast.LENGTH_SHORT).show();

                //TODO: Implement code to get current weather for clicked location using OpenWeatherMap API
                currentMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Weather for " +
                        latLng.latitude + ", " + latLng.longitude));
                getWeatherInfo(latLng.latitude, latLng.longitude);

                fragment_container.setVisibility(View.VISIBLE);
            }
        });
    }


    private void getCurrentLocation() {
        // Check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Get last known location
            mFusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                            } else {
                                Toast.makeText(getApplicationContext(), "Current location not available",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }
    @SuppressLint("StaticFieldLeak")
    private void getWeatherInfo(double lat, double lng) {
        String apiKey = "9ef2a06136a165ffa412b6a0b318a12f";
        String urlString = "https://api.openweathermap.org/data/2.5/forecast?lat=" + lat + "&lon=" + lng + "&cnt=40&units=metric&appid=" + apiKey;

        new AsyncTask<Void, Void, List<WeatherItem>>() {
            @Override
            protected List<WeatherItem> doInBackground(Void... voids) {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream inputStream = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                    String response = stringBuilder.toString();

                    // Parse the JSON response to get the weather information
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray weatherArray = jsonResponse.getJSONArray("list");
                    List<WeatherItem> weatherItems = new ArrayList<>();

                    for (int i = 0; i < weatherArray.length(); i++) {
                        JSONObject weatherObject = weatherArray.getJSONObject(i);
                        JSONObject main = weatherObject.getJSONObject("main");
                        double temperature = main.getDouble("temp");
                        double pressure = main.getDouble("pressure");
                        double humidity = main.getDouble("humidity");
                        long timestamp = weatherObject.getLong("dt") * 1000L; // Convert timestamp to milliseconds
                        String dtTxt = weatherObject.getString("dt_txt");
                        weatherItems.add(new WeatherItem(timestamp, temperature, pressure, humidity, dtTxt));
                    }


                    return weatherItems;
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<WeatherItem> weatherItems) {
                super.onPostExecute(weatherItems);

                if (weatherItems != null) {
                    // Create and show the weather list fragment
                    WeatherListFragment fragment = new WeatherListFragment();

                    fragment.setWeatherItems(weatherItems);
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.fragment_container, fragment).commit();
                    // Remove the fragment after 5 seconds
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getSupportFragmentManager().beginTransaction()
                                    .remove(fragment).commit();
                            fragment_container.setVisibility(View.GONE);
                        }
                    }, 8000);
                } else {
                    Toast.makeText(MapsActivity.this, "Error getting weather information", Toast.LENGTH_SHORT).show();
                }
            }

        }.execute();
    }




}