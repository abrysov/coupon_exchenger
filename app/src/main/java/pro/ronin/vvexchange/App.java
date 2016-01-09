package pro.ronin.vvexchange;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;
import pro.ronin.vvexchange.data.Config;
import pro.ronin.vvexchange.data.LocalStorage;
import pro.ronin.vvexchange.data.rest.model.Voucher;

/**
 * Created by ABrysov on 14/07/15
 */
public class App extends Application {

    private String mScanedCode;
    private Context mMainContext;

    private Integer mVouchersCount;

    private ArrayList<Voucher> mVouchers;

    private SharedPreferences sharedPreferences;    // = getSharedPreferences(PREFS_NAME, 0);
    private LocalStorage mLocalStorage;             // = new LocalStorage(sharedPreferences, getApplicationContext());

    public static final String PREFS_NAME = "prefs";

    private boolean showInfo = true;

    private String apiServer;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        mLocalStorage = new LocalStorage(sharedPreferences, mMainContext);

        if (BuildConfig.DEBUG) {
            apiServer = Config.API_SERVER_STAGING;
        } else {
            apiServer = Config.API_SERVER;
        }
    }


    public String getScanedCode() {
        return mScanedCode;
    }

    public void setScanedCode(String code) {
        this.mScanedCode = code;
    }

    public Context getMainContext() {
        return mMainContext;
    }

    public void setMainContext(Context c) {
        this.mMainContext = c;
    }

    public Integer getVouchersCount() {
        return mVouchersCount;
    }

    public void setVouchersCount(Integer count) {
        this.mVouchersCount = count;
    }

    public ArrayList<Voucher> getVouchers() {
        return mVouchers;
    }

    public void setVouchers(ArrayList<Voucher> vouchers) {
        this.mVouchers = vouchers;
    }

    public LocalStorage getLocalStorage() {
        return mLocalStorage;
    }

    public boolean isShowInfo() {
        return showInfo;
    }

    public void setShowInfo(boolean showInfo) {
        this.showInfo = showInfo;
    }

    public String getApiServer() {
        return apiServer;
    }

}
