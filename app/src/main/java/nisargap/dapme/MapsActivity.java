package nisargap.dapme;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Handler;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private LocationManager locationManager;

    private static final int LOCATION_REQUEST_CODE = 101;
    private String TAG = "MapDemo";
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    private static final int ZOOM_LEVEL = 15;

    private ArrayList<Marker> mMembers;

    private SyncMapTask mSyncMapTask;

    private class SyncMapTask extends AsyncTask<Void,Void,ArrayList<MarkerOptions> > {


        private ArrayList<MarkerOptions> mMarkerOptions;
        public SyncMapTask(ArrayList<MarkerOptions> markerOptions){

            mMarkerOptions = markerOptions;

        }


        @Override
        protected ArrayList<MarkerOptions> doInBackground(Void... params) {

            return mMarkerOptions;
        }

        protected void onPostExecute(ArrayList<MarkerOptions> result) {

            for(int i = 0; i < result.size(); ++i) {

                newMarkers.add(mMap.addMarker(result.get(i)));

            }
            if(!addedMarkers.isEmpty()){

                for(Marker a : addedMarkers) {

                    a.remove();

                }
                addedMarkers.clear();
            }
            for(Marker a : newMarkers) {

                addedMarkers.add(a);
            }



        }
    }

//    private Marker myMarker;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private ArrayList<MarkerOptions> members;
    private ArrayList<Marker> addedMarkers;
    private ArrayList<Marker> newMarkers;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        members = new ArrayList<>();
        addedMarkers = new ArrayList<>();
        newMarkers = new ArrayList<>();

        SocketUtility.getInstance().listenForNotifications(new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    if(data != null) {

                        notifyDap();


                    }
                } catch (NullPointerException e) {

                }

            }
        });

        SocketUtility.getInstance().listenOnUserData(new Emitter.Listener() {
            @Override
            public void call(Object... args) {


                try {
                    JSONArray data = (JSONArray) args[0];


                    if (!members.isEmpty()) {

                        members.clear();
                    }
                    FirebaseUserAuth userAuth = new FirebaseUserAuth();
                    for (int i = 0; i < data.length(); ++i) {
                        try {
                            JSONObject dataObj = (JSONObject) data.get(i);

                            LatLng loc = new LatLng((double) dataObj.get("lat"),
                                    (double) dataObj.get("lng"));

                            MarkerOptions newMarker = new MarkerOptions().position(loc).icon(BitmapDescriptorFactory.fromResource(R.mipmap.fistbump));


                            String user = dataObj.get("user").toString();

                            if (user.equals(userAuth.getUuid())) {
                                newMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                            }

                            members.add(newMarker);

                            // Async task call
                            mSyncMapTask = new SyncMapTask(members);
                            mSyncMapTask.execute((Void) null);


                        } catch (Exception e) {

                            Log.d("ME", e.getMessage());
                        }

                    }


                } catch (NullPointerException e) {

                    Log.d("ME", e.getMessage());
                }

            }
        });
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                LOCATION_REQUEST_CODE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // TODO: Add websocket handlers here
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }

    protected void requestPermission(String permissionType, int requestCode) {
        int permission = ContextCompat.checkSelfPermission(this,
                permissionType);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{permissionType}, requestCode
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {

                if (grantResults.length == 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Unable to show location - permission required", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.maps_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.back:
                Intent goBack = new Intent(this, MembersViewActivity.class);
                startActivity(goBack);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void sendNotifications(View v) throws JSONException {

        FirebaseUserAuth userAuth = new FirebaseUserAuth();

        SocketUtility.getInstance().sendNotificaction(userAuth.getUuid());

        Log.d("NISARGA", "SENT NOTIFICATION REQUEST!");

    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//
//        myMarker = mMap.addMarker(new MarkerOptions()
//                .position(new LatLng(0, 0))
//                .title("My marker!")
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));

//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myMarker.getPosition(), 15));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if(location != null) {

            currentLocation = location;
            boolean isBetterLoc = isBetterLocation(location, currentLocation);
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL));

        }



        /*
        if (currentLocation == null || isBetterLoc) {
            currentLocation = location;

            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//            myMarker.setPosition(latLng);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        } */
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://nisargap.dapme/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://nisargap.dapme/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("ME", "Location:\nlat: = " + location.getLatitude() + "\nlng = " + location.getLongitude());

        boolean isBetterLoc = isBetterLocation(location, currentLocation);

        if(currentLocation == null) {

            currentLocation = location;

            LatLng latLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());

            try {

                SocketUtility.getInstance().sendUserData(currentLocation.getLatitude(), currentLocation.getLongitude(), "");
                //Log.d("ME", "RIGHT AFTER WE GET INSTANCE AND SET LAT LNG");

            } catch(JSONException e){

                Log.d("ME", e.getMessage());
            }

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL));

            // TODO: Send position to server

        } else {

            currentLocation = location;
            try {
                FirebaseUserAuth userRef = new FirebaseUserAuth();
                SocketUtility.getInstance().sendUserData(currentLocation.getLatitude(), currentLocation.getLongitude(), userRef.getUuid());
                //Log.d("ME", "RIGHT AFTER WE GET INSTANCE AND SET LAT LNG");

            } catch(JSONException e){

                Log.d("ME", e.getMessage());
            }
        }

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

    public void notifyDap() {

        NotificationCompat.Builder mBuilder;

        mBuilder = new NotificationCompat.Builder(MapsActivity.this)
                .setSmallIcon(R.mipmap.fistbump)
                .setContentTitle("Dap Alert!")
                .setContentText("Somebody near you dapped you!");
// Sets an ID for the notification
        int mNotificationId = 001;
// Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }



    @Override
    protected void onResume() {
        super.onResume();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        SocketUtility.getInstance().connect();
        SocketUtility.getInstance().listenOnUserData(new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                try {
                    JSONArray data = (JSONArray) args[0];

                    Log.d("ME", data.toString());

                } catch (NullPointerException e) {

                    Log.d("ME", e.getMessage());
                }

            }
        });
        SocketUtility.getInstance().listenForNotifications(new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject data = (JSONObject) args[0];
                    if(data != null) {

                        notifyDap();
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(400);

                    }
                } catch (NullPointerException e) {

                }

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(this);
        SocketUtility.getInstance().closeConnection();
        SocketUtility.getInstance().stopListenOnUserData();
        SocketUtility.getInstance().stopListeningForNotifications();
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
