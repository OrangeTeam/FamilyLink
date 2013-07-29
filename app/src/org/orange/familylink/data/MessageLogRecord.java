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
		public MessageLogRecord setContact(Contact contact) {
			throw new IllegalStateException("you cannot chang default value.");
		}
		/**
		 * 禁用此方法
		 */
		@Override
		public MessageLogRecord setAddress(String address) {
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
		public MessageLogRecord setHasRead(Boolean hasRead) {
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

	/** 记录ID。null表示未知（未设置） */
	private Long mId = null;
	/** 联系人（对方）。接收消息的发送者，发送消息的收信者 */
	private Contact mContact = null;
	/** 电话号、Email等地址。允许为null */
	private String mAddress = null;
	/** 消息时间戳 */
	private long mTimestamp = 0;
	/** 消息方向 */
	private Direction mDirection = null;
	/** 已读。true表示已读，false表示未读，null表示未知（未设置） */
	private Boolean mHasRead = null;
	/** 消息内容 */
	private Message mMessage = null;

	/**
	 * 用默认值构造本类的实例
	 * <p>Tips: 可以这样链式调用：<br />
	 * <pre><code>new MessageLogRecord().setId(432L).setAddress("10010")</code></pre></p>
	 */
	public MessageLogRecord() {
		super();
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
	 * @return 联系人（对方）。接收的消息的发送者，发送的消息的收信者。返回的是<strong>引用</strong>
	 */
	public Contact getContact() {
		return mContact;
	}
	/**
	 * @param contact 联系人（对方）。接收的消息的发送者，发送的消息的收信者。本类保存此<strong>引用</strong>
	 * @return this（用于链式调用）
	 */
	public MessageLogRecord setContact(Contact contact) {
		this.mContact = contact;
		return this;
	}
	/**
	 * @return 电话号、Email等地址。可能为null
	 */
	public String getAddress() {
		return mAddress;
	}
	/**
	 * @param address 电话号、Email等地址。允许为null
	 * @return this（用于链式调用）
	 */
	public MessageLogRecord setAddress(String address) {
		this.mAddress = address;
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
	 * @return 如果本条消息已读，返回true；未读，返回false；null表示未设置或未知
	 */
	public Boolean hasRead() {
		return mHasRead;
	}
	/**
	 * @param hasRead 如果本条消息已读，应设为true；未读用false表示；null表示未设置
	 * @return this（用于链式调用）
	 */
	public MessageLogRecord setHasRead(Boolean hasRead) {
		this.mHasRead = hasRead;
		return this;
	}
	/**
	 * 取得{@link Message}（可能为null）。返回本对象消息内容的<strong>{@link Message#clone() clone}</strong>。
	 * @return 消息内容
	 */
	public Message getMessage() {
		return mMessage != null ? mMessage.clone() : null;
	}
	/**
	 * 为了设置消息内容，取得{@link Message}（可能为null）。返回本对象消息内容的<strong>引用</strong>。
	 * @return 消息内容
	 */
	public Message getMessageToSet() {
		return mMessage;
	}
	/**
	 * 设置{@link Message}（设置为null，来取消之前的设置）。本对象会保留参数的<strong>{@link Message#clone() clone}</strong>。
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
	 * 调用{@link Objects#compare(Object, Object)}比较各对象字段</em></p>
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
			return Objects.compare(mId, other.mId)
					&& Objects.compare(mContact, other.mContact)
					&& Objects.compare(mAddress, other.mAddress)
					&& mTimestamp == other.mTimestamp
					&& Objects.compare(mDirection, other.mDirection)
					&& Objects.compare(mHasRead, other.mHasRead)
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
	 * 拷贝本对象。本拷贝既不是深拷贝，也不是影子拷贝。拷贝件中的联系人（对方）与原件的引用相同，拷贝件中的消息内容是原件消息的深拷贝。
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
