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
 * Created by v01d on 15/07/15
 */
public class OneVoucherFoundFragment extends Fragment {

    private static final String TAG = OneVoucherFoundFragment.class.getSimpleName();

    private Button mIssued;
    private Button mCancel;

    private App mApp;

    private TextView mVoucherText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_vaucher_found, container, false);

        mApp = MainActivity.mApp;

        mVoucherText = (TextView) v.findViewById(R.id.voucher_text);
        if (mApp.getVouchers() != null) {
            mVoucherText.setText(mApp.getVouchers().get(0).voucher);
        }

        mCancel = (Button)v.findViewById(R.id.button_cancel);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.mFragmentManager.popBackStack();
            }
        });

        mIssued = (Button) v.findViewById(R.id.button_issued);
        mIssued.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (NetworkConnection.isConnected(mApp.getMainContext())) {
                    spendVoucher();
                } else {
                    MainActivity.showMessageDialog(R.string.error, getString(R.string.error_check_internet));
                }

            }
        });

        return v;
    }

    private void spendVoucher() {

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

                                MainActivity.mFragmentManager.popBackStack();
                                String mess = getString(R.string.message_succes_voucher_spend);
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
            }

            @Override
            public void failure(RetrofitError error) {
                MainActivity.showMessageDialog(R.string.error, error.getMessage());
                Log.e(TAG, "callback.failure " + error.getMessage());
            }
        };

        TypedInput ti = null;

        try {
            ti = new TypedByteArray("application/json", getRequest().toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        restAdapter.create(ApiHelper.class).postVouchers(ti, callback);
    }

    public JSONObject getRequest() {
        try {
            return new JSONObject().put("voucher", mApp.getVouchers().get(0).voucher);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getHeaderValue(ArrayList<Voucher> vouchers) {

        ArrayList<String> valuesList = new ArrayList<>();
        valuesList.add(vouchers.get(0).voucher); // берем только один ваучер
        return Utilites.md5("voucher|$;" + valuesList.toString().replace("[", "").replace("]", "").replace(" ", ""));
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.mCurrentFragment = MainActivity.FRAGMENT_ONE_VOUCHER_FOUND;
    }
}
