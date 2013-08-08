package org.orange.familylink.location;

import java.util.Iterator;
import java.util.List;

import org.orange.familylink.data.Position;
import org.orange.familylink.database.LocationDB;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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
	// LocationProvider的监听
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
			// 获取到上一次可用的GPS信息,也就是上一次requestLocationUpdates()更新到的信息
			Location location = locManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			Position postion = new Position(location.getLongitude(),
					location.getLatitude(), location.getAltitude(),
					location.getSpeed());
			// 以Position对象返回
			return postion;
		} catch (Exception e) {
			Log.i("getCurrentLocationInfo", "getLocation Exception");
			return null;
		}
	}

	/**
	 * 添加临近警告
	 * 
	 * @param Activity的context上下文
	 *            。
	 * @param addPosition
	 *            ，添加警告的中心位置
	 * @param radius
	 *            ，警告圆范围的半径大小
	 */
	public void addProximity(Context context, Position addPostion, double radius) {
		// 创建启动receiver的intent
		Intent intent = new Intent(context, ProximityAlertReceiver.class);
		// 将Intent包装为PendingIntent
		PendingIntent pi = PendingIntent.getBroadcast(context, -1, intent, 0);
		// 添加临近警告，当进入或离开这个范围，启动receiver
		locManager.addProximityAlert(addPostion.getLatitude(),
				addPostion.getLongitude(), (float) radius, -1, pi);
		// 创建数据库连接
		SQLiteDatabase db = LocationDB.dataBaseOpen(context);
		// 把添加的数据写入数据库
		LocationDB.insertLocation(db, addPostion, radius);
		//关闭数据库
		LocationDB.dataBaseClose(db);
	}

	/**
	 * 把数据库中储存的临界警告添加进locationManager
	 * @param locManager
	 * @param context
	 */
	
	public void readProximity(LocationManager locManager,Context context) {
		// 创建数据库连接
		SQLiteDatabase db = LocationDB.dataBaseOpen(context);
		// 把添加的数据写入数据库
		// 创建启动receiver的intent
		Intent intent = new Intent(context, ProximityAlertReceiver.class);
		// 将Intent包装为PendingIntent
		PendingIntent pi = PendingIntent.getBroadcast(context, -1, intent, 0);
		// 查询所有的临近警告，以list保存
		List<Position> list = LocationDB.queryLoction(db);
		// iterator循环
		Iterator<Position> it = list.iterator();
		while (it.hasNext()) {
			Position position = it.next();
			//把所有保存的警告，用locationManager添加
			locManager.addProximityAlert(position.getLatitude(),
					position.getLongitude(), (float) position.getRadius(), -1,
					pi);
		}
		//关闭数据库
		LocationDB.dataBaseClose(db);
	}
}
