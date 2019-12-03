package com.cartermooring.restaurantsnearme;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GoogleAPI {
    // wrapper class for all of our Google API related members
    static final String DETAILS_BASE_URL = "https://maps.googleapis.com/maps/api/place/details/json?";
    static final String SEARCH_BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
    static final String API_KEY = "AIzaSyAUZiAsCfcqxmM-79ZIAx1WYWiykQzlbjk"; // BAD PRACTICE!!
    static final String TAG = "GoogleAPITag";
    static String input = "";
    static String location = "";

    PlaceSearchActivity placeSearchActivity;  // for callbacks, because our code
    // is going to run asynchronously

    public GoogleAPI(PlaceSearchActivity placeSearchActivity, String userSearch, String userLocation){
        this.placeSearchActivity = placeSearchActivity;
        this.input = userSearch;
        this.location = userLocation;
        Log.d(TAG, "GoogleAPI: ");
    }

    public void fetchNearbyPlacesList(){
        // need url for request
        String url = constructNearbyPlacesListURL();
        Log.d(TAG, "fetchNearbyPlacesList: " + url);

        // start the background task to fetch the photos
        // we have to use a background task!
        // Android will not let you do any network activity
        // on the main UI thread
        // define a subclass of AsyncTask
        // (async asynchronous which means doesn't wait/block)
        FetchNearbyPlacesListAsyncTask asyncTask = new FetchNearbyPlacesListAsyncTask();
        asyncTask.execute(url);
    }

    public String constructNearbyPlacesListURL() {
        String url = SEARCH_BASE_URL;

        //TODO change these
        url += "&key=" + API_KEY;
        url += "&location=" + location;
        url += "&rankby=distance";
        url += "&keyword=" + input;
        Log.d(TAG, "URl: ");

        return url;
    }

    // parameterized types are
    // argument to doInBackground(): String url
    // argument to publish(): Void (we are not going to update the UI mid task)
    // return value of doInBackground() and the argument to onPostExecute():
    // is our list of sucessfully parsed and created InterestingPhoto objects
    class FetchNearbyPlacesListAsyncTask
            extends AsyncTask<String, Void, List<NearbyPlace>> {//TODO make list nearby place

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // executes on the main UI thread
            // we can update the UI here
            // later, we will show an indeterminate progress bar
//            ProgressBar progressBar = placeSearchActivity.findViewById(R.id.progressBar); //TODO do we do this?
 //           progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<NearbyPlace> doInBackground(String... strings) {
            // executes on the background thread
            // CANNOT update the UI thread
            // this is where we do 3 things
            // 1. open the url request
            // 2. download the JSON response
            // 3. parse the JSON response in to InterestingPhoto objects

            List<NearbyPlace> nearbyPlaceList = new ArrayList<>();
            String url = strings[0]; // ... is call var args, treat like an array

            try {
                // 1. open url request
                URL urlObject = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection) urlObject.openConnection();
                // successfully opened url over HTTP protocol

                // 2. download JSON response
                String jsonResult = "";
                // character by character, we are going to build the json string from an input stream
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    jsonResult += (char) data;
                    data = reader.read();
                }
                Log.d(TAG, "doInBackground: " + jsonResult);

                // 3. parse the JSON
                JSONObject jsonObject = new JSONObject(jsonResult);
                // grab the "root" photos jsonObject
                JSONObject placesObject = jsonObject.getJSONObject("candidates"); // photos is the key
                JSONArray placeArray = placesObject.getJSONArray("candidates"); // photo is the key
                for (int i = 0; i < placeArray.length(); i++) {
                    JSONObject singlePlaceObject = placeArray.getJSONObject(i);
                    // try to a parse a single photo info
                    NearbyPlace nearbyPlace = parseNearbyPlace(singlePlaceObject);
                    if (nearbyPlace != null) {
                        nearbyPlaceList.add(nearbyPlace);
                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return nearbyPlaceList;
        }

        private NearbyPlace parseNearbyPlace(JSONObject singlePlaceObject) {
            NearbyPlace nearbyPlace = null;

            try {
                String name = singlePlaceObject.getString("name"); // name is the key
                String formatted_address = singlePlaceObject.getString("formatted_address");
                String rating = singlePlaceObject.getString("rating");
                nearbyPlace = new NearbyPlace(name, formatted_address, rating);
            } catch (JSONException e) {
                // do nothing
            }

            return nearbyPlace;
        }

        @Override
        protected void onPostExecute(List<NearbyPlace> nearbyPlaces) {
            super.onPostExecute(nearbyPlaces);
            // executes on the main UI thread
            // after doInBackground() is done
            // update the main UI thread with the result of doInBackground()
            // interestingPhotos
            Log.d(TAG, "onPostExecute: " + nearbyPlaces);
            Log.d(TAG, "onPostExecute: " + nearbyPlaces.size());
            placeSearchActivity.receivedNearbyPlaces(nearbyPlaces);

            ProgressBar progressBar = placeSearchActivity.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE);
        }
    }


    //TODO do we need to changevvvvvvallbelowvvvvvv
    public void fetchPlaceBitmap(String placeURL) {
        PlaceRequestAsyncTask asyncTask = new PlaceRequestAsyncTask();
        asyncTask.execute(placeURL);
    }


    class PlaceRequestAsyncTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

          ProgressBar progressBar = (ProgressBar) placeSearchActivity.findViewById(R.id.progressBar);
          progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bitmap = null;

            try {
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection)
                        url.openConnection();

                InputStream in = urlConnection.getInputStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            ProgressBar progressBar = (ProgressBar) placeSearchActivity.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE);
            //placeSearchActivity.receivedNearbyPlaces(bitmap);
        }
    }
}
