package com.cartermooring.restaurantsnearme;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class PlaceSearchActivity extends AppCompatActivity {
    private FusedLocationProviderClient mFusedLocationProviderClient;
    static final String TAG = "PlaceSearchTag";
    static final int LOCATION_REQUEST_CODE = 1;
    static double lat = -1;
    static double longitude = -1;
    int currPlaceIndex = -1;
    List<NearbyPlace> nearbyPlacesList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        //this is to clear the search bar when x button is clicked
        final EditText searchBar = (EditText) findViewById(R.id.searchBar); //create reference to search bar
        Button clearButton = (Button) findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBar.getText().clear();
            }
        });

        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH) {  // so the keyboard action button is a search icon
                    // TODO ​// call your search method here
                    return true;
                }
                return false;
            }
        });


        mFusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this); // get the user's last known location

        setupLastKnownLocation();
        setupUserLocationUpdates();

    }

    //gets users last known location
    private void setupLastKnownLocation() {
        //if we don't have user permission yet
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //get user permission
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE); // shows a dialog for the user to select allow or deny for location permission
        } else {
            // we have permission!!! to access the user's location
            Task<Location> locationTask = mFusedLocationProviderClient.getLastLocation();
            // add a complete/successful/failure listener so we know when the task is done
            locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // at this point the parameter location should store
                    // the users last known location
                    // location could be null if the device does not have a last known location
                    if (location != null) { //display users last known location
                        Log.d(TAG, "onSuccess: " + location.getLatitude() + ", " + location.getLongitude());
                        lat = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            });
        }
    }

    //update users location
    private void setupUserLocationUpdates() {
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000); // request an update every 10 seconds
        locationRequest.setFastestInterval(5000); // handle at most updates every 5 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // most precise

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                LocationCallback locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        Log.d(TAG, "onLocationResult: ");
                        for (Location location : locationResult.getLocations()) {
                            Log.d(TAG, "onSuccess2: " + location.getLatitude() + ", " + location.getLongitude());
                            lat = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                };
                if (ActivityCompat.checkSelfPermission(PlaceSearchActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(PlaceSearchActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_REQUEST_CODE);
                } else {
                    Log.d(TAG, "onSuccess: We have the user's permission");
                    mFusedLocationProviderClient.requestLocationUpdates(locationRequest,
                            locationCallback, null);
                }
            }
        });
    }

    // this method executes when the user responds to the permissions dialog
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // we have the user's permission!!
                setupLastKnownLocation();
            }
        }
    }

    public void receivedNearbyPlaces(List<NearbyPlace> nearbyPlaces) {
        nearbyPlacesList = nearbyPlaces;
        nextPlace();
    }

    public void nextPlace() {
        if (nearbyPlacesList != null & nearbyPlacesList.size() > 0) {
            currPlaceIndex++;
            currPlaceIndex %= nearbyPlacesList.size();

            ListView listView = findViewById(R.id.placesList);

            //nearbyPlacesList = new ArrayList<>(); //create an array for a single list view

            ArrayAdapter<NearbyPlace> arrayAdapter = new ArrayAdapter<NearbyPlace>(this,
                    android.R.layout.simple_list_item_1, //which view is the "main" view
                    nearbyPlacesList) {

                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    //we can set values for subviews of the view
                    //for simple_list_item_1... the view is a textView
                    //first, get the Contact at position
                    NearbyPlace nearbyPlace = nearbyPlacesList.get(currPlaceIndex);//get the index
                    //next, we need a reference to the TextView
//                TextView tv1 = (TextView) view.findViewById(R.id.text); //dont forget view.f
//                tv1.setText(contact.toString());
                    if(convertView == null){
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_row_view, parent, false);
                    }

                    //2. simple_list_item_2
                    //  task, do this! first text view show name, second show phone number
                    TextView tv1 = (TextView) convertView.findViewById(android.R.id.text1);
                    tv1.setText(nearbyPlace.getName() + " " + nearbyPlace.getRating());
                    TextView tv2 = (TextView) convertView.findViewById(android.R.id.text2);
                    tv2.setText(nearbyPlace.getAddress());

                    return convertView;
                }
            };
            listView.setAdapter(arrayAdapter);
        }
    }

    // override a callback that executes whenever an options menu action is clicked
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // get a reference to the MenuInflater
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.updateLocation:
                setupLastKnownLocation();
                return true; // we have consumed/handled this click event
            case R.id.searchButton:
                Toast.makeText(this, "TODO: show preferences", Toast.LENGTH_SHORT).show();
                EditText searchBar = (EditText) findViewById(R.id.searchBar); //create reference to search bar
                String userSearch = searchBar.getText().toString();
                String userLocation = lat + "," + longitude;

                GoogleAPI googleAPI = new GoogleAPI(this, userSearch, userLocation);
                googleAPI.fetchNearbyPlacesList();
                nextPlace();
                ProgressBar progressBar = findViewById(R.id.progressBar);
                progressBar.setVisibility(View.GONE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
