/**
 *
 */
package org.orange.familylink.data;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * 在受顾方与监护方之间传送的消息。
 * <p><em>本类的Setters允许链式调用（Method chaining）</em></p>
 * @author Team Orange
 */
public class Message {
	/**
	 * 消息代码。指明消息主体的语义，明确命令。
	 * @author Team Orange
	 */
	public abstract static class Code {
		/**
		 * 未定义。这是默认值，表示尚未指明代码。
		 */
		public static final int UNDEFINED	 =-0x001;
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
			}
		}
		private static final int MINIMUM	 = UNDEFINED;
		private static final int MAXIMUM		 = 0x1ff;

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
		public Message setCode(int code) {
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
	private int code = Code.UNDEFINED;
	/**
	 * 消息主体
	 */
	private String body = null;

	/**
	 * 使用默认值构造本类。
	 */
	public Message() {
		super();
	}
	/**
	 * 使用指定值构造本类。
	 * @param code 消息代码，参见{@link Code}
	 * @param body 消息主体
	 * @see Message#mDefaultValue
	 */
	public Message(int code, String body) {
		this();
		setCode(code).setBody(body);
	}

	/**
	 * 取得消息代码
	 * @return 消息代码
	 * @see Code
	 */
	public int getCode() {
		return code;
	}
	/**
	 * 设置消息代码
	 * @param code 消息代码
	 * @return this（为了链式调用）
	 * @see Code
	 */
	public Message setCode(int code) {
		if(!Code.isLegalCode(code))
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
	 * 判断指定对象是否与本对象内容相同。
	 * <p><em>会调用{@link #isSameClass(Object)}判断是否是本类的实例，
	 * 调用{@link #compare(Object, Object)}比较{@link String}等对象</em></p>
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
			return code == other.code && compare(body, other.body);
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
	 * 判断两对象是否相等，根据o1的<code>equals</code>方法判断。
	 * <p><em>当o1为null时：若o2也为null，返回true；若o2不是null，返回false</em></p>
	 * @param o1 待比较对象1
	 * @param o2 待比较对象2
	 * @return 若o1 == o2，返回true；若o1 != o2，返回false
	 */
	protected static boolean compare(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}
}
