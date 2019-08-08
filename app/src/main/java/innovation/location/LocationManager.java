package innovation.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.farm.innovation.utils.FarmerPreferencesUtils;
import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.netutils.Constants;
import com.innovation.pig.insurance.netutils.PreferencesUtils;


import innovation.env.Logger;
import innovation.login.Utils;
import innovation.utils.DeviceUtil;
import okhttp3.internal.Util;

/**
 * @author wbs on 11/30/17.
 */

public class LocationManager {
    private Logger mLogger = new Logger(LocationManager.class);
    @SuppressLint("StaticFieldLeak")
    private static LocationManager sInstance;

    private final Context mContext;
    private String mLocationStr;
    private AMapLocationClient mLocationClient = null;

    public static LocationManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LocationManager.class) {
                if (sInstance == null) {
                    sInstance = new LocationManager(context);
                }
            }
        }
        return sInstance;
    }

    private LocationManager(Context context) {
        mContext = context.getApplicationContext();
        mLocationClient = new AMapLocationClient(mContext);
        AMapLocationListener mLocationListener = new LocationListener();
        mLocationClient.setLocationListener(mLocationListener);
    }

    public void startLocation() {
        AMapLocationClientOption locationOption = new AMapLocationClientOption();
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        locationOption.setOnceLocation(true);
        locationOption.setOnceLocationLatest(true);
        //单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
        locationOption.setHttpTimeOut(15000);
        mLocationStr = null;
        mLocationClient.startLocation();
    }

    public String getLocationDetail() {
        return mLocationStr;
    }

    private class LocationListener implements AMapLocationListener {

        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation == null) {
                mLogger.e("get location failed.");
                return;
            }
            if (aMapLocation.getErrorCode() == 0) {
                String currentLat = String.valueOf(aMapLocation.getLatitude());//获取纬度
                String currentLon = String.valueOf(aMapLocation.getLongitude());//获取经度
                String str_address = aMapLocation.getAddress();
                if(TextUtils.isEmpty(str_address)){
                    str_address = mLocationClient.getLastKnownLocation().getAddress();
                }
                Log.i("===str_address====", "str_address" + str_address);
                PreferencesUtils.saveKeyValue(Constants.longitude, currentLon+"", AppConfig.getAppContext());
                PreferencesUtils.saveKeyValue(Constants.latitude, currentLat+"", AppConfig.getAppContext());
                PreferencesUtils.saveKeyValue(Constants.address, str_address, AppConfig.getAppContext());

                if (Utils.getMD5(DeviceUtil.getImei(mContext)).equalsIgnoreCase("6b9e3b1ee1042312a18464c407b424dc")) {
                    mLocationStr = aMapLocation.getCity() + aMapLocation.getDistrict();
                } else {
                    mLocationStr = aMapLocation.getCity()
                            + aMapLocation.getDistrict()
                            + aMapLocation.getStreet()
                            + aMapLocation.getStreetNum()
                            + aMapLocation.getAoiName();
//                    mLogger.i("我的位置： " + mLocationStr);
                }
            } else {
                mLogger.e("location error, errorCode: " + aMapLocation.getErrorCode()
                        + " errorInfo: " + aMapLocation.getErrorInfo());
            }
        }
    }
}
