package id.co.nbs.android.wifetracker;

import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class TrackingService extends GcmTaskService {
    private FusedLocationUtil fusedLocationUtil;
    private FusedLocationUtil.LocationCallback locationCallback;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private AppPreference mAppPreference;
    public static String TAG_TASK_PRIODDIC_LOG = "PeriodicTask";
    public TrackingService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mAppPreference = new AppPreference(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        int result = 0;
        if (taskParams.getTag().equals(TAG_TASK_PRIODDIC_LOG)){
            postCurrentUserLocation();
            result = GcmNetworkManager.RESULT_SUCCESS;
        }
        return result;
    }

    private void postCurrentUserLocation(){
        locationCallback = new FusedLocationUtil.LocationCallback() {
            @Override
            public void onHandleNewLocation(Location location) {
                fusedLocationUtil.stopLocationUpdate();
                fusedLocationUtil.disconnect();

                Map<String, Object> params = new HashMap();
                params.put("user_id", mAppPreference.getUserId());
                params.put("latitude", location.getLatitude());
                params.put("longitude", location.getLongitude());
                params.put("time", System.currentTimeMillis());

                mDatabaseReference.child("wife_location")
                        .child(mAppPreference.getUserId())
                        .push()
                        .setValue(params)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Log.d("PostToFirebase", "Success post to firebase");
                                }else{
                                    Log.d("PostToFirebase", "Failed to post to firebase");
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("PostToFirebase", e.getMessage());
                            }
                        });
            }
        };
        fusedLocationUtil = new FusedLocationUtil(getApplicationContext(), locationCallback);
        fusedLocationUtil.connect();
    }
}
