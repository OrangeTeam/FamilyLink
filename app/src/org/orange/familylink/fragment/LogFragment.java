/**
 *
 */
package org.orange.familylink.fragment;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.orange.familylink.R;
import org.orange.familylink.data.Contact;
import org.orange.familylink.data.Message;
import org.orange.familylink.data.Message.Code;
import org.orange.familylink.data.Message.Code.Extra;
import org.orange.familylink.data.MessageLogRecord;
import org.orange.familylink.data.MessageLogRecord.Direction;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 日志{@link ListFragment}
 * @author Team Orange
 */
public class LogFragment extends ListFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setListAdapter(new MockLogAdapter(getActivity()));
		return view;
	}

	private int getStringResIdOfCode(Integer code) {
		if(code == null) {
			return R.string.undefined;
		} else if(Code.isInform(code)){
			if(Code.Extra.Inform.hasSetUrgent(code))
				return R.string.urgent;
			else if(Code.Extra.Inform.hasSetRespond(code))
				return R.string.respond;
			else if(Code.Extra.Inform.hasSetPulse(code))
				return R.string.pulse;
			else
				return R.string.inform;
		} else if(Code.isCommand(code)){
			if(Code.Extra.Command.hasSetLocateNow(code))
				return R.string.locate_now;
			else
				return R.string.command;
		} else if(!Code.isLegalCode(code)) {
				return R.string.illegal_code;
		} else {
			throw new IllegalArgumentException("May be method Code.isLegalCode() not correct");
		}
	}
	private String valueOfCode(Integer code) {
		int resId = getStringResIdOfCode(code);
		if(resId != R.string.illegal_code)
			return getString(resId);
		else
			return getString(R.string.illegal_code, code.intValue());
	}
	private String valueOfDirection(Direction direction) {
		if(direction == null)
			return getString(R.string.unknown);
		else if(direction == Direction.SEND)
			return getString(R.string.send);
		else if(direction == Direction.RECEIVE)
			return getString(R.string.receive);
		else
			throw new UnsupportedOperationException("unsupport "+direction+" now.");
	}
	private String valueOfHasRead(Boolean hasRead) {
		if(hasRead == null)
			return getString(R.string.unknown);
		else if(hasRead)
			return getString(R.string.has_read);
		else
			return getString(R.string.unread);
	}

	private class MockLogAdapter extends BaseAdapter {
		private final ArrayList<MessageLogRecord> mMockLog = new ArrayList<MessageLogRecord>();
		{
			int[] codes = new int[]{Code.INFORM,
					Code.INFORM | Extra.Inform.PULSE, Code.INFORM | Extra.Inform.RESPOND,
					Code.INFORM | Extra.Inform.URGENT,
					Code.INFORM | Extra.Inform.PULSE | Extra.Inform.RESPOND,
					Code.INFORM | Extra.Inform.PULSE | Extra.Inform.URGENT,
					Code.INFORM | Extra.Inform.RESPOND | Extra.Inform.URGENT,
					Code.INFORM | Extra.Inform.PULSE | Extra.Inform.RESPOND | Extra.Inform.URGENT,
					Code.COMMAND,
					Code.COMMAND | Extra.Command.LOCATE_NOW};
			Boolean[] booleans = new Boolean[]{null, Boolean.FALSE, Boolean.TRUE};
			Direction[] directions = new Direction[Direction.values().length+1];
			for(int i = 0 ; i < Direction.values().length ; i++)
				directions[i] = Direction.values()[i];
			directions[directions.length-1] = null;
			for(int i = 1 ; i <= 100 ; i++) {
				mMockLog.add(new MessageLogRecord().setId((long)i).setContact(new Contact())
						.setAddress("Address "+ i)
						.setDate(new Date(System.currentTimeMillis() + i * 1000000))
						.setDirection(directions[i%directions.length])
						.setHasRead(booleans[i%booleans.length])
						.setMessage(new Message().setCode(codes[i % codes.length])
						.setBody("Body " + i)));
			}
		}

		private Context mContext;
		private DateFormat mDateFormat;
		private DateFormat mTimeFormat;
		private LayoutInflater mInflater;

		public MockLogAdapter(Context context) {
			// Cache the LayoutInflate to avoid asking for a new one each time.
			mContext = context;
			mInflater = LayoutInflater.from(context);
			mDateFormat = android.text.format.DateFormat.getDateFormat(context);
			mTimeFormat = android.text.format.DateFormat.getTimeFormat(context);
		}

		@Override
		public int getCount() {
			return mMockLog.size();
		}

		@Override
		public MessageLogRecord getItem(int position) {
			return mMockLog.get(position);
		}

		@Override
		public long getItemId(int position) {
			return mMockLog.get(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// A ViewHolder keeps references to children views to avoid unneccessary calls
			// to findViewById() on each row.
			ViewHolder holder;
			// When convertView is not null, we can reuse it directly, there is no need
			// to reinflate it. We only inflate a new View when the convertView supplied
			// by ListView is null.
			if(convertView == null) {
				convertView = mInflater.inflate(R.layout.fragment_log_item, parent, false);
				// Creates a ViewHolder and store references to the two children views
				// we want to bind data to.
				holder = new ViewHolder();
				holder.parent = convertView;
				holder.code = (TextView) convertView.findViewById(R.id.code);
				holder.body = (TextView) convertView.findViewById(R.id.body);
				holder.contact_name = (TextView) convertView.findViewById(R.id.contact_name);
				holder.address = (TextView) convertView.findViewById(R.id.address);
				holder.date = (TextView) convertView.findViewById(R.id.date);
				holder.directon_icon = (ImageView) convertView.findViewById(R.id.direction_icon);
				holder.unread_icon = (ImageView) convertView.findViewById(R.id.unread_icon);

				convertView.setTag(holder);
			} else {
				// Get the ViewHolder back to get fast access to the TextView
				holder = (ViewHolder) convertView.getTag();
			}
			// Bind the data efficiently with the holder.
			bindView(position, holder);
			return convertView;
		}
		/**
		 * 把每项记录的数据绑定到其视图上
		 * @param position 记录位置
		 * @param holder 视图的{@link ViewHolder}
		 */
		private void bindView(int position, ViewHolder holder) {
			MessageLogRecord record = mMockLog.get(position);
			// message code
			holder.code.setText(valueOfCode(record.getMessageToSet().getCode()));
			setViewColor(position, holder);
			// message body
			if(record.getMessageToSet().getBody() != null)
				holder.body.setText(record.getMessageToSet().getBody());
			else
				holder.body.setText("");
			//TODO 联系人
//			holder.contact_name.setText(record.getContact().toString());
			holder.contact_name.setText("People Name " + position);
			// address
			if(record.getAddress() != null)
				holder.address.setText(record.getAddress());
			else
				holder.address.setText(R.string.unknown);
			// date
			if(record.getDate() != null) {
				holder.date.setVisibility(View.VISIBLE);
				holder.date.setText(mDateFormat.format(record.getDate())
						+ " " + mTimeFormat.format(record.getDate()));
			} else {
				holder.date.setVisibility(View.INVISIBLE);
			}
			// direction
			if(record.getDirection() != null) {
				holder.directon_icon.setVisibility(View.VISIBLE);
				holder.directon_icon.setContentDescription(valueOfDirection(record.getDirection()));
				if(record.getDirection() == Direction.SEND)
					holder.directon_icon.setImageResource(R.drawable.left);
				else if(record.getDirection() == Direction.RECEIVE)
					holder.directon_icon.setImageResource(R.drawable.right);
				else
					throw new IllegalStateException("unknown Direction: "
							+ record.getDirection().name());
			} else {
				holder.directon_icon.setVisibility(View.INVISIBLE);
			}
			// has read or unread
			if(record.hasRead() != null && record.hasRead()){
				holder.unread_icon.setVisibility(View.INVISIBLE);
				setTextAppearance(holder, R.style.TextAppearance_AppTheme_ListItem_Weak);
				holder.parent.getBackground().setAlpha(100);
			} else {
				holder.unread_icon.setVisibility(View.VISIBLE);
				setTextAppearance(holder, R.style.TextAppearance_AppTheme_ListItem);
				holder.parent.getBackground().setAlpha(255);
			}
			holder.unread_icon.setContentDescription(valueOfHasRead(record.hasRead()));
		}
		/**
		 * 设置每项记录视图的颜色
		 */
		private void setViewColor(int position, ViewHolder holder) {
			Integer colorResId = null;
			switch(getStringResIdOfCode(mMockLog.get(position).getMessageToSet().getCode())) {
				case R.string.urgent:
					colorResId = R.color.urgent;
					break;
				case R.string.respond:
					colorResId = R.color.respond;
					break;
				case R.string.locate_now: case R.string.command:
					colorResId = R.color.command;
					break;
				default:
					colorResId = R.color.other_code;
			}
			holder.parent.setBackgroundColor(getResources().getColor(colorResId));
		}
		/**
		 * 设置文字显示效果（颜色、大小等）
		 * @param holder 要设置的数据项视图的{@link ViewHolder}
		 * @param resid TextAppearance资源ID
		 * @see TextView#setTextAppearance(Context, int)
		 */
		private void setTextAppearance(ViewHolder holder, int resid) {
			holder.code.setTextAppearance(mContext, resid);
			holder.body.setTextAppearance(mContext, resid);
			holder.contact_name.setTextAppearance(mContext, resid);
			holder.address.setTextAppearance(mContext, resid);
			holder.date.setTextAppearance(mContext, resid);
		}

		/**
		 * 保存对 每项记录的视图中各元素 的引用，避免每次重复执行<code>findViewById()</code>，也方便使用
		 * @author Team Orange
		 */
		private class ViewHolder {
			View parent;

			TextView code;
			TextView body;
			TextView contact_name;
			TextView address;
			TextView date;
			ImageView directon_icon;
			ImageView unread_icon;
		}
	}
}
