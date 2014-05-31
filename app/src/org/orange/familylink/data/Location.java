package org.orange.familylink.data;

import java.io.Serializable;

/**
 * 位置信息
 * 
 * @author Team Orange
 */
public class Location implements Serializable {
	private static final long serialVersionUID = -3734746950853065137L;
	/** 经度 */
	private Double longitude;
	/** 纬度 */
	private Double latitude;

	/**
	 * @return 经度
	 */
	public Double getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude 经度
	 * @return this
	 */
	public Location setLongitude(Double longitude) {
		this.longitude = longitude;
		return this;
	}

	/**
	 * @return 纬度
	 */
	public Double getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude 纬度
	 * @return this
	 */
	public Location setLatitude(Double latitude) {
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
