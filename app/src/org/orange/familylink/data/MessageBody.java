/**
 *
 */
package org.orange.familylink.data;

import com.google.gson.Gson;

/**
 * {@link Message}的{@link Message#body body}
 * @author Team Orange
 */
public class MessageBody {
	private String content;

	/**
	 * 取得附加内容
	 * @return 附加内容
	 */
	public String getContent() {
		return content;
	}
	/**
	 * 设置附加内容
	 * @param content 附加内容
	 */
	public MessageBody setContent(String content) {
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
}
