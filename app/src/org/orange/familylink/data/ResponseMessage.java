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
	public void setId(long id) {
		this.id = id;
	}
}
