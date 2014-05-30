package org.orange.familylink.data;

import org.orange.familylink.data.Message.Code.Extra.Inform;

/**
 * {@link Inform#URGENT URGENT} {@link Message}的{@link Message#body body}
 * @author Team Orange
 */
public class UrgentMessageBody extends MessageBody {
	private Type type;
	/** 发送方的当前位置，用[纬度, 经度]二元组表示 */
	private double[] position;

	/**
	 * 取得本消息的{@link Type}
	 * @return 本消息的{@link Type}
	 */
	public Type getType() {
		return type;
	}
	/**
	 * 设置本消息的{@link Type}
	 * @param type
	 */
	public UrgentMessageBody setType(Type type) {
		this.type = type;
		return this;
	}
	/**
	 * 检测是否包含位置信息
	 * @return 如果包含位置信息，返回true；否则返回false
	 */
	public boolean containsPosition() {
		return position != null;
	}
	/**
	 * 取得发送方位置的纬度
	 * @return 如果发送方提供了位置信息，则返回其纬度；否则返回null
	 */
	public Double getPositionLatitude() {
		return position != null ? position[0] : null;
	}
	/**
	 * 取得发送方位置的经度
	 * @return 如果发送方提供了位置信息，则返回其经度；否则返回null
	 */
	public Double getPositionLongitude() {
		return position != null ? position[1] : null;
	}
	/**
	 * 设置发送方当前地理位置
	 * @param latitude 纬度
	 * @param longitude 经度
	 */
	public UrgentMessageBody setPosition(double latitude, double longitude) {
		if(position == null)
			position = new double[2];
		position[0] = latitude;
		position[1] = longitude;
		return this;
	}

	/**
	 * {@link UrgentMessageBody}的类型
	 * @author Team Orange
	 */
	public static enum Type {
		/** 摔倒警报{@link UrgentMessageBody} */
		FALL_DOWN_ALARM,
		/** 求助{@link UrgentMessageBody} */
		SEEK_HELP;
	}
}
