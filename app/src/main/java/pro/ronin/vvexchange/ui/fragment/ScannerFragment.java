package pro.ronin.vvexchange.ui.fragment;

import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import pro.ronin.vvexchange.App;
import pro.ronin.vvexchange.R;
import pro.ronin.vvexchange.data.rest.ResponseStatus;
import pro.ronin.vvexchange.utils.NetworkConnection;
import pro.ronin.vvexchange.utils.Utilites;
import pro.ronin.vvexchange.data.rest.interfaces.ApiHelper;
import pro.ronin.vvexchange.data.rest.model.Voucher;
import pro.ronin.vvexchange.ui.activity.MainActivity;
import pro.ronin.vvexchange.ui.custom.CameraPreview;
import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;
import retrofit.mime.TypedInput;

/**
 * Created by v01d on 15/07/15
 */
public class ScannerFragment extends Fragment {

    private static final String TAG = ScannerFragment.class.getSimpleName();

    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private Handler mAutoFocusHandler;
    private FrameLayout preview;

    private ImageScanner mScanner;
    private boolean previewing = true;
    private String mLastScannedCode;
    private Image mCodeImage;
    private App mApp;

    private Button mBlockButton;

    private long oldScanTime;

    static {
        System.loadLibrary("iconv");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_scaner, container, false);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mApp = (App) getActivity().getApplication();

        mAutoFocusHandler = new Handler();

        preview = (FrameLayout) v.findViewById(R.id.cameraPreview);

        mBlockButton = (Button) v.findViewById(R.id.button_block);
        mBlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.lock();
            }
        });

        /* Instance barcode scanner */
        mScanner = new ImageScanner();
        mScanner.setConfig(0, Config.X_DENSITY, 3);
        mScanner.setConfig(0, Config.Y_DENSITY, 3);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeCamera();
        MainActivity.mCurrentFragment = MainActivity.FRAGMENT_SCANER;
    }

    public void onPause() {
        super.onPause();
        releaseCamera();
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            //
        }
        return c;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            previewing = false;
            mCamera.cancelAutoFocus();
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void resumeCamera() {

        Log.d(TAG, "start scanning...");

        mCamera = getCameraInstance();

        if (mCamera != null) {
            mCameraPreview = new CameraPreview(getActivity().getApplicationContext(), mCamera, previewCb, autoFocusCB); //mApp.getMainContext()
            preview.removeAllViews();
            preview.addView(mCameraPreview);

            Camera.Parameters parameters = mCamera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

            mCodeImage = new Image(size.width, size.height, "Y800");
            previewing = true;
            mCameraPreview.refreshDrawableState();
        }else {
            Toast.makeText(getActivity().getApplicationContext(), "Camera cant inicialize!", Toast.LENGTH_SHORT).show();
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing && mCamera != null) {
                mCamera.autoFocus(autoFocusCB);
            }
        }
    };

    Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            mCodeImage.setData(data);
            int result = mScanner.scanImage(mCodeImage);
            if (result != 0) {
                SymbolSet syms = mScanner.getResults();
                for (Symbol sym : syms) {
                    mLastScannedCode = sym.getData();
                    if (mLastScannedCode != null && (System.currentTimeMillis() - oldScanTime) > 10000) {
                        Log.d("CameraTestActivity", "Last scan result: " + mLastScannedCode);
                        oldScanTime = System.currentTimeMillis();

                        mApp.setScanedCode(mLastScannedCode);

                        if (NetworkConnection.isConnected(mApp.getMainContext())) {
                            getCodes(mLastScannedCode);
                        } else {
                            MainActivity.showMessageDialog(R.string.error, getString(R.string.error_check_internet));
                        }

                    }
                }
            }
            camera.addCallbackBuffer(data);
        }
    };

    final Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            mAutoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };

    private void getCodes(final String code) {

        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("X-Auth", getHeaderValue(code));
                request.addQueryParam("code", code);
            }
        };

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(mApp.getApiServer())
                .setRequestInterceptor(requestInterceptor)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        Callback<JSONObject[]> callback = new Callback<JSONObject[]>() {
            @Override
            public void success(JSONObject[] listJsonArrays, Response response) {

                for (Header h : response.getHeaders()) {
                    if (h.getName() != null && h.getValue() != null) {
                        if (h.getName().equals("X-App-Status")) {

                            if (h.getValue().equals(ResponseStatus.invalid_code.toString())) {
                                MainActivity.showMessageDialog(R.string.error, getString(R.string.error_invalid_code));
                            }
                        }
                    }
                }

                if (response.getStatus() == 200) {

                    ArrayList<Voucher> vouchers = new ArrayList<>();
                    inflateVouchers(response, vouchers);
                    operateResponseCodes(response, vouchers);

                } else if (response.getStatus() == 400) {
                    MainActivity.showMessageDialog(R.string.error, getString(R.string.error_invalid_content));
                } else if (response.getStatus() == 404) {
                    MainActivity.showMessageDialog(R.string.error, getString(R.string.error_invalid_url));
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "callback.failure: " + error.getMessage());
                MainActivity.showMessageDialog(R.string.error, getString(R.string.error_invalid_response));
            }
        };

        restAdapter.create(ApiHelper.class).getVouchers(callback);
    }

    private void operateResponseCodes(Response response, ArrayList<Voucher> vouchers) {

        if (response.getStatus() == 200) {

            if (vouchers.size() > 1) {
                MainActivity.selectCurrentFragment(MainActivity.FRAGMENT_MANY_VOUCHER_FOUND, R.string.header_many_voucher_found);
            } else {
                MainActivity.selectCurrentFragment(MainActivity.FRAGMENT_ONE_VOUCHER_FOUND, R.string.header_one_voucher_found);
            }

        } else if (response.getStatus() == 400) {
            MainActivity.showMessageDialog(R.string.error, getString(R.string.error_invalid_content));
        } else if (response.getStatus() == 404) {
            MainActivity.showMessageDialog(R.string.error, getString(R.string.error_invalid_url));
        } else {
            for (Header h : response.getHeaders()) {
                if (h.getName() != null && h.getValue() != null) {
                    if (h.getName().equals("X-App-Status") && h.getValue().equals("invalid_code")) {
                        MainActivity.showMessageDialog(R.string.error, getString(R.string.not_valid_code));
                    } else if (h.getName().equals("X-App-Status") && h.getValue().equals("")) {

                    }
                }
            }
        }
    }

    private void inflateVouchers(Response response, ArrayList<Voucher> vouchers) {

        byte[] bytes;
        JSONArray ja;
        TypedInput ti = response.getBody();

        try {
            InputStream is = ti.in();

            bytes = IOUtils.toByteArray(is);
            String str = new String(bytes, "UTF-8");

            ja = new JSONArray(str);

            Log.i(TAG, "ja = " + ja.toString());

            for (int i = 0; i < ja.length(); i++) {

                Voucher v = new Voucher();

                JSONObject jo = ja.getJSONObject(i);
                v.voucher = jo.getString("voucher");

                vouchers.add(v);
            }

            mApp.setVouchers(vouchers);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getHeaderValue(String code) {
        String res = Utilites.md5("code|$;" + code);
        return res;
    }

}
