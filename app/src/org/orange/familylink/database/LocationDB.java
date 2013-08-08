package org.orange.familylink.database;

import java.util.ArrayList;
import java.util.List;

import org.orange.familylink.data.Position;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class LocationDB {

	/**
	 * 定位信息的数据库操作类
	 * 
	 * @author orange team
	 */

	// 数据库名字
	public static final String DATABASE_NAME = "familylink.db";
	// 以下两个为联系人和信息表的表名
	public static final String DATABASE_LOCATION_TABLE = "location";
	// 数据库的版本
	public static final int DATABASE_VERSION = 1;

	// 字段
	public static final String COLUMN_NAME_LONGITUDE = "longitude";
	public static final String COLUMN_NAME_LATITUDE = "latitude";
	public static final String COLUMN_NAME_ALTITUDE = "altitude";
	public static final String COLUMN_NAME_SPEED = "speed";
	public static final String COLUMN_NAME_RADIUS = "radius";

	// 创建数据库语句

	/**
	 * table location _id integer primarykey autoincrement longitude varchar(20)
	 * latitude varchar(20) altitude varchar(20) speed varchar(20) radius
	 * varchar(20)
	 * 
	 */
	public static final String LOCATION_TABLE_CREATE = "create table "
			+ DATABASE_LOCATION_TABLE
			+ "(_id integer primary key autoincrement," + COLUMN_NAME_LONGITUDE
			+ " varchar(20), " + COLUMN_NAME_LATITUDE + " varchar(20) , "
			+ COLUMN_NAME_ALTITUDE + " varchar(20) , " + COLUMN_NAME_SPEED
			+ " varchar(20)," + COLUMN_NAME_RADIUS + " varchar(20))";

	/**
	 * 插入临近警告数据
	 * 
	 * @param db
	 *            SQLiteDatabase对象
	 * @param position
	 *            ,临近警告的中心位置
	 * @param radius
	 *            ，临近警告的半径
	 */

	public static void insertLocation(SQLiteDatabase db, Position position,
			double radius) {
		//临近警告数据
		String longitude = Double.toString(position.getLongitude());
		String latitude = Double.toString(position.getLatitude());
		String altitude = Double.toString(position.getAltitude());
		String speed = Double.toString(position.getSpeed());
		String radiuss = Double.toString(radius);
		String[] content = new String[] { longitude, latitude, altitude, speed,
				radiuss };
		try {
			//插入临近警告数据
			db.execSQL("insert into " + DATABASE_LOCATION_TABLE
					+ " values(null,?,?,?,?,?) ", content);
		} catch (SQLiteException se) {
			//创建location表
			db.execSQL(LOCATION_TABLE_CREATE);
			//插入临近警告数据
			db.execSQL("insert into " + DATABASE_LOCATION_TABLE
					+ " values(null,?,?,?,?,?) ", content);
		}
	}

	/**
	 * 查询所有临近警告数据
	 * @param db
	 * @return list 把所有结果全部以list<Position>返回
	 */
	
	public static List<Position> queryLoction(SQLiteDatabase db) {
		//查询location表的所有数据
		Cursor cursor = db.rawQuery("select * from " + DATABASE_LOCATION_TABLE,
				null);
		List<Position> list = new ArrayList<Position>();
		//将cursor放入List<Postion>中
		while (cursor.moveToNext()) {
			list.add(new Position(Double.valueOf(cursor.getString(1)), Double
					.valueOf(cursor.getString(2)), Double.valueOf(cursor
					.getString(3)), Double.valueOf(cursor.getString(4)), Double
					.valueOf(cursor.getString(5))));
		}
		//关闭cursor
		cursor.close();
		return list;
	}
	
	/**
	 * 关闭数据库
	 * @param db
	 * @return 如果能关闭返回true，否则返回false
	 */
	
	public static boolean dataBaseClose(SQLiteDatabase db) {
		try {
			//关闭数据库
			db.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 连接数据库
	 * @param context上下文
	 * @return SQLiteDatabase对象
	 */
	public static SQLiteDatabase dataBaseOpen(Context context) {
		return SQLiteDatabase.openOrCreateDatabase(context.getFilesDir()
				.toString() + "/" + DATABASE_NAME, null);
	}

}
