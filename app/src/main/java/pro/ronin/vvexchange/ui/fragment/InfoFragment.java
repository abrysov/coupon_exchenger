package pro.ronin.vvexchange.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import pro.ronin.vvexchange.App;
import pro.ronin.vvexchange.R;
import pro.ronin.vvexchange.ui.activity.MainActivity;

/**
 * Created by v01d on 15/07/15
 */
public class InfoFragment extends Fragment {

    private Button mStart;
    private App mApp;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_info, container, false);

        mApp = (App)getActivity().getApplication();

        mStart = (Button)v.findViewById(R.id.button_start);
        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mApp.setShowInfo(false);

                if (!mApp.getLocalStorage().isLock()){
                    MainActivity.selectCurrentFragment(MainActivity.FRAGMENT_SCANER, R.string.header_scaner);
                }else{
                    MainActivity.selectCurrentFragment(MainActivity.FRAGMENT_LOGIN, R.string.header_login);
                }
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.mCurrentFragment = MainActivity.FRAGMENT_INFO;
    }
}
