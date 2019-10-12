package innovation.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.innovation.pig.insurance.AppConfig;
import com.innovation.pig.insurance.netutils.Constants;
import com.xiangchuang.risks.utils.PigPreferencesUtils;

import org.tensorflow.demo.env.Logger;

/**
 * @author wbs on 11/30/17.
 */

public class LocationManager_new {
    private Logger mLogger = new Logger(LocationManager_new.class);
    @SuppressLint("StaticFieldLeak")
    private static LocationManager_new sInstance;

    private final Context mContext;
    private String mLocationStr;
    private AMapLocationClient mLocationClient = null;
    private GetAddress getAddress;

    public static LocationManager_new getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LocationManager_new.class) {
                if (sInstance == null) {
                    sInstance = new LocationManager_new(context);
                }
            }
        }
        return sInstance;
    }

    private LocationManager_new(Context context) {
        mContext = context.getApplicationContext();
        mLocationClient = new AMapLocationClient(mContext);
        //AMapLocationListener mLocationListener = new LocationListener();
        mLocationClient.setLocationListener(mLocationListener);
    }

    public void startLocation() {
        AMapLocationClientOption locationOption = new AMapLocationClientOption();
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
//        locationOption.setOnceLocation(true);
//        locationOption.setOnceLocationLatest(true);
        //单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
        locationOption.setHttpTimeOut(15000);
        mLocationStr = null;
        mLocationClient.setLocationOption(locationOption);
        mLocationClient.startLocation();
    }

    public String getLocationDetail() {
        return mLocationStr;
    }

   /* private class LocationListener implements AMapLocationListener {

        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation == null) {
                mLogger.e("get location failed.");
                return;
            }
            if (aMapLocation.getErrorCode() == 0) {
                if (Utils.getMD5(DeviceUtil.getImei(mContext)).equalsIgnoreCase("6b9e3b1ee1042312a18464c407b424dc")) {
                    mLocationStr = aMapLocation.getCity() + aMapLocation.getDistrict();
                } else {
//                    String getCity = aMapLocation.getCity();
//                    String getDistrict = aMapLocation.getDistrict();
//                    String getStreet = aMapLocation.getStreet();
//                    String getStreetNum = aMapLocation.getStreetNum();
//                    String getAoiName = aMapLocation.getAoiName();
                    mLocationStr = aMapLocation.getCity()
                            + aMapLocation.getDistrict()
                            + aMapLocation.getStreet()
                            + aMapLocation.getStreetNum()
                            + aMapLocation.getAoiName();
                }
            } else {
                mLogger.e("location error, errorCode: " + aMapLocation.getErrorCode()
                        + " errorInfo: " + aMapLocation.getErrorInfo());
            }
        }
    }*/

    public double currentLat;
    public double currentLon;
    public String str_address = "";
    private final AMapLocationListener mLocationListener = amapLocation -> {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                currentLat = amapLocation.getLatitude();//获取纬度
                currentLon = amapLocation.getLongitude();//获取经度
                str_address = amapLocation.getAddress();
                if(TextUtils.isEmpty(str_address)){
                    str_address = mLocationClient.getLastKnownLocation().getAddress();
                }
                Log.i("===str_address====", "str_address" + str_address);
                PigPreferencesUtils.saveKeyValue(Constants.longitude, currentLon+"", AppConfig.getAppContext());
                PigPreferencesUtils.saveKeyValue(Constants.latitude, currentLat+"", AppConfig.getAppContext());
                PigPreferencesUtils.saveKeyValue(Constants.address, str_address, AppConfig.getAppContext());
                getAddress.getaddress(str_address);
                amapLocation.getAccuracy();//获取精度信息
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
            }
        }
    };

    public void setAddress(GetAddress getAddress) {
        this.getAddress=getAddress;
    }

    public interface GetAddress {
        void getaddress(String address);
    }

    public void getAddressByLatlng(double latitude, double longitude, android.os.Handler.Callback callback) {
        //地理搜索类
        GeocodeSearch geocodeSearch = new GeocodeSearch(mContext);

        geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {

            //得到逆地理编码异步查询结果
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
                String formatAddress = regeocodeAddress.getFormatAddress();
                AppConfig.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(callback != null){
                            Message m = Message.obtain();
                            m.obj = formatAddress;
                            callback.handleMessage(m);
                        }
                    }
                });
            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

            }
        });

        //逆地理编码查询条件：逆地理编码查询的地理坐标点、查询范围、坐标类型。
        LatLonPoint latLonPoint = new LatLonPoint(latitude, longitude);
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 500f, GeocodeSearch.AMAP);
        //异步查询
        geocodeSearch.getFromLocationAsyn(query);
    }
}
