package id.co.nbs.android.wifetracker;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Sidiq on 04/09/2016.
 */
public class AppPreference {
    private String PREFS_NAME = "WifiTracking.Prefs";
    private String KEY_USER_ID = "userId";
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public AppPreference(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public void setUserId(String userId){
        editor.putString(KEY_USER_ID, userId);
        editor.commit();
    }

    public String getUserId(){
        return preferences.getString(KEY_USER_ID, "");
    }
}
