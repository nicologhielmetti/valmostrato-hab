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
                    URL url = new URL("http://valmostrato.altervista.org/lastpos.txt");
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
                        final Double myLat = Double.parseDouble(myArrayString[1]);
                        final Double myLon = Double.parseDouble(myArrayString[0]);
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
                                        TextView nsat           = (TextView)markerWindow.findViewById(R.id.nsat);
                                        TextView velocità       = (TextView)markerWindow.findViewById(R.id.velocità);
                                        TextView temperaturaInt = (TextView)markerWindow.findViewById(R.id.temperaturaInt);
                                        TextView temperaturaExt = (TextView)markerWindow.findViewById(R.id.temperaturaExt);
                                        TextView pressione      = (TextView)markerWindow.findViewById(R.id.pressione);
                                        TextView voltaggio      = (TextView)markerWindow.findViewById(R.id.voltaggio);
                                        TextView giorno         = (TextView)markerWindow.findViewById(R.id.giorno);
                                        TextView ora            = (TextView)markerWindow.findViewById(R.id.ora);
                                        altezza.setText         ("Altezza : " + myArrayString[2] + " m");
                                        nsat.setText            ("Numero satelitti : " + myArrayString[5]);
                                        velocità.setText        ("Velocità : " + myArrayString[6] + " km/h");
                                        temperaturaInt.setText  ("Temperatura interna : " + myArrayString[7] + " °C");
                                        temperaturaExt.setText  ("Temperatura esterna : " + myArrayString[8] + " °C");
                                        pressione.setText       ("Pressione : " + myArrayString[9] + " hPa");
                                        voltaggio.setText       ("Voltaggio batteria : " + myArrayString[10] + " V");
                                        giorno.setText          ("Giorno : " + myArrayString[3]);
                                        ora.setText             ("Ora : " + myArrayString[4]);
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