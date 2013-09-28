package org.orange.familylink.data;

import org.orange.familylink.data.Message.Code.Extra.Inform;

import com.google.gson.Gson;

/**
 * {@link Inform#URGENT URGENT} {@link Message}的{@link Message#body body}
 * <p><em>本类的Setters允许链式调用（Method chaining）</em></p>
 * @author Team Orange
 */
public class UrgentMessageBody {
	private Type type;
	private String content;

	public Type getType() {
		return type;
	}
	public UrgentMessageBody setType(Type type) {
		this.type = type;
		return this;
	}
	public String getContent() {
		return content;
	}
	public UrgentMessageBody setContent(String content) {
		this.content = content;
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

	public static enum Type {
		FALL_DOWN_ALARM,
		SEEK_HELP;
	}
}
