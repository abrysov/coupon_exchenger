package pro.ronin.vvexchange.ui.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import java.util.Calendar;

import pro.ronin.vvexchange.App;
import pro.ronin.vvexchange.R;
import pro.ronin.vvexchange.ui.activity.MainActivity;

/**
 * Created by v01d on 15/07/15
 */
public class LoginFragment extends Fragment {

    private static final String TAG = LoginFragment.class.getSimpleName();
    private static String CODE = "1234";
    private Button mOk;
    private static EditText mPass;

    private App mApp;
    private static Context mContext;
    private long timeOut;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        mContext = getActivity().getApplicationContext();

        mApp = (App) getActivity().getApplication();

        if (mApp.getLocalStorage().getPass() != null) {
            CODE = mApp.getLocalStorage().getPass();
        }

        mOk = (Button)v.findViewById(R.id.button_ok);
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPass();
                hideSoftKeyboard();
            }
        });

        mPass = (EditText)v.findViewById(R.id.pass);
        mPass.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mPass.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (KeyEvent.ACTION_DOWN == event.getAction()) {

                    if (keyCode == KeyEvent.KEYCODE_ENTER && isTimeOut()) {

                        Log.e(TAG, "KeyEvent.KEYCODE_ENTER");

                        checkPass();
                        hideSoftKeyboard();
                    }
                }

                return false;
            }
        });

        return v;
    }

    public void checkPass() {

        if (mPass.getText().toString().equals(CODE)) {
            if (mApp.isShowInfo()){
                MainActivity.selectCurrentFragment(MainActivity.FRAGMENT_INFO, R.string.header_info);
            }else{
                MainActivity.selectCurrentFragment(MainActivity.FRAGMENT_SCANER, R.string.header_scaner);
            }
            mApp.getLocalStorage().setLock(false);
        } else {
            MainActivity.showMessageDialog(R.string.error, getString(R.string.error_invalid_pass));
        }
        mPass.setText("");

    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.mCurrentFragment = MainActivity.FRAGMENT_LOGIN;
    }

    public void hideSoftKeyboard() {
        if (getActivity().getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    public boolean isTimeOut() {

        if (timeOut == 0) {
            timeOut = Calendar.getInstance().getTimeInMillis();
            return true;
        } else {
            return Calendar.getInstance().getTimeInMillis() - timeOut > 2000;
        }
    }
}
