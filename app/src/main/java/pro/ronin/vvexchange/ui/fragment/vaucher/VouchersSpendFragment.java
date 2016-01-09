package pro.ronin.vvexchange.ui.fragment.vaucher;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import pro.ronin.vvexchange.App;
import pro.ronin.vvexchange.R;
import pro.ronin.vvexchange.data.rest.ResponseStatus;
import pro.ronin.vvexchange.data.rest.interfaces.ApiHelper;
import pro.ronin.vvexchange.data.rest.model.Voucher;
import pro.ronin.vvexchange.ui.activity.MainActivity;
import pro.ronin.vvexchange.utils.NetworkConnection;
import pro.ronin.vvexchange.utils.Utilites;
import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedInput;

/**
 * Created by ABrysov on 16/07/15
 */
public class VouchersSpendFragment extends Fragment {

    private static final String TAG = VouchersSpendFragment.class.getSimpleName();

    private App mApp;
    private Button mCancel;
    private Button mSubmit;

    private TextView mVouchersCount;
    private TextView mVouchersText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_vouchers_spend, container, false);

        mApp = MainActivity.mApp;

        mVouchersText = (TextView) v.findViewById(R.id.voucher_text);
        mVouchersText.setText(getVouchersText());

        mVouchersCount = (TextView) v.findViewById(R.id.vouchers_count);
        mVouchersCount.setText(mApp.getVouchersCount().toString());

        mCancel = (Button)v.findViewById(R.id.button_cancel);
        mCancel.setText("< " + getString(R.string.button_cancel));
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.mFragmentManager.popBackStack();
                MainActivity.mFragmentManager.popBackStack();
            }
        });

        mSubmit = (Button) v.findViewById(R.id.button_next);
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkConnection.isConnected(mApp.getMainContext())) {
                    spendVouchers();
                } else {
                    MainActivity.showMessageDialog(R.string.error, getString(R.string.error_check_internet));
                }

            }
        });

        return v;
    }

    public String getVouchersText() {

        ArrayList<String> values = new ArrayList<>();

        String res = "";

        for (int i = 0; i < mApp.getVouchersCount(); i++) {

            values.add(mApp.getVouchers().get(i).voucher);

            if (i == mApp.getVouchersCount() - 1) {
                res += values.get(i) + ".\n";
            } else {
                res += values.get(i) + ",\n";
            }
        }

        mVouchersText.setLines(mApp.getVouchersCount());

        return res;
    }

    private void spendVouchers() {

        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("X-Auth", getHeaderValue(mApp.getVouchers()));
            }
        };

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(mApp.getApiServer())
                .setRequestInterceptor(requestInterceptor)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        Callback<JSONObject> callback = new Callback<JSONObject>() {
            @Override
            public void success(JSONObject list, Response response) {

                for (Header h : response.getHeaders()) {
                    if (h.getName() != null && h.getValue() != null) {
                        if (h.getName().equals("X-App-Status")) {

                            if (h.getValue().equals(ResponseStatus.ok.toString())) {
                                String mess;

                                if (mApp.getVouchersCount() == 1){
                                    mess = mApp.getVouchersCount() + " " + getString(R.string.message_succes_voucher_spend);
                                }else{
                                    mess = mApp.getVouchersCount() + " " + getString(R.string.message_succes_many_voucher_spend);
                                }

                                MainActivity.showMessageDialog(R.string.succes, mess);

                            } else if (h.getValue().equals(ResponseStatus.invalid_voucher.toString())) {
                                MainActivity.showMessageDialog(R.string.error, getString(R.string.error_invalid_voucher));
                            }
                        }
                    }
                }

                if (response.getStatus() == 400) {
                    MainActivity.showMessageDialog(R.string.error, getString(R.string.error_invalid_content));
                } else if (response.getStatus() == 404) {
                    MainActivity.showMessageDialog(R.string.error, getString(R.string.error_invalid_url));
                }

                MainActivity.mFragmentManager.popBackStack();
                MainActivity.mFragmentManager.popBackStack();

            }

            @Override
            public void failure(RetrofitError error) {
                MainActivity.showMessageDialog(R.string.error, error.getMessage());
                Log.e(TAG, "GetCodesTask.getCodes(): " + error.getMessage());
            }
        };

        TypedInput ti = null;

        try {
            ti = new TypedByteArray("application/json", getRequestBody().toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        restAdapter.create(ApiHelper.class).postVouchers(ti, callback);

    }


    public String getHeaderValue(ArrayList<Voucher> vouchers) {

        ArrayList<String> valuesList = new ArrayList<>();

        for (int i = 0; i < mApp.getVouchersCount(); i++) {
            valuesList.add(vouchers.get(i).voucher);
        }

        return Utilites.md5("voucher|$;" + valuesList.toString().replace("[", "").replace("]", "").replace(" ", ""));
    }

    public JSONObject getRequestBody() {

        ArrayList<String> valuesList = new ArrayList<>();

        for (int i = 0; i < mApp.getVouchersCount(); i++) {
            valuesList.add(mApp.getVouchers().get(i).voucher);
        }

        try {
            return new JSONObject().put("voucher", valuesList.toString().replace("[", "").replace("]", "").replace(" ", ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.mCurrentFragment = MainActivity.FRAGMENT_VOUCHER_SPEND;
    }
}
