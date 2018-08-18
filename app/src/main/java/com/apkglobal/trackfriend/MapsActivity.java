package com.apkglobal.trackfriend;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    double speed = 0.0;
    private TextToSpeech tts;
    LocationManager locationManager;
    private SpeechRecognizer sr;
    static int count =2;
    public int x=1;

    //String loc;

    static final Double EARTH_Radius = 6371.00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        locationManager=(LocationManager) getSystemService(LOCATION_SERVICE);
                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if(x==1){
        Intent intent = new Intent(MapsActivity.this ,TalkBot.class);
        startActivity(intent);
        x=2;}
        /*Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
        sr.startListening(intent);*/
    }
    private void intializeTextToSpeech() {
        tts=new TextToSpeech(this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                if(tts.getEngines().size()==0)
                {
                    Toast.makeText(MapsActivity.this, "Not Available", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else {
                    tts.setLanguage(Locale.US);
                    if (speed >0.4 && count==2) {

                        count++;}
                       else if(speed>0.4 && count==3){

                        speak("Please Maintain the speed This is your first warning");
                            count++;
                        }
                       else if(speed>0.4 && count==4){
                        speak("Please Maintain the speed This is your Second warning Next time you have to pay fine");
                            count++;

                        }
                    else if(speed>0.4 && count==5){
                        speak("Your Challan receipt has been generated");
                        count++;

                    }




                }
              //  mMap.addMarker(new MarkerOptions().position(latLng).title(loc)).setInfoWindowAnchor(speak("the ");

            }
        });
    }
    private void speak(String s) {
        if(Build.VERSION.SDK_INT>=21)
        {
            tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,null);

        }
        else{
            tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,null);
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.clear();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                         int[] grantResults)
            // to handle the case where the user grants the permission. See th
            // e documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, new LocationListener() {
            double old_lat=0.0;
            double old_lng=0.0;

            @Override
            public void onLocationChanged(Location location) {
                double lat, lng;
                lat = location.getLatitude();
                lng = location.getLongitude();

                LatLng latLng = new LatLng(lat,lng);

                Geocoder geocoder = new Geocoder(MapsActivity.this);
                speed=0.0;
                try {
                    List<Address> list = geocoder.getFromLocation(lat,lng,1);
                    String loc = list.get(0).getAddressLine(0);
                   // float speed=location.getSpeed();
                 // boolean sspeed=location.hasSpeed();

                  double dis=CalcDistance(lat,lng,old_lat,old_lng);

                  speed = dis/1.0;
                    Toast.makeText(MapsActivity.this, String.valueOf(speed), Toast.LENGTH_SHORT).show();
                    //Toast.makeText(MapsActivity.this, String.valueOf(sspeed), Toast.LENGTH_SHORT).show();
                    Formatter formatter=new Formatter(new StringBuilder());
                    formatter.format(Locale.US,"%5.1f",speed);
                    String s=formatter.toString();
                    sendloc(loc,s);
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                    mMap.addMarker(new MarkerOptions().position(latLng).title(loc));
                     mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                         @Override
                         public boolean onMarkerClick(Marker marker) {

                             speak("the maximum speed limit of vehicle on this road is 40 kilometer per hour");
                             return false;
                         }
                     });

                    old_lat = lat;
                    old_lng = lng;
                    intializeTextToSpeech();

                } catch (IOException e) {
                    e.printStackTrace();
                }
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
        });






    }

    public double CalcDistance(double lat1,double lon1 ,double lat2 ,double lon2){
        double radius = EARTH_Radius;
        double dlat = Math.toRadians(lat2-lat1);
        double dlon = Math.toRadians(lon2-lon1);
        double a = Math.sin(dlat/2) * Math.sin(dlat/2) + Math.cos(Math.toRadians(lat1 ))*Math.cos(Math.toRadians(lat2))*Math.sin(dlon/2) * Math.sin(dlon/2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return  radius *c;

    }



    private void sendloc(final String loc, final String s) {
        String url="http://searchkero.com/akmsit/insert.php";
        StringRequest stringRequest=new StringRequest(1, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map=new HashMap<>();
                map.put("namekey","");
                map.put("emailkey","");
                map.put("mobilekey",s);
                map.put("aboutkey",loc);
                return map;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
