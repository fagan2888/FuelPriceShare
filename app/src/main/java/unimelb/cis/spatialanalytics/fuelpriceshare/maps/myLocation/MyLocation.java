package unimelb.cis.spatialanalytics.fuelpriceshare.maps.myLocation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;

/**
 * Created by Yu Sun on 26/02/2015.
 * Used exclusively (only) for ContributePriceFragment to get current user location
 * when choosing the fuel stations nearby.
 */
public class MyLocation {

    private static LocationManager locationManager;
    private static Context context;
    private static  String TAG=MyLocation.class.getSimpleName();

    public MyLocation (Context context)
    {
        this.context=context;
        this.locationManager= (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        //isGPSEnabled();

    }

    /**
     * TODO add comments
     * @return
     */
    public static Location getMyLocation(){
        //before fetching the location, we need to enable the GPS server.
        //isGPSEnabled();

        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();
        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);


        // Get the initial Current Location
        Location myLocation = locationManager.getLastKnownLocation(provider);
        return myLocation;
    }

    public static void isGPSEnabled()
    {
        Log.e(TAG,"check GPS");
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //Ask the user to enable GPS
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Location Manager");
            builder.setMessage("Would you like to enable GPS?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Launch settings, allowing user to make a change
                    Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(i);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //No location service, no Activity
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }
    }

}
