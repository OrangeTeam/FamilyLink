package org.orange.familylink.data;

import org.orange.familylink.data.Message.Code.Extra.Inform;

/**
 * {@link Inform#URGENT URGENT} {@link Message}的{@link Message#body body}
 * @author Team Orange
 */
public class UrgentMessageBody extends MessageBody {
	private Type type;

	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}

	public static enum Type {
		FALL_DOWN_ALARM,
		SEEK_HELP;
	}
}
