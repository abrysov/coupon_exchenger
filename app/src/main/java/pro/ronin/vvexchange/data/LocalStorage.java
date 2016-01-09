package pro.ronin.vvexchange.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ABrysov on 21/07/15
 */
public class LocalStorage {

    private static SharedPreferences mPrefs;

    private static Boolean usePass;
    private static String pass;

    private static Context mContext;
    private boolean lock;

    public LocalStorage(SharedPreferences prefs, Context context) {
        mPrefs = prefs;
        mContext = context;
    }

    public static String getPass() {
        if (pass == null) {
            pass = mPrefs.getString("pass", null);
        }
        return pass;
    }

    public static void savePass(String pass) {
        mPrefs.edit().putString("pass", pass).commit();
        LocalStorage.pass = pass;
    }

    public static void setUsePass(boolean use) {
        usePass = use;
        mPrefs.edit().putBoolean("usePass", use).commit();
    }

    public static boolean getUsePass() {
        if (usePass == null) {
            usePass = mPrefs.getBoolean("usePass", true);
        }
        return usePass;
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean l){
        lock = l;
    }
}
