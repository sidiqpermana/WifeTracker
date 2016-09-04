package id.co.nbs.android.wifetracker;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseAuth mAuth;
    private String TAG = "WifeTracker";
    private GcmNetworkManager mGcmNetworkManager;
    private AppPreference mAppPreference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mAppPreference = new AppPreference(this);
        mAuthStateListener = new FirebaseAuth.AuthStateListener(){

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null){
                    createPeriodicTask();
                    Log.d(TAG, "User is logged in");
                }else{
                    Log.d(TAG, "User is logged out");
                }
            }
        };

        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInAnonymously", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            Log.w(TAG, "signInAnonymously : Success");
                            mAppPreference.setUserId(task.getResult().getUser().getUid());
                            createPeriodicTask();

                        }
                    }


                });

    }

    private void createPeriodicTask() {
        mGcmNetworkManager = GcmNetworkManager.getInstance(MainActivity.this);
        com.google.android.gms.gcm.Task periodicTask = new PeriodicTask.Builder()
                .setService(TrackingService.class)
                .setPeriod(20)
                .setFlex(10)
                .setTag(TrackingService.TAG_TASK_PRIODDIC_LOG)
                .setPersisted(true)
                .build();

        mGcmNetworkManager.schedule(periodicTask);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthStateListener != null){
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }
}
