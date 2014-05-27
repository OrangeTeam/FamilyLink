/**
 *
 */
package org.orange.familylink.data;

import org.orange.familylink.data.MessageLogRecord.Status;
import org.orange.familylink.database.Contract;
import org.orange.familylink.database.Contract.Messages;
import org.orange.familylink.util.Objects;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * 在受顾方与监护方之间传送的消息。
 * <p><em>本类的Setters允许链式调用（Method chaining）</em></p>
 * @author Team Orange
 */
public abstract class Message implements Cloneable{
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
				public static final int RESPONSE = 0x01;
				/**
				 * 脉冲通告，定时报告
				 */
				public static final int PULSE	 = 0x02;
				/**
				 * 紧急消息
				 */
				public static final int URGENT	 = 0x04;

				/**
				 * 检测指定code是不是设置了{@link #RESPONSE}位
				 * @param code 待检测code
				 * @return 如果code是{@link Code#INFORM}并且设置了{@link #RESPONSE}位，返回true；否则返回false
				 */
				public static boolean hasSetRespond(int code) {
					if(!isInform(code))
						return false;
					return (code & RESPONSE) == RESPONSE;
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
				 * 回显。主要用于测试
				 */
				public static final int ECHO		 = 0x01;
				/**
				 * 现在定位
				 */
				public static final int LOCATE_NOW	 = 0x02;

				/**
				 * 检测指定code是不是设置了{@link #ECHO}位
				 * @param code 待检测code
				 * @return 如果code是{@link Code#COMMAND}并且设置了{@link #ECHO}位，返回true；否则返回false
				 */
				public static boolean hasSetEcho(int code) {
					if(!isCommand(code))
						return false;
					return (code & ECHO) == ECHO;
				}
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
	 * 消息代码
	 * @see Code
	 */
	private Integer code = null;
	/**
	 * 消息主体
	 */
	private String body = null;

	/**
	 * 使用默认值构造本类
	 * <p>
	 * Tips：可以类似这样使用链式调用
	 * <pre><code>new Message().setBody("Hello")
	 *     .setCode(Message.Code.INFORM | Message.Code.Extra.Inform.PULSE);</code></pre>
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
	 * 把Json反序列化。结果保存到本对象中。
	 * @param json Json表示法的本类的对象
	 * @return this（为了链式调用）
	 * @throws JsonSyntaxException 当给定的Json不表示本类时
	 */
	public Message fromJson(String json) {
		Message m = new Gson().fromJson(json, getClass());
		setCode(m.getCode()).setBody(m.getBody());
		return this;
	}

	/**
	 * 发送本消息
	 * <p>
	 * <strong>注意：</strong><em>不</em> 应在UI线程调用本方法
	 * @param context 应用全局信息
	 * @param contactId 联系人{@link Messages#COLUMN_NAME_CONTACT_ID ID}
	 * @param dest 发送目的{@link Messages#COLUMN_NAME_ADDRESS 地址}
	 * @return 保存后的本消息的{@link Uri}
	 * @throws IllegalArgumentException 当dest == null || dest.isEmpty()时
	 * @see #receiveAndSave(Context, String)
	 */
	public Uri sendAndSave(Context context, Long contactId , String dest) {
		return sendAndSave(context, contactId, dest, Settings.getPassword(context));
	}
	/**
	 * 发送本消息
	 * <p>
	 * <strong>注意：</strong><em>不</em> 应在UI线程调用本方法
	 * @param context 应用全局信息
	 * @param contactId 联系人{@link Messages#COLUMN_NAME_CONTACT_ID ID}
	 * @param dest 发送目的{@link Messages#COLUMN_NAME_ADDRESS 地址}
	 * @param password 要发送信息的加密密码
	 * @return 保存后的本消息的{@link Uri}
	 * @throws IllegalArgumentException 当dest == null || dest.isEmpty()时
	 * @see #receiveAndSave(String, String)
	 */
	public Uri sendAndSave(Context context, Long contactId , String dest, String password) {
		if(dest == null || dest.isEmpty())
			throw new IllegalArgumentException("dest address shouldn't be empty");
		Uri newUri = saveMessage(context, contactId, dest, Status.SENDING);
		if (Code.isCommand(getCode())) beforeSendCommandMessage(context, newUri);
		send(context, newUri, dest, password);
		return newUri;
	}
	/**
	 * 发送Command Message之前，更新body的ID
	 * @param messageUri 此{@link Message}的{@link Uri}
	 */
	private void beforeSendCommandMessage(Context context, Uri messageUri) {
		if (getBody() !=  null) throw new IllegalStateException("have setBody");
		final long id = ContentUris.parseId(messageUri);
		CommandMessageBody body = new CommandMessageBody();
		body.setId(id);
		setBody(body.toJson());
		// 更新Content Provider中的记录发送日志
		ContentValues message = new ContentValues();
		message.put(Messages.COLUMN_NAME_TIME, System.currentTimeMillis());
		message.put(Messages.COLUMN_NAME_BODY, getBody());
		final int rowsUpdated = context.getContentResolver().update(
				ContentUris.withAppendedId(Messages.MESSAGES_URI, id),
				message, null, null);
		assert rowsUpdated == 1;
	}

	/**
	 * 发送本消息
	 * @param context 应用包环境信息
	 * @param messageUri 本消息的存储{@link Uri}
	 * @param dest 发送目的地址
	 * @param password 发送时的加密密钥
	 */
	public abstract void send(Context context, Uri messageUri, String dest, String password);

	/**
	 * 接收并保存消息。接收到的消息存到本{@link Message}对象
	 * <p>
	 * <strong>注意：</strong><em>不</em> 应在UI线程调用本方法
	 * @param context 应用全局信息
	 * @param receivedMessage 接收到的消息原始内容
	 * @param srcAddr 消息来源地址
	 * @return 保存后的本消息的{@link Uri}
	 * @throws JsonSyntaxException 当给定的receivedMessage与本类不对应时
	 * @see #sendAndSave(Context, Long, String)
	 */
	public Uri receiveAndSave(Context context, String receivedMessage, String srcAddr) {
		// remove spaces and dashes from destination number
		// (e.g. "801 555 1212" -> "8015551212")
		// (e.g. "+8211-123-4567" -> "+82111234567")
		srcAddr = PhoneNumberUtils.stripSeparators(srcAddr);
		return receiveAndSave(context, receivedMessage, queryContactId(context, srcAddr),
				srcAddr, Settings.getPassword(context));
	}
	/**
	 * 接收并保存消息。接收到的消息存到本{@link Message}对象
	 * <p>
	 * <strong>注意：</strong><em>不</em> 应在UI线程调用本方法
	 * @param context 应用全局信息
	 * @param receivedMessage 接收到的消息原始内容
	 * @param contactId 消息来源联系人ID
	 * @param srcAddr 消息来源地址
	 * @param password 解密密钥
	 * @return 保存后的本消息的{@link Uri}
	 * @throws JsonSyntaxException 当给定的receivedMessage与本类不对应时
	 * @see #sendAndSave(Context, Long, String, String)
	 * @see #receive(String, String)
	 */
	public Uri receiveAndSave(Context context, String receivedMessage,
			Long contactId, String srcAddr, String password) {
		receive(receivedMessage, password);
		return saveMessage(context, contactId, srcAddr, Status.UNREAD);
	}
	protected Long queryContactId(Context context, String contactAddress) {
		Long contactId = null;
		Uri baseUri = Contract.Contacts.CONTACTS_URI;
		String[] projection = {Contract.Contacts._ID};
		String selection = Contract.Contacts.COLUMN_NAME_PHONE_NUMBER + " = ?";
		contactAddress = removePrefix(contactAddress);
		String[] args = {contactAddress};
		Log.w("cont", "" + contactAddress);
		Cursor c = context.getContentResolver()
				.query(baseUri, projection, selection, args, null);
		if(c.moveToFirst()) {
			int column = c.getColumnIndex(Contract.Contacts._ID);
			if(!c.isNull(column))
				contactId = c.getLong(column);
		}
		return contactId;
	}
	/**
	 * 移除电话号码的前缀，如“+86”
	 * @param contactAddress 电话号码
	 * @return 去除前缀之后的电话号码
	 */
	private String removePrefix(String contactAddress){
		String newContactAddress = null;
		//手机号的长度为11位
		int phoneDigits = 11;
		//手机号的前缀“+”的位置为0
		final int PLUS_SIGN_POSITION = 0;
		if(contactAddress.charAt(PLUS_SIGN_POSITION) == '+'){
			int prefixDigits = contactAddress.length() - phoneDigits;
			newContactAddress = contactAddress.substring(prefixDigits);
		}else{
			newContactAddress = contactAddress;
		}
		return newContactAddress;
	}

	/**
	 * 接收消息。接收到的消息存到本{@link Message}对象
	 * @param receivedMessage 接收到的消息原始内容
	 * @param password 解密密钥
	 * @throws JsonSyntaxException 当给定的receivedMessage与本类不对应时
	 */
	public abstract void receive(String receivedMessage, String password);

	/**
	 * 保存消息并返回其{@link Uri}
	 * @param context 应用上下文环境
	 * @param contactId 联系人ID
	 * @param address 地址
	 * @param status 状态
	 * @return 保存的消息的{@link Uri}
	 */
	protected Uri saveMessage(Context context, Long contactId , String address, Status status) {
		Log.w("smsfamily", "sms5");
		// 在Content Provider中记录发送日志
		ContentValues newMessage = new ContentValues();
		newMessage.put(Messages.COLUMN_NAME_CONTACT_ID, contactId);
		newMessage.put(Messages.COLUMN_NAME_ADDRESS, address);
		newMessage.put(Messages.COLUMN_NAME_TIME, System.currentTimeMillis());
		newMessage.put(Messages.COLUMN_NAME_STATUS, status.name());
		newMessage.put(Messages.COLUMN_NAME_BODY, getBody());
		newMessage.put(Messages.COLUMN_NAME_CODE, getCode());
		return context.getContentResolver().insert(Messages.MESSAGES_URI, newMessage);
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
		return getClass() == o.getClass();
	}
}
