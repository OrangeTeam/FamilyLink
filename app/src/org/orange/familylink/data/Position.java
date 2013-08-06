package org.orange.familylink.data;


public class Position {
	/**
	 * 位置信息类
	 * @author Team Orange
	 */
	
	/** 经度 */
	private double longitude ;
	/** 纬度 */
	private double latitude ;
	/** 高度 */
	private double altitude ;
	/** 速度 */
	private double speed ;
	
	
	public double getAltitude() {
		return altitude;
	}
	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}
	public double getSpeed() {
		return speed;
	}
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public Position(double longitude, double latitude, double altitude,
			double speed) {
		super();
		this.longitude = longitude;
		this.latitude = latitude;
		this.altitude = altitude;
		this.speed = speed;
	}
	@Override
	/** 用于转换String类型 */
	public String toString() {
		return "Position :\n longitude=" + longitude + " \n latitude=" + latitude
				+ "\n altitude = " + altitude + "\n speed = " + speed;
	}
}
