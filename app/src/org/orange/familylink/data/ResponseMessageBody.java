/**
 *
 */
package org.orange.familylink.data;

import org.orange.familylink.data.Message.Code.Extra.Inform;

/**
 * {@link Inform#RESPONSE RESPONSE} {@link Message}的{@link Message#body body}
 * @author Team Orange
 */
public class ResponseMessageBody extends MessageBody {
	private Long id;

	/**
	 * 取得 要应答的消息(target)在其发送方的ID
	 * @return 要应答的消息(target)在其发送方的ID
	 */
	public Long getId() {
		return id;
	}
	/**
	 * 设置 要应答的消息(target)在其发送方的ID
	 * @param id 要应答的消息(target)在其发送方的ID
	 */
	public ResponseMessageBody setId(Long id) {
		this.id = id;
		return this;
	}
	/**
	 * 取得响应信息的json表示。请根据本消息响应的命令消息，确认此json的类型
	 * @return 响应信息的json表示
	 */
	@Override
	public String getContent() {
		return super.getContent();
	}
	/**
	 * @param response 响应信息的json表示
	 * @return this
	 */
	@Override
	public ResponseMessageBody setContent(String response) {
		super.setContent(response);
		return this;
	}

}
