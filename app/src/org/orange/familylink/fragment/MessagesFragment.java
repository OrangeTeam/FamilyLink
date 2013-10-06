/**
 *
 */
package org.orange.familylink.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.orange.familylink.R;
import org.orange.familylink.data.Message;
import org.orange.familylink.data.Message.Code;
import org.orange.familylink.data.MessageLogRecord.Direction;
import org.orange.familylink.data.MessageLogRecord.Status;
import org.orange.familylink.data.Settings;
import org.orange.familylink.data.UrgentMessageBody;
import org.orange.familylink.database.Contract;
import org.orange.familylink.fragment.MessagesFragment.MessagesSender.MessageWrapper;
import org.orange.familylink.sms.SmsMessage;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

/**
 * 日志{@link ListFragment}
 * @author Team Orange
 */
public class MessagesFragment extends ListFragment {
	/** 参数Key：需要选中的消息IDs */
	public static final String ARGUMENT_KEY_IDS =
			MessagesFragment.class.getName() + ".argument.IDS";
	/** 参数Key：需要设置的 <em>消息状态</em> 筛选条件，用{@link Status}设置 */
	public static final String ARGUMENT_KEY_STATUS =
			MessagesFragment.class.getName() + ".argument.STATUS";
	/** 参数Key：需要设置的 <em>消息方向</em> 筛选条件，用{@link Direction}设置 */
	public static final String ARGUMENT_KEY_DIRECTION =
			MessagesFragment.class.getName() + ".argument.DIRECTION";

	private static final String STATE_CHECKED_ITEM_IDS =
			MessagesFragment.class.getName() + ".state.CHECKED_ITEM_IDS";
	private static final int LOADER_ID_CONTACTS = 1;
	private static final int LOADER_ID_LOG = 2;

	/** 当前启动的{@link ActionMode}；如果没有启动，则为null */
	private ActionMode mActionMode;
	/** 最近选中的消息的IDs，用于恢复之前的选中状态。仅在{@link #mActionMode} != null时有效 */
	private long[] mCheckedItemids;
	/** 用于把联系人ID映射为联系人名称的{@link Map} */
	private Map<Long, String> mContactIdToNameMap;
	/** 用于显示消息日志的{@link ListView}的{@link ListAdapter} */
	private CursorAdapter mAdapterForLogList;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// 尝试恢复选中状态
		if(savedInstanceState != null) {
			long[] ids = savedInstanceState.getLongArray(STATE_CHECKED_ITEM_IDS);
			if(ids != null)
				mCheckedItemids = ids;
		}
		// 处理Fragment的参数
		Bundle arguments = getArguments();
		if(arguments != null) {
			long[] argumentIds = arguments.getLongArray(ARGUMENT_KEY_IDS);
			if(argumentIds != null) {
				mCheckedItemids = argumentIds;
			}
		}

		// Give some text to display if there is no data.
		setEmptyText(getResources().getText(R.string.no_message_record));

		// 设置ListView
		ListView listView = getListView();
		// 设置ListView为多选模式
		listView.setItemsCanFocus(false);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setMultiChoiceModeListener(mMultiChoiceModeListener);
		// 设置ListView的FastScrollBar
		listView.setFastScrollEnabled(true);
		listView.setFastScrollAlwaysVisible(false);
		listView.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_LEFT);

		// We have a menu item to show in action bar.
		setHasOptionsMenu(true);

		// Create an empty adapter we will use to display the loaded data.
		mAdapterForLogList = new LogAdapter(getActivity(), null, 0);
		setListAdapter(mAdapterForLogList);

		// Start out with a progress indicator.
		setListShown(false);

		// Prepare the loader.  Either re-connect with an existing one,
		// or start a new one.
		LoaderManager loaderManager = getLoaderManager();
		loaderManager.initLoader(LOADER_ID_CONTACTS, null, mLoaderCallbacksForContacts);
		loaderManager.initLoader(LOADER_ID_LOG, null, mLoaderCallbacksForLogList);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(mActionMode != null)
			outState.putLongArray(STATE_CHECKED_ITEM_IDS, getListView().getCheckedItemIds());
		else
			outState.putLongArray(STATE_CHECKED_ITEM_IDS, null);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if(mActionMode == null)
			l.setItemChecked(position, true); //触发ActionModes
	}

	/**
	 * 选中指定ID的日志消息记录
	 * <p>
	 * <strong>Note</strong>：此方法 <em>不会</em> 自动清除以前选中的内容，只会选中指定的消息（如果有的话）
	 * @param ids 应当选中的消息（如果有的话）的ID
	 * @return 实际选中的消息的个数
	 * @see #setItemsCheckedByIds(long[])
	 */
	public int checkItemsByIds(long[] ids) {
		ListView listView = getListView();
		if(listView == null || mAdapterForLogList == null || mAdapterForLogList.isEmpty()
				|| ids == null || ids.length == 0)
			return 0;
		int counter = 0;
		// 对ids、LogItems排序
		SortedMap<Long, Integer> items = new TreeMap<Long, Integer>();
		for(int i = 0 ; i < mAdapterForLogList.getCount() ; i++) {
			items.put(mAdapterForLogList.getItemId(i), i);
		}
		Arrays.sort(ids);
		// 依次序比较ids和items中的元素
		int idIndex = 0;
		Iterator<Entry<Long, Integer>> iterator = items.entrySet().iterator();
		long id = ids[idIndex++];	// idIndex和iterator类似指针，id和item类似上个元素的值
		Entry<Long, Integer> item = iterator.next();
		while(idIndex < ids.length && iterator.hasNext()) {
			if(id == item.getKey()) {
				listView.setItemChecked(item.getValue(), true);
				counter++;
				id = ids[idIndex++];
				item = iterator.next();
			} else if(id > item.getKey()) {
				item = iterator.next();
			} else {	// id < item.getKey()
				id = ids[idIndex++];
			}
		}
		// id或者item是最后一个元素，比较一下这个边界元素（如果另一个还有，可能还需比较后续元素）
		if(id == item.getKey()) {
			listView.setItemChecked(item.getValue(), true);
			counter++;
		} else if(id > item.getKey()) {
			// 尝试后移item
			while(id > item.getKey() && iterator.hasNext()) {
				item = iterator.next();
				if(id == item.getKey()) {
					listView.setItemChecked(item.getValue(), true);
					counter++;
				}
			}
		} else {	// id < item.getKey()
			while(id < item.getKey() && idIndex < ids.length) {
				id = ids[idIndex++];
				if(id == item.getKey()) {
					listView.setItemChecked(item.getValue(), true);
					counter++;
				}
			}
		}
		return counter;
	}
	/**
	 * 选中指定ID的日志消息记录，并移动到第一个选中的消息处
	 * <p>
	 * <strong>Note</strong>：此方法 <em>会</em> 自动清除以前选中的内容，再选中指定的消息（如果有的话）
	 * @param ids 应当选中的消息（如果有的话）的ID
	 * @return 实际选中的消息的个数
	 * @see #checkItemsByIds(long[])
	 */
	public int setItemsCheckedByIds(long[] ids) {
		ListView listView = getListView();
		if(listView == null)
			return 0;
		listView.clearChoices();
		int checkedCount = checkItemsByIds(ids);
		if(mActionMode != null)
			mMultiChoiceModeListener.updateTitle(mActionMode);

		if(checkedCount >= 1) {
			List<Integer> positions = getCheckedItemPositions();
			int min = positions.get(0);
			for(Integer position : positions) {
				if(min > position)
					min = position;
			}
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				listView.smoothScrollToPositionFromTop(min, 0);
			else
				listView.smoothScrollToPosition(min);
		}
		return checkedCount;
	}

	/** 取得选中的Items的位置 */
	public List<Integer> getCheckedItemPositions() {
		List<Integer> checkeditems = new ArrayList<Integer>();
		ListView listView = getListView();
		if(listView == null)
			return checkeditems;
		SparseBooleanArray checkedPositionsBool = listView.getCheckedItemPositions();
		if(checkedPositionsBool == null)
			return checkeditems;

		for (int i = 0; i < checkedPositionsBool.size(); i++) {
			if (checkedPositionsBool.valueAt(i)) {
				checkeditems.add(checkedPositionsBool.keyAt(i));
			}
		}
		return checkeditems;
	}

	private int getImportantCode(Integer code) {
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
	private String valueOfUrgentMessageBodyType(UrgentMessageBody.Type type) {
		if(type == null)
			return getString(R.string.undefined);
		switch (type) {
		case SEEK_HELP:
			return getString(R.string.seek_help);
		case FALL_DOWN_ALARM:
			return getString(R.string.fall_down_alarm);
		default:
			throw new UnsupportedOperationException("unsupported type: " + type);
		}
	}
	private String valueOfSendStatus(Status status) {
		if(status == null || status.getDirection() != Direction.SEND)
			return "";
		switch(status) {
		case SENDING:
			return getString(R.string.sending);
		case SENT:
			return getString(R.string.sent);
		case DELIVERED:
			return getString(R.string.delivered);
		case FAILED_TO_SEND:
			return getString(R.string.failed_to_send);
		default:
			throw new UnsupportedOperationException("unsupport "+status+" now.");
		}
	}

	protected final LoaderCallbacks<Cursor> mLoaderCallbacksForContacts =
			new LoaderCallbacks<Cursor>(){
		private final Uri baseUri = Contract.Contacts.CONTACTS_URI;;
		private final String[] projection = {Contract.Contacts._ID, Contract.Contacts.COLUMN_NAME_NAME};
		private static final String sortOrder = Contract.Contacts.COLUMN_NAME_NAME;

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			// One callback only to one loader, so we don't care about the ID.
			return new CursorLoader(getActivity(), baseUri, projection, null, null, sortOrder);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			// setup Map
			Map<Long, String> id2nameNew = new HashMap<Long, String>(data.getCount());
			int indexId = data.getColumnIndex(Contract.Contacts._ID);
			int indexName = data.getColumnIndex(Contract.Contacts.COLUMN_NAME_NAME);
			data.moveToPosition(-1);
			while(data.moveToNext()) {
				id2nameNew.put(data.getLong(indexId), data.getString(indexName));
			}
			mContactIdToNameMap = id2nameNew;
			mAdapterForLogList.notifyDataSetChanged();
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			mContactIdToNameMap = null;
		}
	};
	protected final LoaderCallbacks<Cursor> mLoaderCallbacksForLogList =
			new LoaderCallbacks<Cursor>() {
		private final Uri baseUri = Contract.Messages.MESSAGES_URI;
		private final String[] projection = {
				Contract.Messages._ID,
				Contract.Messages.COLUMN_NAME_ADDRESS,
				Contract.Messages.COLUMN_NAME_BODY,
				Contract.Messages.COLUMN_NAME_CODE,
				Contract.Messages.COLUMN_NAME_CONTACT_ID,
				Contract.Messages.COLUMN_NAME_STATUS,
				Contract.Messages.COLUMN_NAME_TIME };
		private final String sortOrder = Contract.Messages.COLUMN_NAME_TIME + " DESC";

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			// This class only has one Loader, so we don't care about the ID.
			// 根据当前筛选条件，构造where子句
			String selection = null;
			Bundle arguments = getArguments();
			if(arguments != null) {
				if(arguments.containsKey(ARGUMENT_KEY_STATUS))
					selection = Contract.Messages.getWhereClause(
							(Status) arguments.getSerializable(ARGUMENT_KEY_STATUS));
				else if(arguments.containsKey(ARGUMENT_KEY_DIRECTION))
					selection = Contract.Messages.getWhereClause(
							(Direction) arguments.getSerializable(ARGUMENT_KEY_DIRECTION));
			}
			// Now create and return a CursorLoader that will take care of
			// creating a Cursor for the data being displayed.
			return new CursorLoader(getActivity(), baseUri, projection, selection, null, sortOrder);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			// Swap the new cursor in.  (The framework will take care of closing the
			// old cursor once we return.)
			Cursor oldCursor = mAdapterForLogList.swapCursor(data);

			// The list should now be shown.
			if (isResumed()) {
				setListShown(true);
			} else {
				setListShownNoAnimation(true);
			}
			// 如果处于多选状态或第一次获得日志信息，尝试恢复以前的状态
			if((mActionMode != null || oldCursor == null) && mCheckedItemids != null) {
				setItemsCheckedByIds(mCheckedItemids);
				mCheckedItemids = null;
			}
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			// This is called when the last Cursor provided to onLoadFinished()
			// above is about to be closed.  We need to make sure we are no
			// longer using it.
			mAdapterForLogList.swapCursor(null);
		}
	};

	protected final LogMultiChoiceModeListener mMultiChoiceModeListener =
			new LogMultiChoiceModeListener();
	protected class LogMultiChoiceModeListener implements MultiChoiceModeListener {
		private int mUnretransmittableCount = 0;
		private MessagesSender mMessagesSender = null;
		private List<AsyncQueryHandler> mDeletehandlers = new LinkedList<AsyncQueryHandler>();

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			mActionMode = mode;
			// Inflate the menu for the contextual action bar
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.fragment_log_action_mode, menu);
			return true;
		}
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// Here you can perform updates to the CAB due to
			// an invalidate() request
			MenuItem retransmit = menu.findItem(R.id.retransmit);
			if(canRetransmit()) {
				retransmit.setVisible(true);
				retransmit.setEnabled(true);
			} else {
				retransmit.setVisible(false);
				retransmit.setEnabled(false);
			}
			return true;
		}
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			// Respond to clicks on the actions in the CAB
			switch (item.getItemId()) {
			case R.id.retransmit:
				if(mMessagesSender == null)
					retransmitSelectedItems();
				else {
					Toast.makeText(
							getActivity(),
							R.string.prompt_one_retransmission_at_a_time,
							Toast.LENGTH_LONG)
						.show();
					return true;
				}
				break;
			case R.id.delete:
				deletetSelectedItems();
				break;
			default:
				return false;
			}
			mode.finish();
			return true;
		}
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// Here you can make any necessary updates to the activity when
			// the CAB is removed. By default, selected items are deselected/unchecked.
			mActionMode = null;
		}

		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position,
				long id, boolean checked) {
			updateTitle(mode);
			Cursor cursor = (Cursor) getListView().getItemAtPosition(position);
			String statusString = cursor.getString(
					cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_STATUS));
			Status status = statusString != null ? Status.valueOf(statusString) : null;
			if(!isRetransmittable(status)) {
				boolean oldCanRetransmit = canRetransmit();
				if(checked)
					mUnretransmittableCount++;
				else
					mUnretransmittableCount--;
				boolean newCanRetransmit = canRetransmit();
				if(oldCanRetransmit != newCanRetransmit)
					mode.invalidate();
			}
		}
		public void updateTitle(ActionMode mode) {
			int count = getListView().getCheckedItemCount();
			String title = getString(
					count != 1 ? R.string.checked_n_messages : R.string.checked_one_message,
					count);
			mode.setTitle(title);
		}

		protected boolean isRetransmittable(Status status) {
			return status == Status.FAILED_TO_SEND;
		}
		protected boolean canRetransmit() {
			return mUnretransmittableCount == 0;
		}
		protected void retransmitSelectedItems() {
			List<Integer> items = getCheckedItemPositions();
			MessageWrapper[] messages = new MessageWrapper[items.size()];
			int index = 0;
			for(int position : items) {
				MessageWrapper message = new MessageWrapper();
				message.message = new SmsMessage();
				Cursor cursor = (Cursor) mAdapterForLogList.getItem(position);
				int indexId = cursor.getColumnIndex(Contract.Messages._ID);
				int indexCode = cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_CODE);
				int indexBody = cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_BODY);
				int indexDest = cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_ADDRESS);
				if(!cursor.isNull(indexCode))
					message.message.setCode(cursor.getInt(indexCode));
				message.message.setBody(cursor.getString(indexBody));
				message.dest = cursor.getString(indexDest);
				// 设置Uri
				long _id = cursor.getLong(indexId);
				Uri uri = Contract.Messages.MESSAGES_ID_URI;
				uri = ContentUris.withAppendedId(uri, _id);
				message.uri = uri;

				messages[index++] = message;
			}
			mMessagesSender = new MessagesSender(getActivity()) {
				@Override
				protected void onPostExecute(Void result) {
					super.onPostExecute(result);
					mMessagesSender = null;
				}
			};
			mMessagesSender.execute(messages);
		}

		protected void deletetSelectedItems() {
			// 构造删除条件（where子句）
			long[] ids = getListView().getCheckedItemIds();
			if(ids == null)
				throw new NullPointerException("ids is null");
			if(ids.length == 0)
				return;
			StringBuilder sb = new StringBuilder();
			for(long id : ids)
				sb.append(id + ",");
			sb.deleteCharAt(sb.length() - 1);
			String selection = Contract.Messages._ID + " IN ( " + sb.toString() + " )";

			AsyncQueryHandler handler = new AsyncQueryHandler(
					getActivity().getContentResolver()) {
				@Override
				protected void onDeleteComplete(int token,
						Object cookie, int result) {
					mDeletehandlers.remove(this);
					if(mDeletehandlers.isEmpty())
						getActivity().setProgressBarIndeterminateVisibility(false);
					Toast.makeText(
							getActivity(),
							getString(R.string.prompt_delete_messages_successfully, result),
							Toast.LENGTH_LONG)
						.show();
				}
			};
			mDeletehandlers.add(handler);
			getActivity().setProgressBarIndeterminateVisibility(true);
			handler.startDelete(-1, null, Contract.Messages.MESSAGES_URI, selection, null);
		}
	}

	protected class LogAdapter extends CursorAdapter {
		private final Context mContext;
		private final LayoutInflater mInflater;

		public LogAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
			mContext = context;
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View rootView = mInflater.inflate(R.layout.fragment_messages_list_item, parent, false);
			// Creates a ViewHolder and store references to the two children views
			// we want to bind data to.
			ViewHolder holder = new ViewHolder();
			holder.senderAndReceiver = (TextView) rootView.findViewById(R.id.sender_and_receiver);
			holder.body = (TextView) rootView.findViewById(R.id.body);
			holder.send_status = (TextView) rootView.findViewById(R.id.send_status);
			holder.date = (TextView) rootView.findViewById(R.id.date);
			holder.type_icon = (ImageView) rootView.findViewById(R.id.type_icon);
			rootView.setTag(holder);
			return rootView;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			long contactId = cursor.getLong(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_CONTACT_ID));
			String address = cursor.getString(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_ADDRESS));
			Date date = null;
			if(!cursor.isNull(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_TIME))) {
				long time = cursor.getLong(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_TIME));
				date = new Date(time);
			}
			String statusString = cursor.getString(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_STATUS));
			Status status = statusString != null ? Status.valueOf(statusString) : null;
			String body = cursor.getString(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_BODY));
			Integer code = null;
			if(!cursor.isNull(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_CODE))) {
				code = cursor.getInt(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_CODE));
			}

			ViewHolder holder = (ViewHolder) view.getTag();
			// message code
			setViewColor(code, holder.type_icon);
			// message body
			holder.body.setText(formatBody(code, body));
			// 联系人
			String contactName = null;
			if(mContactIdToNameMap != null)
				contactName = mContactIdToNameMap.get(contactId);
			if(contactName == null)
				contactName = getString(R.string.unknown);
			// address
			String senderAndReceiver;
			if(address != null)
				senderAndReceiver = getString(R.string.contact_formatter, contactName, address);
			else
				senderAndReceiver = contactName;
			// direction (in status)
			if(status != null) {
				Direction dirct = status.getDirection();
				if(dirct == Direction.SEND) {
					senderAndReceiver = getString(R.string.me) + "→" + senderAndReceiver;
				} else if(dirct == Direction.RECEIVE) {
					senderAndReceiver = senderAndReceiver + " → " + getString(R.string.me);
				} else
					throw new IllegalStateException("unknown Direction: " + dirct.name());
			}
			holder.senderAndReceiver.setText(senderAndReceiver);
			// date
			if(date != null) {
				holder.date.setVisibility(View.VISIBLE);
				holder.date.setText(DateFormat.getDateFormat(mContext).format(date)
						+ " " + DateFormat.getTimeFormat(mContext).format(date));
			} else {
				holder.date.setVisibility(View.INVISIBLE);
			}
			// is it sent
			if(status == null || status.getDirection() != Direction.SEND)
				holder.send_status.setVisibility(View.GONE);
			else {
				holder.send_status.setText(
					getString(R.string.send_status_formatter, valueOfSendStatus(status)));
				holder.send_status.setVisibility(View.VISIBLE);
			}
		}
		private CharSequence formatBody(Integer code, String body) {
			if(body == null)
				return "";
			if(code == null || !Code.Extra.Inform.hasSetUrgent(code))
				return body;
			final UrgentMessageBody urgentBody = new Gson().fromJson(body, UrgentMessageBody.class);
			StringBuilder sb = new StringBuilder();
			final UrgentMessageBody.Type type = urgentBody.getType();
			if(type != null)
				sb.append(getString(R.string.type) + ": " + valueOfUrgentMessageBodyType(type) + "\n");
			if(urgentBody.getContent() != null)
				sb.append(getString(R.string.content) + ": " + urgentBody.getContent() + "\n");
			if(urgentBody.containsPosition()) {
				String location = urgentBody.getPositionLatitude() + "," + urgentBody.getPositionLongitude();
				sb.append(getString(R.string.position_of_sender) + ": " + location);
			}
			return sb;
		}
		/**
		 * 根据消息类型，设置消息视图的颜色
		 */
		private void setViewColor(Integer code, ImageView typeIcon) {
			Integer colorResId = null;
			switch(getImportantCode(code)) {
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
					colorResId = android.R.color.transparent;
			}
			typeIcon.setImageResource(colorResId);
		}
		/**
		 * 保存对 每项记录的视图中各元素 的引用，避免每次重复执行<code>findViewById()</code>，也方便使用
		 * @author Team Orange
		 */
		private class ViewHolder {
			TextView senderAndReceiver;
			TextView body;
			TextView send_status;
			TextView date;
			ImageView type_icon;
		}
	}

	/**
	 * 用于发送消息的{@link AsyncTask}。支持批量发送。
	 * @author Team Orange
	 */
	protected static class MessagesSender extends
					AsyncTask<MessageWrapper, Integer, Void> {
		private final Context mContext;

		public MessagesSender(Context context) {
			super();
			mContext = context;
		}

		@Override
		protected Void doInBackground(MessageWrapper... messages) {
			if(messages == null)
				return null;
			final int total = messages.length;
			int finished = 0;
			String password = Settings.getPassword(mContext);
			for(MessageWrapper message : messages) {
				if(message.uri != null)
					message.message.send(mContext, message.uri, message.dest, password);
				else
					message.message.sendAndSave(mContext, message.contactId, message.dest, password);
				publishProgress(total, ++finished);
				if(isCancelled())
					break;
			}
			return null;
		}

		/**
		 * {@link Message}的包装器，作为{@link MessagesSender}的泛型参数，用于传递发送参数。
		 * @author Team Orange
		 */
		public static class MessageWrapper {
			/** 要发送的消息主体，也将使用此对象的方法发送消息 */
			public Message message;
			/** 如果用于重发，可以不设置此字段；如果发送新消息，请设置为对方联系人ID */
			public Long contactId;
			/** 目标地址。如对方手机号 */
			public String dest;
			/** 如果用于重发，此属性为原消息的{@link Uri}；如果发送新消息，此属性应设置为null */
			public Uri uri;
		}
	}
}
