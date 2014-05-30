/**
 *
 */
package org.orange.familylink.data;

import org.orange.familylink.data.Message.Code;

/**
 * {@link Code#COMMAND COMMAND} {@link Message}的{@link Message#body body}
 * @author Team Orange
 */
public class CommandMessageBody extends MessageBody {
	private Long id;

	/**
	 * 取得本消息在发送方的ID
	 * @return 本消息在发送方的ID
	 */
	public Long getId() {
		return id;
	}
	/**
	 * 设置本消息在发送方的ID
	 * @param id 本消息在发送方的ID
	 */
	public CommandMessageBody setId(Long id) {
		this.id = id;
		return this;
	}
}
