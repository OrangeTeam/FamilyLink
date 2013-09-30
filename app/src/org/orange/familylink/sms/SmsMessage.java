/**
 *
 */
package org.orange.familylink.sms;

import org.orange.familylink.data.Message;
import org.orange.familylink.util.Crypto;

import android.content.Context;
import android.net.Uri;

/**
 * 通过使用Sms实现的{@link Message}
 * @author Team Orange
 * @see SmsSender
 */
public class SmsMessage extends Message {

	@Override
	public void send(Context context, Uri messageUri, String dest,
			String password) {
		// 加密body
		String body = getBody();
		if(body != null)
			setBody(Crypto.encrypt(body, password));
		String json = toJson();
		// 通过SMS发送消息
		json = encode(json);
		SmsSender.sendMessage(context, messageUri, json, dest);
		// 恢复body
		setBody(body);
	}
	@Override
	public void receive(String receivedMessage, String password) {
		receivedMessage = decode(receivedMessage);
		fromJson(receivedMessage);
		String body = getBody();
		if(body != null)
			setBody(Crypto.decrypt(body, password));
	}
	protected static String encode(String origin) {
		origin = origin.replace('{', '(');
		origin = origin.replace('}', ')');
		origin = origin.replace('[', '<');
		origin = origin.replace(']', '>');
		return origin;
	}
	protected static String decode(String encoded) {
		encoded = encoded.replace('(', '{');
		encoded = encoded.replace(')', '}');
		encoded = encoded.replace('<', '[');
		encoded = encoded.replace('>', ']');
		return encoded;
	}

}
