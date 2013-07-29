package org.orange.familylink.data;

import org.orange.familylink.util.Objects;

/**
 * 消息日志中的记录
 * @author Team Orange
 */
public class MessageLogRecord implements Cloneable {
	/**
	 * 消息方向。如{@link #SEND}、{@link #RECEIVE}
	 * @author Team Orange
	 */
	public enum Direction {
		/** 发送 */
		SEND,
		/** 接收 */
		RECEIVE
	}
	/**
	 * 本类的默认值，你可以通过它取得本类各字段的默认值。
	 * <p><em>禁用此对象的Setters</em></p>
	 */
	public static final MessageLogRecord mDefaultValue = new MessageLogRecord(){
		/**
		 * 禁用此方法
		 */
		@Override
		public MessageLogRecord setId(Long id) {
			throw new IllegalStateException("you cannot chang default value.");
		}
		/**
		 * 禁用此方法
		 */
		@Override
		public MessageLogRecord setTimestamp(long timestamp) {
			throw new IllegalStateException("you cannot chang default value.");
		}
		/**
		 * 禁用此方法
		 */
		@Override
		public MessageLogRecord setDirection(Direction direction) {
			throw new IllegalStateException("you cannot chang default value.");
		}
		/**
		 * 禁用此方法
		 */
		@Override
		public MessageLogRecord setMessage(Message message) {
			throw new IllegalStateException("you cannot chang default value.");
		}

		/* (non-Javadoc)
		 * @see org.orange.familylink.data.Message#isSameClass(java.lang.Object)
		 */
		@Override
		protected boolean isSameClass(Object o) {
			return o.getClass() == getClass().getSuperclass() || o.getClass() == getClass();
		}
	};

	/** 记录ID */
	private Long mId = null;
	/** 消息时间戳 */
	private long mTimestamp = 0;
	/** 消息方向 */
	private Direction mDirection = null;
	/** 消息内容 */
	private Message mMessage = null;

	/**
	 * 用默认值构造本类的实例
	 */
	public MessageLogRecord() {
		super();
	}
	/**
	 * @param id 记录ID。允许设置为null
	 * @param timestamp 消息时间戳。{@link System#currentTimeMillis()}格式
	 * @param direction 消息方向。如{@link Direction#SEND}
	 * @param message 消息内容
	 */
	public MessageLogRecord(Long id, long timestamp, Direction direction, Message message) {
		this();
		setId(id).setTimestamp(timestamp).setDirection(direction).setMessage(message);
	}

	/**
	 * @return 记录ID。可能为null
	 */
	public Long getId() {
		return mId;
	}
	/**
	 * @param id 记录ID。可以设置为null，来取消之前的设置
	 * @return this（用于链式调用）
	 */
	public MessageLogRecord setId(Long id) {
		mId = id;
		return this;
	}
	/**
	 * @return 消息时间戳。{@link System#currentTimeMillis()}格式
	 */
	public long getTimestamp() {
		return mTimestamp;
	}
	/**
	 * @param timestamp 消息时间戳。{@link System#currentTimeMillis()}格式
	 * @return this（用于链式调用）
	 */
	public MessageLogRecord setTimestamp(long timestamp) {
		this.mTimestamp = timestamp;
		return this;
	}
	/**
	 * @return 消息方向。如{@link Direction#SEND}
	 */
	public Direction getDirection() {
		return mDirection;
	}
	/**
	 * @param direction 消息方向。如{@link Direction#SEND}
	 * @return this（用于链式调用）
	 */
	public MessageLogRecord setDirection(Direction direction) {
		this.mDirection = direction;
		return this;
	}
	/**
	 * 取得{@link Message}（可能为null）。返回本对象消息内容的{@link Message#clone() clone}。
	 * @return 消息内容
	 */
	public Message getMessage() {
		return mMessage != null ? mMessage.clone() : null;
	}
	/**
	 * 为了设置消息内容，取得{@link Message}（可能为null）。返回本对象消息内容的引用。
	 * @return 消息内容
	 */
	public Message getMessageToSet() {
		return mMessage;
	}
	/**
	 * 设置{@link Message}（设置为null，来取消之前的设置）。本对象会保留参数的{@link Message#clone() clone}。
	 * @param message 消息内容
	 * @return this（用于链式调用）
	 */
	public MessageLogRecord setMessage(Message message) {
		this.mMessage = message != null ? message.clone() : null;
		return this;
	}

	/**
	 * 判断指定对象是否与本对象内容相同。
	 * <p><em>会调用{@link #isSameClass(Object)}判断是否是本类的实例，
	 * 调用{@link Objects#compare(Object, Object)}比较{@link Message}等对象</em></p>
	 * @param o 待比较对象
	 * @return 如果内容与本对象相同，返回true；不同，返回false
	 */
	@Override
	public boolean equals(Object o) {
		if(o == null)
			return false;
		else if(!isSameClass(o))
			return false;
		else {
			MessageLogRecord other = (MessageLogRecord) o;
			return mId == other.mId && mTimestamp == other.mTimestamp
					&& mDirection == other.mDirection
					&& Objects.compare(mMessage, other.mMessage);
		}
	}
	/**
	 * 判断指定对象是否是本类（或{@link #mDefaultValue}）的实例
	 * @param o 待测试对象
	 * @return 如果是本类（或{@link #mDefaultValue}）的实例，返回true；不是，返回false
	 */
	protected boolean isSameClass(Object o) {
		return getClass() == o.getClass() || mDefaultValue.getClass() == o.getClass();
	}
	/**
	 * 深拷贝
	 */
	@Override
	public MessageLogRecord clone() {
		MessageLogRecord clone = null;
		try {
			clone = (MessageLogRecord) super.clone();
			clone.mMessage = getMessage();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("can't clone MessageLogRecord", e);
		}
		return clone;
	}
}
