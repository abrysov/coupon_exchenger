package pro.ronin.vvexchange.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * Created by ABrysov on 08/05/15
 */
public class NetworkConnection {

    /**
     * Get the network info
     *
     * @param context -
     * @return -
     */
    public static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * Check if there is any connectivity
     *
     * @param context -
     * @return -
     */
    public static boolean isConnected(Context context) {
        NetworkInfo info = NetworkConnection.getNetworkInfo(context);
        return (info != null && info.isConnected());
    }

    /**
     * Check if there is any connectivity to a Wifi network
     *
     * @param context -
     * @return -
     */
    public static boolean isConnectedWifi(Context context) {
        NetworkInfo info = NetworkConnection.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    /**
     * Check if there is any connectivity to a mobile network
     *
     * @param context -
     * @return -
     */
    public static boolean isConnectedMobile(Context context) {
        NetworkInfo info = NetworkConnection.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    /**
     * Check if there is fast connectivity
     *
     * @param context -
     * @return -
     */
    public static boolean isConnectedFast(Context context) {
        NetworkInfo info = NetworkConnection.getNetworkInfo(context);
        return (info != null && info.isConnected() && NetworkConnection.isConnectionFast(info.getType(), info.getSubtype()));
    }

    /**
     * Check if the connection is fast
     *
     * @param type    -
     * @param subType -
     * @return -
     */
    public static boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_WIFI) {
            return true;
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
            /*
			 * Above API level 7, make sure to set android:targetSdkVersion
			 * to appropriate level to use these
			 */
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    return true; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    return true; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    return true; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    return false; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    return true; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    public static float getQualityCoefficient(Context c) {
        NetworkInfo info = NetworkConnection.getNetworkInfo(c);

        if (info == null) {
            return 0;
        }

        if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            return 1.0f;
        } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {

            switch (info.getSubtype()) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return 0.25f; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return 0.25f; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return 0.25f; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return 0.25f; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return 0.25f; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return 0.25f; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return 0.5f; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return 0.5f; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return 0.5f; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return 0.5f; // ~ 400-7000 kbps
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    return 1.0f; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    return 1.0f; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    return 1.0f; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    return 1.0f; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    return 1.0f; // ~ 10+ Mbps

                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return 0.25f;
            }
        }

        return 0;
    }
}
