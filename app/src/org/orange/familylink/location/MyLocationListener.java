package org.orange.familylink.location;

import org.orange.familylink.data.Position;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class MyLocationListener implements LocationListener {

	/**
	 * 定位监听类
	 * 
	 * @author Team Orange
	 */

	/**
	 * 更新了gps信息，不需要返回值，只要更新后，会自动记录缓存。
	 * 
	 * @param Location对象
	 */

	@Override
	public void onLocationChanged(Location location) {
		// 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
		if (location != null) {
			Position postion = new Position(location.getLongitude(),
					location.getLatitude(), location.getAltitude(),
					location.getSpeed());
			// log记录
			Log.i("onLocationChanged", postion.toString());
		}
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// Provider被disable时触发此函数，比如GPS被关闭
		Log.i("onProviderDisabled", "gps关闭");
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// Provider被enable时触发此函数，比如GPS被打开
		Log.i("onProviderEnabled", "gps开启");
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// Provider的转态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
	}

}
