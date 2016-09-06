package com.example.myaccount.myapp2;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class MapsActivity extends FragmentActivity
{
    public String positionString;
    public GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded()
    {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null)
        {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null)
            {
                mMap.setMyLocationEnabled(true);
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
        }
        else
        {
            runThread();
        }
    }

    private void runThread()
    {
        new Thread()
        {
            public void run()
            {
                try
                {
                    URL url = new URL("http://valmostrato.ddns.net/lastpos.txt");
                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "US-ASCII"));
                    positionString = in.readLine();
                }
                catch (final Exception e)
                {
                    runOnUiThread(new ShowException(e));
                    return;
                }
                try
                {
                    final String[] myArrayString = (positionString.split(","));
                    if ((Double.parseDouble(myArrayString[0]) != 0) && (Double.parseDouble(myArrayString[1]) != 0))
                    {
                        final Double myLat = Double.parseDouble(myArrayString[0]);
                        final Double myLon = Double.parseDouble(myArrayString[1]);
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                LatLng latLng = new LatLng(myLat, myLon);
                                mMap.clear();
                                mMap.addMarker(new MarkerOptions().position(latLng));
                                if (mMap.getCameraPosition().zoom < 15)
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter()
                                {
                                    View markerWindow;
                                    @Override
                                    public View getInfoWindow(Marker marker)
                                    {
                                        return null;
                                    }
                                    @Override
                                    public View getInfoContents(Marker marker)
                                    {
                                        markerWindow = getLayoutInflater().inflate(R.layout.my_layout,null);
                                        TextView altezza        = (TextView)markerWindow.findViewById(R.id.altezza);
                                        TextView velocità_vert  = (TextView)markerWindow.findViewById(R.id.velocità_vert);
                                        TextView velocità_oriz  = (TextView)markerWindow.findViewById(R.id.velocità_oriz);
                                        TextView voltaggio      = (TextView)markerWindow.findViewById(R.id.voltaggio);
                                        TextView giorno         = (TextView)markerWindow.findViewById(R.id.giorno);
                                        TextView ora            = (TextView)markerWindow.findViewById(R.id.ora);
                                        altezza.setText         ("Altezza : " + myArrayString[2] + " " + myArrayString[3] );
                                        velocità_oriz.setText   ("V. verticale : " + myArrayString[5] + " m/s");
                                        velocità_vert.setText   ("V. orizzontale : " + myArrayString[4] + " m/s");
                                        voltaggio.setText       ("Voltaggio batteria : " + myArrayString[7] + " V");
                                        giorno.setText          ("Giorno : " + myArrayString[6].substring(6,8) + "/" + myArrayString[6].substring(4,6   ));
                                        ora.setText             ("Ora : " + myArrayString[6].substring(8,10) + ":" + myArrayString[6].substring(10,12) );
                                        return markerWindow;
                                    }
                                });
                            }
                        });
                    }
                }
                catch (final Exception e)
                {
                    runOnUiThread(new ShowException(e));
                }
            }
        }.start();
    }

    class ShowException implements Runnable
    {
        Exception exception;
        public ShowException(Exception e)
        {
             exception = e;
        }
        @Override
        public void run()
        {
            String text;
            if (exception != null)
              text = exception.toString();
            else
              text = "Null Exception";
            Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
            toast.show();
        }
    }
}