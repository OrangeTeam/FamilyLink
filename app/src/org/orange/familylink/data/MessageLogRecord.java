package org.orange.familylink.data;

import java.util.Date;

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
	 * 消息状态
	 * @author Team Orange
	 */
	public enum Status {
		/** 未读 */
		UNREAD(Direction.RECEIVE),
		/** 已读 */
		HAVE_READ(Direction.RECEIVE),
		/** 发送中 */
		SENDING(Direction.SEND),
		/** 已发送 */
		SENT(Direction.SEND),
		/** 已送达 */
		DELIVERED(Direction.SEND),
		/** 发送失败 */
		FAILED_TO_SEND(Direction.SEND);

		private final Direction mDirection;
		private Status(Direction direction) {
			this.mDirection = direction;
		}
		/**
		 * @return 消息方向。如{@link Direction#SEND}
		 */
		public Direction getDirection() {
			return mDirection;
		}
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
		public MessageLogRecord setDate(Date date) {
			throw new IllegalStateException("you cannot chang default value.");
		}
		/**
		 * 禁用此方法
		 */
		@Override
		public MessageLogRecord setStatus(Status status) {
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
	/** 消息发送或接收时间 */
	private Date mDate = null;
	/**
	 * 消息状态
	 * <p>
	 * <strong>Tips</strong>: 可以用{@link Status#getDirection()}得到消息方向
	 */
	private Status mStatus = null;
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
	 * @return 消息发送或接收的时间。返回<strong>{@link Date#clone()}</strong>
	 */
	public Date getDate() {
		return mDate != null ? (Date) (mDate.clone()) : null;
	}
	/**
	 * @param date 消息发送或接收的时间。本对象保留<strong>{@link Date#clone()}</strong>
	 * @return this（用于链式调用）
	 */
	public MessageLogRecord setDate(Date date) {
		this.mDate = date != null ? (Date) (date.clone()) : null;
		return this;
	}
	/**
	 * 取得消息状态
	 * <p>
	 * <strong>Tips</strong>: 可以用{@link Status#getDirection()}方法得到消息方向
	 * @return 如果本条消息的状态
	 */
	public Status getStatus() {
		return mStatus;
	}
	/**
	 * @param status 消息状态
	 * @return this（用于链式调用）
	 */
	public MessageLogRecord setStatus(Status status) {
		this.mStatus = status;
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
					&& Objects.compare(mDate, other.mDate)
					&& Objects.compare(mStatus, other.mStatus)
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
	 * 拷贝本对象。本拷贝既不是深拷贝，也不是影子拷贝。
	 * <p>拷贝件中的联系人（对方）与原件的引用相同，拷贝件中的消息时间、消息内容是原件消息的深拷贝。</p>
	 */
	@Override
	public MessageLogRecord clone() {
		MessageLogRecord clone = null;
		try {
			clone = (MessageLogRecord) super.clone();
			clone.mDate = getDate();
			clone.mMessage = getMessage();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("can't clone MessageLogRecord", e);
		}
		return clone;
	}
}
