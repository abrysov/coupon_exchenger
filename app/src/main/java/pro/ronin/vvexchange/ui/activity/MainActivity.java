package pro.ronin.vvexchange.ui.activity;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import pro.ronin.vvexchange.App;
import pro.ronin.vvexchange.R;
import pro.ronin.vvexchange.ui.fragment.InfoFragment;
import pro.ronin.vvexchange.ui.fragment.LoginFragment;
import pro.ronin.vvexchange.ui.fragment.ScannerFragment;
import pro.ronin.vvexchange.ui.fragment.vaucher.ManyVouchesFoundFragment;
import pro.ronin.vvexchange.ui.fragment.vaucher.OneVoucherFoundFragment;
import pro.ronin.vvexchange.ui.fragment.vaucher.VouchersSpendFragment;
import pro.ronin.vvexchange.utils.NetworkConnection;

public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    public final static String TAG = MainActivity.class.getSimpleName();

    public final static int FRAGMENT_LOGIN = 10;
    public final static int FRAGMENT_INFO = 11;
    public final static int FRAGMENT_SCANER = 12;
    public final static int FRAGMENT_ONE_VOUCHER_FOUND = 13;
    public final static int FRAGMENT_MANY_VOUCHER_FOUND = 14;
    public final static int FRAGMENT_VOUCHER_SPEND = 15;

    private static final String ARG_SECTION_NUMBER = "section_number";

    public static FragmentManager mFragmentManager;
    public static ActionBar mActionBar;
    public static App mApp;

    public static int mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);

        mApp = (App)getApplication();
        mApp.setMainContext(this);

        mFragmentManager = getFragmentManager();
        mFragmentManager.addOnBackStackChangedListener(this);

        mActionBar = getSupportActionBar();
        if (mActionBar!=null){
            mActionBar.setTitle(R.string.app_name);
        }

        lock();
        createConectionChecker();
    }

    private void createConectionChecker(){

        final Handler h = new Handler(getMainLooper());
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                connectionCheck();
            }
        };

        new Thread(r){
            @Override
            public void run() {
                try {
                    while (true){
                        sleep(15000);
                        h.post(r);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void connectionCheck() {
        if (!NetworkConnection.isConnected(getApplicationContext())){
            Toast.makeText(getApplicationContext(), getString(R.string.error_check_internet_connection), Toast.LENGTH_SHORT).show();
        }
    }

    public static void selectCurrentFragment(int identificator, int name) {

        mFragmentManager.beginTransaction()
                .replace(R.id.container, getFragmentContainer(identificator), String.valueOf(identificator))
                .setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out)
                .addToBackStack(String.valueOf(identificator)) //name
                .commit();

        mActionBar.setTitle(name);
    }

    private static Fragment getFragmentContainer(int containerNumber) {
        Fragment fragment;

        switch (containerNumber){
            case FRAGMENT_INFO:

                if (mFragmentManager.findFragmentByTag(String.valueOf(FRAGMENT_INFO)) != null) {
                    fragment = mFragmentManager.findFragmentByTag(String.valueOf(FRAGMENT_INFO));
                } else {
                    fragment = new InfoFragment();
                }
                break;

            case FRAGMENT_LOGIN:

                if (mFragmentManager.findFragmentByTag(String.valueOf(FRAGMENT_LOGIN)) != null) {
                    fragment = mFragmentManager.findFragmentByTag(String.valueOf(FRAGMENT_LOGIN));
                } else {
                    fragment = new LoginFragment();
                }
                break;

            case FRAGMENT_SCANER:

                if (mFragmentManager.findFragmentByTag(String.valueOf(FRAGMENT_SCANER)) != null) {
                    fragment = mFragmentManager.findFragmentByTag(String.valueOf(FRAGMENT_SCANER));
                } else {
                    fragment = new ScannerFragment();
                }
                break;

            case FRAGMENT_ONE_VOUCHER_FOUND:

                if (mFragmentManager.findFragmentByTag(String.valueOf(FRAGMENT_ONE_VOUCHER_FOUND)) != null) {
                    fragment = mFragmentManager.findFragmentByTag(String.valueOf(FRAGMENT_ONE_VOUCHER_FOUND));
                } else {
                    fragment = new OneVoucherFoundFragment();
                }
                break;

            case FRAGMENT_VOUCHER_SPEND:

                if (mFragmentManager.findFragmentByTag(String.valueOf(FRAGMENT_VOUCHER_SPEND)) != null) {
                    fragment = mFragmentManager.findFragmentByTag(String.valueOf(FRAGMENT_VOUCHER_SPEND));
                } else {
                    fragment = new VouchersSpendFragment();
                }
                break;

            case FRAGMENT_MANY_VOUCHER_FOUND:

                if (mFragmentManager.findFragmentByTag(String.valueOf(FRAGMENT_MANY_VOUCHER_FOUND)) != null) {
                    fragment = mFragmentManager.findFragmentByTag(String.valueOf(FRAGMENT_MANY_VOUCHER_FOUND));
                } else {
                    fragment = new ManyVouchesFoundFragment();
                }
                break;

            default:
                Log.e(TAG, "Invalid fragamnet id" + Integer.toString(containerNumber));
                fragment = new Fragment();
                break;
        }

        if (fragment.getArguments() == null) {
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, containerNumber);
            fragment.setArguments(args);
        }

        return fragment;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {

            if (mCurrentFragment != FRAGMENT_LOGIN) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }

            return true;
        } else if (id == R.id.action_blocker) {
            lock();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        lock();
        super.onPause();
    }

    public static void lock() {
        mFragmentManager.popBackStack();
        mApp.getLocalStorage().setLock(true);
        selectLockFragment();
    }

    private static void selectLockFragment() {
        if (mApp.getLocalStorage().getUsePass()) {
            selectCurrentFragment(FRAGMENT_LOGIN, R.string.header_login);
        } else {
            selectCurrentFragment(FRAGMENT_INFO, R.string.header_info);
        }
    }

    public static void showMessageDialog(final int title, final String message) {

        final Dialog dialog = new Dialog(mApp.getMainContext());

        dialog.setContentView(R.layout.dialog);
        dialog.setTitle(title);

        TextView messageText = (TextView) dialog.findViewById(R.id.message);
        messageText.setText(message);

        Button b = (Button) dialog.findViewById(R.id.button_issued_dialog);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        if (mFragmentManager.getBackStackEntryCount() > 1) {

            if (mCurrentFragment == FRAGMENT_INFO){
                super.onBackPressed();
            }else{
                mFragmentManager.popBackStack();
            }

        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onBackStackChanged() {

        if (mCurrentFragment == FRAGMENT_INFO) {
            mActionBar.setTitle(R.string.header_info);
        } else if (mCurrentFragment == FRAGMENT_MANY_VOUCHER_FOUND) {
            mActionBar.setTitle(R.string.header_many_voucher_found);
        } else if (mCurrentFragment == FRAGMENT_ONE_VOUCHER_FOUND) {
            mActionBar.setTitle(R.string.header_one_voucher_found);
        } else if (mCurrentFragment == FRAGMENT_VOUCHER_SPEND) {
            mActionBar.setTitle(R.string.header_voucher_spend);
        } else if (mCurrentFragment == FRAGMENT_SCANER) {
            mActionBar.setTitle(R.string.header_scaner);
        } else if (mCurrentFragment == FRAGMENT_LOGIN) {
            mActionBar.setTitle(R.string.header_login);
        } else {
            mActionBar.setTitle(R.string.app_name);
        }
    }

    private class ConnectionCheckerTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            createConnectionChecker();
            return null;
        }

        private void createConnectionChecker(){
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    connectionCheck();
                }
            }, 10000);

        }
    }
}
