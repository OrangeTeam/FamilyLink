package org.orange.familylink.data;

/**
 * 位置信息
 * 
 * @author Team Orange
 */
public class Position {

	/** 经度 */
	private Double longitude;
	/** 纬度 */
	private Double latitude;

	/**
	 * @return 经度
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude 经度
	 * @return this
	 */
	public Position setLongitude(Double longitude) {
		this.longitude = longitude;
		return this;
	}

	/**
	 * @return 纬度
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude 纬度
	 * @return this
	 */
	public Position setLatitude(Double latitude) {
		this.latitude = latitude;
		return this;
	}

	@Override
	/** 用于转换String类型 */
	public String toString() {
		return "Position :\n longitude=" + longitude + " \n latitude="
				+ latitude + "\n altitude = ";
	}
}
