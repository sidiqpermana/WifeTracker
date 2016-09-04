package id.co.nbs.android.wifetracker;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by benjakuben on 12/17/14.
 */
public class FusedLocationUtil implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private boolean isGpsEnabled = false;

    private boolean isNetworkEnabled = false;
    private boolean isBestProviderAvail = false;
    private LocationManager mLocationManager;

    private static final int PLAY_SERVICE_RESOLUTION_REQUEST = 1000;

    public static final String TAG = FusedLocationUtil.class.getSimpleName();

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private LocationCallback mLocationCallback;
    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    public FusedLocationUtil(Context context, LocationCallback callback) {
        mContext = context;
        if (isPlayserviceAvail()) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            mLocationCallback = callback;

            // Create the LocationRequest object
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                    .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        }
    }

    public void connect() {
        mGoogleApiClient.connect();
    }

    public void disconnect() {
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdate();
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            startLocationUpdate();
        } else {
            mLocationCallback.onHandleNewLocation(location);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void stopLocationUpdate() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    public void startLocationUpdate() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution() && mContext instanceof Activity) {
            try {
                Activity activity = (Activity) mContext;
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(activity, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            /*
             * Thrown if Google Play services canceled the original
             * PendingIntent
             */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocationCallback.onHandleNewLocation(location);
    }

    private boolean isPlayserviceAvail() {
        int resultCode = 0;
        GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, (Activity) mContext, PLAY_SERVICE_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(mContext, "This Device is Not Supported", Toast.LENGTH_SHORT).show();
                ((Activity) mContext).finish();
            }
            Log.d(TAG, "result false");
            return false;
        }
        Log.d(TAG, "result true");
        return true;
    }

    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void setmGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }

    public boolean checkProviderAvail() {
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        isGpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (isNetworkEnabled && isGpsEnabled) {
            isBestProviderAvail = true;
            Log.d(TAG, "isBestProviderAvail" + String.valueOf(isBestProviderAvail));
        } else if (!isGpsEnabled || !isNetworkEnabled) {
            isBestProviderAvail = false;
            Log.d(TAG, "isBestProviderAvail" + String.valueOf(isBestProviderAvail));
            setSettingAlert();
        }
        return isBestProviderAvail;
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void setSettingAlert() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Location is Disabled")
                .setMessage("it seems that the location is not enabled on your device, " +
                        "Please go to setting and make sure you have Location Access" +
                        " Enabled")
                .setCancelable(false)
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        mContext.startActivity(intent);
                        builder.setCancelable(true);
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    public interface LocationCallback {
        public void onHandleNewLocation(Location location);
    }
}