package pro.ronin.vvexchange.ui.fragment.vaucher;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import pro.ronin.vvexchange.App;
import pro.ronin.vvexchange.R;
import pro.ronin.vvexchange.ui.activity.MainActivity;

/**
 * Created by v01d on 15/07/15
 */
public class ManyVouchesFoundFragment extends Fragment {

    private Button mCancel;
    private Button mNext;
    private App mApp;

    private TextView mVouchrsCount;
    private NumberPicker mNumberPicker;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_vouchers_select, container, false);

        mApp = MainActivity.mApp;

        mNumberPicker = (NumberPicker) v.findViewById(R.id.numberPicker);
        mNumberPicker.setMaxValue(mApp.getVouchers().size());
        mNumberPicker.setMinValue(1);
        mNumberPicker.setValue(mApp.getVouchers().size());

        mVouchrsCount = (TextView) v.findViewById(R.id.vouchers_count);
        mVouchrsCount.setText(String.valueOf(mApp.getVouchers().size()));

        mCancel = (Button)v.findViewById(R.id.button_cancel);
        mCancel.setText("< " + getString(R.string.button_cancel));
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.mFragmentManager.popBackStack();
            }
        });

        mNext = (Button)v.findViewById(R.id.button_next);
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.selectCurrentFragment(MainActivity.FRAGMENT_VOUCHER_SPEND, R.string.header_voucher_spend);
                mApp.setVouchersCount(mNumberPicker.getValue());
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.mCurrentFragment = MainActivity.FRAGMENT_MANY_VOUCHER_FOUND;
    }
}
