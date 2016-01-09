package pro.ronin.vvexchange.data.rest.interfaces;

import org.json.JSONObject;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.mime.TypedInput;

/**
 * Created by v01d on 16/07/15
 */
public interface ApiHelper {
    @GET("/vouchers")
    void getVouchers(Callback<JSONObject[]> callback);

    @POST("/vouchers")
    void postVouchers(@Body TypedInput body, Callback<JSONObject> callback);
}
