/**
 *
 */
package org.orange.familylink.data;

import org.orange.familylink.data.Message.Code.Extra.Inform;

/**
 * {@link Inform#RESPONSE RESPONSE} {@link Message}的{@link Message#body body}
 * @author Team Orange
 */
public class ResponseMessage extends MessageBody {
	private Long id;
	private String response;

	/**
	 * 取得 要应答的消息(target)在其发送方的ID
	 * @return 要应答的消息(target)在其发送方的ID
	 */
	public long getId() {
		return id;
	}
	/**
	 * 设置 要应答的消息(target)在其发送方的ID
	 * @param id 要应答的消息(target)在其发送方的ID
	 */
	public ResponseMessage setId(long id) {
		this.id = id;
		return this;
	}
	/**
	 * 取得响应信息的json表示，请根据响应的命令消息，确认此json的类型
	 * @return 响应信息的json表示
	 */
	public String getResponse() {
		return response;
	}
	/**
	 * @param response 响应信息的json表示
	 * @return this
	 */
	public ResponseMessage setResponse(String response) {
		this.response = response;
		return this;
	}

}
