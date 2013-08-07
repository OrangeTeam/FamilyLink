/**
 *
 */
package org.orange.familylink.data;

import org.orange.familylink.database.Contract.Messages;
import org.orange.familylink.sms.SmsSender;
import org.orange.familylink.util.Objects;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * 在受顾方与监护方之间传送的消息。
 * <p><em>本类的Setters允许链式调用（Method chaining）</em></p>
 * @author Team Orange
 */
public class Message implements Cloneable{
	/**
	 * 消息代码。指明消息主体的语义，明确命令。
	 * @author Team Orange
	 */
	public abstract static class Code {
		/**
		 * 通告。如定时通告、紧急通知、应答命令等
		 * @see Extra.Inform
		 */
		public static final int INFORM		 = 0x000;
		/**
		 * 命令。如现在定位等。
		 * @see Extra.Command
		 */
		public static final int COMMAND		 = 0x100;
		/**
		 * {@link Code}的附加信息。如具体命令，通告原因等。
		 * <p><strong>使用方法</strong>：用“|”与主命令连接。如
		 * <code>Code.INFORM | Code.Extra.Inform.PULSE</code></p>
		 * <p><em>可以用“|”连接多个Extra</em></p>
		 * @author Team Orange
		 */
		public abstract static class Extra {
			/**
			 * {@link Code#INFORM}的额外信息，指明通告原因等
			 * @author Team Orange
			 * @see Extra
			 */
			public abstract static class Inform {
				/**
				 * 应答命令
				 */
				public static final int RESPOND	 = 0x01;
				/**
				 * 脉冲通告，定时报告
				 */
				public static final int PULSE	 = 0x02;
				/**
				 * 紧急消息
				 */
				public static final int URGENT	 = 0x04;

				/**
				 * 检测指定code是不是设置了{@link #RESPOND}位
				 * @param code 待检测code
				 * @return 如果code是{@link Code#INFORM}并且设置了{@link #RESPOND}位，返回true；否则返回false
				 */
				public static boolean hasSetRespond(int code) {
					if(!isInform(code))
						return false;
					return (code & RESPOND) == RESPOND;
				}
				/**
				 * 检测指定code是不是设置了{@link #PULSE}位
				 * @param code 待检测code
				 * @return 如果code是{@link Code#INFORM}并且设置了{@link #PULSE}位，返回true；否则返回false
				 */
				public static boolean hasSetPulse(int code) {
					if(!isInform(code))
						return false;
					return (code & PULSE) == PULSE;
				}
				/**
				 * 检测指定code是不是设置了{@link #URGENT}位
				 * @param code 待检测code
				 * @return 如果code是{@link Code#INFORM}并且设置了{@link #URGENT}位，返回true；否则返回false
				 */
				public static boolean hasSetUrgent(int code) {
					if(!isInform(code))
						return false;
					return (code & URGENT) == URGENT;
				}
			}
			/**
			 * {@link Code#COMMAND}的额外信息，指明具体命令
			 * @author Team Orange
			 * @see Extra
			 */
			public abstract static class Command {
				/**
				 * 现在定位
				 */
				public static final int LOCATE_NOW = 0x01;

				/**
				 * 检测指定code是不是设置了{@link #LOCATE_NOW}位
				 * @param code 待检测code
				 * @return 如果code是{@link Code#COMMAND}并且设置了{@link #LOCATE_NOW}位，返回true；否则返回false
				 */
				public static boolean hasSetLocateNow(int code) {
					if(!isCommand(code))
						return false;
					return (code & LOCATE_NOW) == LOCATE_NOW;
				}
			}
		}
		/** {@link Extra}所在位置 */
		public static final int EXTRA_BITS	 = 0xff;
		/** 可能的最小{@link Code} */
		public static final int MINIMUM		 = INFORM;
		/** 可能的最大{@link Code} */
		public static final int MAXIMUM		 = COMMAND | EXTRA_BITS;

		/**
		 * 检查code的合法性。
		 * @param code 待检测的代码
		 * @return 如果合法，返回true；如果非法，返回false
		 */
		public static boolean isLegalCode (int code) {
			if(code >= MINIMUM && code <= MAXIMUM)
				return true;
			else
				return false;
		}
		/**
		 * 检测指定code是不是{@link #INFORM} code
		 * @param code 待检测code
		 * @return 如果是{@link #INFORM}，返回true；如果不是，返回false
		 */
		public static boolean isInform(int code) {
			if(!isLegalCode(code))
				return false;
			return (code & (~EXTRA_BITS)) == INFORM;
		}
		/**
		 * 检测指定code是不是{@link #COMMAND} code
		 * @param code 待检测code
		 * @return 如果是{@link #COMMAND}，返回true；如果不是，返回false
		 */
		public static boolean isCommand(int code) {
			if(!isLegalCode(code))
				return false;
			return (code & (~EXTRA_BITS)) == COMMAND;
		}
	}
	/**
	 * 本类的默认值，你可以通过它取得本类各字段的默认值。
	 * <p><em>禁用此对象的Setters</em></p>
	 */
	public static final Message mDefaultValue = new Message(){
		/**
		 * 禁用此方法
		 */
		@Override
		public Message setCode(Integer code) {
			throw new IllegalStateException("you cannot chang default value.");
		}
		/**
		 * 禁用此方法
		 */
		@Override
		public Message setBody(String body) {
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

	/**
	 * 消息代码
	 * @see Code
	 */
	private Integer code = null;
	/**
	 * 消息主体
	 */
	private String body = null;

	/**
	 * 使用默认值构造本类。
	 * <p>Tips：可以类似这样使用链式调用
	 * <pre><code>new Message().setBody(MessageTest.TEST_CASE_BODY)
	 *     .setCode(Message.Code.INFORM | Message.Code.Extra.Inform.PULSE);</code></pre></p>
	 */
	public Message() {
		super();
	}

	/**
	 * 取得消息代码
	 * @return 消息代码
	 * @see Code
	 */
	public Integer getCode() {
		return code;
	}
	/**
	 * 设置消息代码
	 * @param code 消息代码
	 * @return this（为了链式调用）
	 * @see Code
	 */
	public Message setCode(Integer code) {
		if((code != null) && (!Code.isLegalCode(code)))
			throw new IllegalArgumentException("Illegal Code :" + code);
		this.code = code;
		return this;
	}
	/**
	 * 取得消息主体
	 * @return 消息主体
	 */
	public String getBody() {
		return body;
	}
	/**
	 * 设置消息主体
	 * @param body 消息主体
	 * @return this（为了链式调用）
	 */
	public Message setBody(String body) {
		this.body = body;
		return this;
	}

	/**
	 * 把本对象序列化为Json表示法的字符串。
	 * @return 与本对象对应的Json
	 * @see Gson#toJson(Object)
	 */
	public String toJson() {
		return new Gson().toJson(this);
	}
	/**
	 * 把Json反序列化为本类的一个实例。
	 * @param json Json表示法的本类的对象
	 * @return 与json对应的，本类的一个实例对象
	 * @throws JsonSyntaxException 当给定的Json不表示本类时
	 */
	public static Message fromJson(String json) {
		return new Gson().fromJson(json, Message.class);
	}

	/**
	 * 发送本消息
	 * <p>
	 * <strong>注意：</strong><em>不</em> 应在UI线程调用本方法
	 * @param context 上下文环境
	 * @param contactId 联系人{@link Messages#COLUMN_NAME_CONTACT_ID ID}
	 * @param dest 发送目的{@link Messages#COLUMN_NAME_ADDRESS 地址}
	 * @param password 要发送信息的加密密码
	 */
	public void send(Context context, Long contactId , String dest, String password) {
		Uri newUri = null;
		ContentValues newMessage = new ContentValues();
		newMessage.put(Messages.COLUMN_NAME_CONTACT_ID, contactId);
		newMessage.put(Messages.COLUMN_NAME_ADDRESS, dest);
		newMessage.put(Messages.COLUMN_NAME_TIME, System.currentTimeMillis());
		newMessage.put(Messages.COLUMN_NAME_STATUS, MessageLogRecord.Status.SENDING.name());
		newMessage.put(Messages.COLUMN_NAME_BODY, getBody());
		newMessage.put(Messages.COLUMN_NAME_CODE, getCode());
		newUri = context.getContentResolver().insert(Messages.MESSAGES_URI, newMessage);
		//TODO 加密Body
		SmsSender.sendMessage(context, newUri, toJson(), dest);
	}

	/**
	 * 深拷贝
	 */
	@Override
	public Message clone() {
		Message clone = null;
		try {
			clone = (Message) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("can't clone Message", e);
		}
		return clone;
	}
	/**
	 * 判断指定对象是否与本对象内容相同。
	 * <p><em>会调用{@link #isSameClass(Object)}判断是否是本类的实例，
	 * 调用{@link Objects#compare(Object, Object)}比较{@link String}等对象</em></p>
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
			Message other = (Message) o;
			return Objects.compare(code, other.code) && Objects.compare(body, other.body);
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
}
