/**
 *
 */
package org.orange.familylink;

import org.orange.familylink.data.MessageLogRecord.Direction;
import org.orange.familylink.data.MessageLogRecord.Status;
import org.orange.familylink.fragment.MessagesFragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

/**
 * 消息列表{@link Activity}
 * @author Team Orange
 * @see MessagesFragment
 */
public class MessagesActivity extends Activity {
	/**
	 * 应当显示的消息的ID列表
	 * <p>
	 * Type: long[]
	 */
	public static final String EXTRA_IDS = MainActivity.class.getName() + ".extra.IDS";
	/**
	 * 意图设置的消息状态筛选条件，用{@link Status}表示
	 * <p>
	 * Type: {@link Status}
	 */
	public static final String EXTRA_STATUS = MainActivity.class.getName() + ".extra.STATUS";
	/**
	 * 意图设置的消息方向筛选条件，用{@link Direction}表示
	 * <p>
	 * Type: {@link Direction}
	 */
	public static final String EXTRA_DIRECTION = MainActivity.class.getName() + ".extra.DIRECTION";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_messages);

		Fragment messages = new MessagesFragment();
		Bundle extras = getIntent().getExtras();
		if(extras != null) {
			Bundle args = new Bundle();
			if(extras.containsKey(EXTRA_STATUS))
				args.putSerializable(MessagesFragment.ARGUMENT_KEY_STATUS,
						extras.getSerializable(EXTRA_STATUS));
			if(extras.containsKey(EXTRA_DIRECTION))
				args.putSerializable(MessagesFragment.ARGUMENT_KEY_DIRECTION,
						extras.getSerializable(EXTRA_DIRECTION));
			if(extras.containsKey(EXTRA_IDS))
				args.putLongArray(MessagesFragment.ARGUMENT_KEY_IDS,
						extras.getLongArray(EXTRA_IDS));
			messages.setArguments(args);
		}
		if(savedInstanceState == null)
			getFragmentManager().beginTransaction().add(R.id.messages, messages).commit();
		else
			getFragmentManager().beginTransaction().replace(R.id.messages, messages).commit();
	}

}
