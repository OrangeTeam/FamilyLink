package org.orange.familylink.location;

import org.orange.familylink.data.Position;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class LocationInfo {

	/**
	 * 定位操作类
	 * 
	 * @author Team Orange
	 */

	private LocationManager locManager;
	// 每获取一次定位信息相隔的时间
	private int repeatTime;
	// locationprovider监听
	private MyLocationListener myLocationListener;

	/**
	 * 构造方法
	 * 
	 * @param 从Activity实例化的LocationMnager对象
	 * @param 循环时间
	 */

	public LocationInfo(LocationManager locManager, int repeatTime) {
		super();
		this.locManager = locManager;
		this.repeatTime = repeatTime;
		// 实例化监听类
		this.myLocationListener = new MyLocationListener();
		// 循环获取定位信息
		this.locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				this.repeatTime, 0, this.myLocationListener);
	}

	/**
	 * 获取当前经纬度
	 * 
	 */
	public Position getCurrentLocationInfo() {
		try {
			// 获取到上一次可用的gps信息,也就是上一次requestLocationUpdates()更新到的信息
			Location location = locManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			Position postion = new Position(location.getLongitude(),
					location.getLatitude(), location.getAltitude(),
					location.getSpeed());
			// 以Postion对象返回
			return postion;
		} catch (Exception e) {
			Log.i("getCurrentLocationInfo", "getLocation Exception");
			return null;
		}
	}

}
