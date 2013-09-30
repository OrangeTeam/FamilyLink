package org.orange.familylink.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * 定位操作类
 * @author OrangeTeam
 *
 */
public class LocationTracker implements LocationListener {

    private final Context mContext;

    //用于判断GPS是否可用
    boolean isGPSEnabled = false;

    //用于判断网络是否可用
    boolean isNetworkEnabled = false;
 
    //判断能否开始定位
    boolean canGetLocation = false;

    //位置类
    Location location;
    //纬度
    double latitude;
    //经度
    double longitude;

    //最小改变的距离进行更新定位,因为我应用的定位主要是根据时间间隔进行定位，所以距离间隔可以设的大些
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 200; // 200 meters

    //定位的更新时间差
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 2; // 2 minute

    //定位操作类
    protected LocationManager locationManager;

    public LocationTracker(Context context) {
        this.mContext = context;
        getLocation();
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);

            //获得GPS状态
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            //获得网络状态
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                //网络和GPS都不可用
            	this.canGetLocation = false;
            } else {
                this.canGetLocation = true;
                //用GPS进行定位获取经纬度
                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                //用网络进行定位获取经纬度
                if (isNetworkEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    /**
     * 停止GPSTracker监听
     * */
    public void stopUsingGPS(){
        if(locationManager != null && this.canGetLocation){
            locationManager.removeUpdates(LocationTracker.this);
        }       
    }

    /**
     * 获取纬度
     * */
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }
        return latitude;
    }

    /**
     * 获取经度
     * */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }
        return longitude;
    }

    /**
     * 次函数用于判断是否能开始定位
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }


    @Override
    public void onLocationChanged(Location location) {
    	if(location != null){
    		longitude = location.getLongitude();
    		latitude = location.getLatitude();
    	}
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

}
