/**
 *
 */
package org.orange.familylink.fragment;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.orange.familylink.R;
import org.orange.familylink.data.Message.Code;
import org.orange.familylink.data.MessageLogRecord.Direction;
import org.orange.familylink.data.MessageLogRecord.Status;
import org.orange.familylink.database.Contract;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

/**
 * 日志{@link ListFragment}
 * @author Team Orange
 */
public class LogFragment extends ListFragment {
	private static final int LOADER_ID_CONTACTS = 1;
	private static final int LOADER_ID_LOG = 2;

	/** 用于把联系人ID映射为联系人名称的{@link Map} */
	private Map<Long, String> mContactIdToNameMap;
	/** 用于显示联系人筛选条件的{@link Spinner}的{@link SpinnerAdapter} */
	private SimpleCursorAdapter mAdapterForContactsSpinner;
	/** 用于显示消息日志的{@link ListView}的{@link ListAdapter} */
	private CursorAdapter mAdapterForLogList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// 因为super.onCreateView没有使用android.R.layout.list_content布局文件，
		// 我们也无法按此方法的文档说明来include list_content，
		// 只能在这用代码继承布局，以保留其内建indeterminant progress state

		// 创建自定义布局，把原来的root作为新布局的子元素
		View originalRoot = super.onCreateView(inflater, container, savedInstanceState);

		LinearLayout root = new LinearLayout(getActivity());
		root.setOrientation(LinearLayout.VERTICAL);
		// ------------------------------------------------------------------
		// 添加 原来的root
		root.addView(originalRoot, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT,
				1));
		// ------------------------------------------------------------------
		// 添加 筛选条件输入部件
		root.addView(createFilterWidget(), new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT,
				0));
		// ------------------------------------------------------------------

		// 按照Android设计指导，设置上下margin
		// http://developer.android.com/design/style/metrics-grids.html
		ListView listView = (ListView) originalRoot.findViewById(android.R.id.list);
		int margin = getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);
		View space = new View(getActivity());
		space.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, margin));
		listView.addHeaderView(space, null, false);
		listView.addFooterView(space, null, false);
		return root;
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Give some text to display if there is no data.
		setEmptyText(getResources().getText(R.string.no_message_record));

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
		loaderManager.initLoader(LOADER_ID_LOG, null, mLoaderCallbacks);
	}

	/**
	 * 创建筛选条件输入部件
	 * @return 新创建的筛选条件输入部件
	 */
	protected View createFilterWidget() {
		LinearLayout spinnersContainer = new LinearLayout(getActivity());
		spinnersContainer.setOrientation(LinearLayout.HORIZONTAL);

		Spinner spinner1 = new Spinner(getActivity());
		// Create an ArrayAdapter using the string array and a default spinner layout
		String[] status = getResources().getStringArray(R.array.message_status);
		if(status.length != 9)
			throw new IllegalStateException("Unexpected number of status. " +
					"Maybe because you only update on one place");
		ArrayAdapter<String> adapter = new MyHierarchicalArrayAdapter<String>(
				getActivity(), android.R.layout.simple_spinner_item, status) {
			@Override
			protected int getLevel(int position) {
				if(position != 0 && position != 1 && position != 4)
					return 2;
				else
					return 1;
			}
		};
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner1.setAdapter(adapter);
		spinnersContainer.addView(spinner1, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.MATCH_PARENT,
				1));

		Spinner spinner2 = new Spinner(getActivity());
		String[] code = getResources().getStringArray(R.array.code);
		if(code.length != 5)
			throw new IllegalStateException("Unexpected number of code. " +
					"Maybe because you only update on one place");
		adapter = new MyHierarchicalArrayAdapter<String>(
				getActivity(), android.R.layout.simple_spinner_item, code) {
			@Override
			protected int getLevel(int position) {
				if(position == 2 || position == 3)
					return 2;
				else
					return 1;
			}
		};
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner2.setAdapter(adapter);
		spinnersContainer.addView(spinner2, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.MATCH_PARENT,
				1));

		Spinner spinner3 = new Spinner(getActivity());
		mAdapterForContactsSpinner = new MySimpleCursorAdapterWithHeader(
				getActivity(),
				null,
				new String[]{Contract.Contacts.COLUMN_NAME_NAME},
				0,
				new String[]{getString(R.string.all)});
		spinner3.setAdapter(mAdapterForContactsSpinner);
		spinnersContainer.addView(spinner3, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.MATCH_PARENT,
				1));
		return spinnersContainer;
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
	private String valueOfCode(Integer code) {
		if(code == null)
			return getString(R.string.undefined);
		else if(Code.isInform(code))
			return getString(R.string.inform);
		else if(Code.isCommand(code))
			return getString(R.string.command);
		else if(!Code.isLegalCode(code))
			return getString(R.string.illegal_code, code.intValue());
		else
			throw new IllegalArgumentException("May be method Code.isLegalCode() not correct");
	}
	private String valueOfCodeExtra(Integer code, String delimiter) {
		if(delimiter.length() == 0)
			throw new IllegalArgumentException("you should set a delimiter");
		if(code == null || !Code.isLegalCode(code))
			return "";
		StringBuilder sb = new StringBuilder();
		if(Code.isInform(code)){
			if(Code.Extra.Inform.hasSetUrgent(code))
				sb.append(getString(R.string.urgent) + delimiter);
			if(Code.Extra.Inform.hasSetRespond(code))
				sb.append(getString(R.string.respond) + delimiter);
			if(Code.Extra.Inform.hasSetPulse(code))
				sb.append(getString(R.string.pulse) + delimiter);
		} else if(Code.isCommand(code)) {
			if(Code.Extra.Command.hasSetLocateNow(code))
				sb.append(getString(R.string.locate_now) + delimiter);
		} else {
			throw new IllegalArgumentException("May be method Code.isLegalCode() not correct");
		}
		int last = sb.lastIndexOf(delimiter);
		if(last > 0)
			return sb.substring(0, last);
		else
			return "";
	}
	private String valueOfDirection(Status status) {
		if(status == null)
			return "";
		Direction direction = status.getDirection();
		if(direction == Direction.SEND)
			return getString(R.string.send);
		else if(direction == Direction.RECEIVE)
			return getString(R.string.receive);
		else
			throw new UnsupportedOperationException("unsupport "+direction+" now.");
	}
	private String valueOfHasRead(Status status) {
		if(status == Status.HAVE_READ)
			return getString(R.string.has_read);
		else if(status == Status.UNREAD)
			return getString(R.string.unread);
		else
			return "";
	}

	protected final LoaderCallbacks<Cursor> mLoaderCallbacksForContacts =
			new LoaderCallbacks<Cursor>(){
		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			// One callback only to one loader, so we don't care about the ID.
			Uri baseUri = Contract.Contacts.CONTACTS_URI;
			String[] projection = {Contract.Contacts._ID, Contract.Contacts.COLUMN_NAME_NAME};
			return new CursorLoader(getActivity(), baseUri, projection, null, null, null);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			mAdapterForContactsSpinner.swapCursor(data);
			// setup Map
			Map<Long, String> id2nameNew = new HashMap<Long, String>(data.getCount());
			int indexId = data.getColumnIndex(Contract.Contacts._ID);
			int indexName = data.getColumnIndex(Contract.Contacts.COLUMN_NAME_NAME);
			while(data.moveToNext()) {
				id2nameNew.put(data.getLong(indexId), data.getString(indexName));
			}
			data.moveToPosition(-1);

			mContactIdToNameMap = id2nameNew;
			mAdapterForLogList.notifyDataSetChanged();
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			mAdapterForContactsSpinner.swapCursor(null);
			mContactIdToNameMap = null;
		}
	};
	protected final LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderCallbacks<Cursor>() {
		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			// This class only has one Loader, so we don't care about the ID.
			// First, pick the base URI to use depending on whether we are
			// currently filtering.
			Uri baseUri = null;
//			if (mFilter != null) {
//				baseUri = Uri.withAppendedPath(People.CONTENT_FILTER_URI, Uri.encode(mFilter));
//			} else {
			baseUri = Contract.Messages.MESSAGES_URI;
//			}

			String sortOrder = Contract.Messages.COLUMN_NAME_TIME + " DESC";
			// Now create and return a CursorLoader that will take care of
			// creating a Cursor for the data being displayed.
			return new CursorLoader(getActivity(), baseUri, null, null, null, sortOrder);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			// Swap the new cursor in.  (The framework will take care of closing the
			// old cursor once we return.)
			mAdapterForLogList.swapCursor(data);

			// The list should now be shown.
			if (isResumed()) {
				setListShown(true);
			} else {
				setListShownNoAnimation(true);
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
			View rootView = mInflater.inflate(R.layout.fragment_log_list_item, parent, false);
			// Creates a ViewHolder and store references to the two children views
			// we want to bind data to.
			ViewHolder holder = new ViewHolder();
			holder.code = (TextView) rootView.findViewById(R.id.code);
			holder.code_extra = (TextView) rootView.findViewById(R.id.code_extra);
			holder.body = (TextView) rootView.findViewById(R.id.body);
			holder.contact_name = (TextView) rootView.findViewById(R.id.contact_name);
			holder.address = (TextView) rootView.findViewById(R.id.address);
			holder.date = (TextView) rootView.findViewById(R.id.date);
			holder.directon_icon = (ImageView) rootView.findViewById(R.id.direction_icon);
			holder.unread_icon = (ImageView) rootView.findViewById(R.id.unread_icon);
			rootView.setTag(holder);
			return rootView;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			long contactId = cursor.getLong(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_CONTACT_ID));
			String address = cursor.getString(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_ADDRESS));
			long time = cursor.getLong(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_TIME));
			Date date = null;
			if(time != 0)
				date = new Date(time);
			String statusString = cursor.getString(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_STATUS));
			Status status = statusString != null ? Status.valueOf(statusString) : null;
			String body = cursor.getString(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_BODY));
			int code = cursor.getInt(cursor.getColumnIndex(Contract.Messages.COLUMN_NAME_CODE));

			ViewHolder holder = (ViewHolder) view.getTag();
			// message code
			holder.code.setText(valueOfCode(code));
			holder.code_extra.setText(valueOfCodeExtra(code, " | "));
			setViewColor(code, view);
			// message body
			if(body != null)
				holder.body.setText(body);
			else
				holder.body.setText("");
			// 联系人
			if(mContactIdToNameMap != null)
				holder.contact_name.setText(mContactIdToNameMap.get(contactId));
			// address
			if(address != null)
				holder.address.setText(address);
			else
				holder.address.setText(R.string.unknown);
			// date
			if(date != null) {
				holder.date.setVisibility(View.VISIBLE);
				holder.date.setText(DateFormat.getDateFormat(mContext).format(date)
						+ " " + DateFormat.getTimeFormat(mContext).format(date));
			} else {
				holder.date.setVisibility(View.INVISIBLE);
			}
			// status (include direction)
			if(status != null) {
				//direction
				holder.directon_icon.setVisibility(View.VISIBLE);
				Direction dirct = status.getDirection();
				if(dirct == Direction.SEND)
					holder.directon_icon.setImageResource(R.drawable.left);
				else if(dirct == Direction.RECEIVE)
					holder.directon_icon.setImageResource(R.drawable.right);
				else
					throw new IllegalStateException("unknown Direction: " + dirct.name());
			} else {
				holder.directon_icon.setVisibility(View.INVISIBLE);
			}
			holder.directon_icon.setContentDescription(valueOfDirection(status));
			// unread
			if((status == Status.UNREAD)) {
				holder.unread_icon.setVisibility(View.VISIBLE);
				setTextAppearance(holder, R.style.TextAppearance_AppTheme_ListItem);
				view.getBackground().setAlpha(255);
			} else {
				holder.unread_icon.setVisibility(View.INVISIBLE);
				setTextAppearance(holder, R.style.TextAppearance_AppTheme_ListItem_Weak);
				view.getBackground().setAlpha(100);
			}
			holder.unread_icon.setContentDescription(valueOfHasRead(status));
		}
		/**
		 * 设置每项记录视图的颜色
		 */
		private void setViewColor(Integer code, View rootView) {
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
					colorResId = R.color.other_code;
			}
			rootView.setBackgroundColor(getResources().getColor(colorResId));
		}
		/**
		 * 设置文字显示效果（颜色、大小等）
		 * @param holder 要设置的数据项视图的{@link ViewHolder}
		 * @param resid TextAppearance资源ID
		 * @see TextView#setTextAppearance(Context, int)
		 */
		private void setTextAppearance(ViewHolder holder, int resid) {
			holder.code.setTextAppearance(mContext, resid);
			holder.code_extra.setTextAppearance(mContext, resid);
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
			TextView code;
			TextView code_extra;
			TextView body;
			TextView contact_name;
			TextView address;
			TextView date;
			ImageView directon_icon;
			ImageView unread_icon;
		}
	}

	/**
	 * 带有层次结构的{@link ArrayAdapter}。低层次的item会被缩进。
	 * @author Team Orange
	 * @see MyHierarchicalArrayAdapter#getLevel(int)
	 */
	protected abstract class MyHierarchicalArrayAdapter<T> extends ArrayAdapter<T> {
		private Integer DefaultPaddingLeft, DefaultPaddingRight, DefaultPaddingTop, DefaultPaddingBottom;

		public MyHierarchicalArrayAdapter(Context context, int resource, T[] objects) {
			super(context, resource, objects);
		}

		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			View view = super.getDropDownView(position, convertView, parent);
			if(DefaultPaddingLeft == null) {
				DefaultPaddingLeft = view.getPaddingLeft();
				DefaultPaddingRight = view.getPaddingRight();
				DefaultPaddingTop = view.getPaddingTop();
				DefaultPaddingBottom = view.getPaddingBottom();
			}
			view.setPadding(DefaultPaddingLeft * getLevel(position),
					DefaultPaddingTop, DefaultPaddingRight, DefaultPaddingBottom);
			return view;
		}

		/**
		 * 取得位置为position的item的层级
		 * @param position 待判定层次的item的position
		 * @return 如果此item是最高层（类似&lt;h1&gt;）返回1；第二层返回2。以此类推
		 */
		protected abstract int getLevel(int position);
	}
	/**
	 * 带有标题的{@link SimpleCursorAdapter}
	 * <p>
	 * <strong>Note</strong>：这是一个特化的{@link SimpleCursorAdapter}，此类是用于{@link Spinner}的{@link SpinnerAdapter}，
	 * 其layout已经设置为了<code>android.R.layout.simple_spinner_item</code>，
	 * 其DropDownViewResource已设置为<code>android.R.layout.simple_spinner_dropdown_item</code>。
	 * @author Team Orange
	 * @see SimpleCursorAdapter#SimpleCursorAdapter(Context, int, Cursor, String[], int[], int)
	 * @see SimpleCursorAdapter#setDropDownViewResource(int)
	 */
	protected class MySimpleCursorAdapterWithHeader extends SimpleCursorAdapter {
		private final String[] mHeader;
		private final LayoutInflater mInflater;

		public MySimpleCursorAdapterWithHeader(Context context,
				Cursor c, String[] from, int flags, String[] header) {
			super(context, android.R.layout.simple_spinner_item, c, from,
					new int[]{android.R.id.text1}, flags);
			setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			mHeader = header;
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return super.getCount() + mHeader.length;
		}

		@Override
		public Object getItem(int position) {
			if(!isHeader(position))
				return super.getItem(getPositionWithoutHeader(position));
			else
				return mHeader[position];
		}

		@Override
		public long getItemId(int position) {
			if(!isHeader(position))
				return super.getItemId(getPositionWithoutHeader(position));
			else
				return position - mHeader.length;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(!isHeader(position))
				return super.getView(getPositionWithoutHeader(position), convertView, parent);
			else {
				if(convertView == null)
					convertView = mInflater.inflate(android.R.layout.simple_spinner_item, parent, false);
				((TextView)convertView.findViewById(android.R.id.text1)).setText(mHeader[position]);
				return convertView;
			}
		}

		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			if(!isHeader(position))
				return super.getDropDownView(getPositionWithoutHeader(position), convertView, parent);
			else {
				if(convertView == null)
					convertView = mInflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
				((TextView)convertView.findViewById(android.R.id.text1)).setText(mHeader[position]);
				return convertView;
			}
		}

		@Override
		public int getViewTypeCount() {
			return 1;
		}
		@Override
		public int getItemViewType(int position) {
			return 0;
		}

		public boolean isHeader(int position) {
			return position < mHeader.length;
		}
		public int getPositionWithoutHeader(int rawPosition) {
			return rawPosition - mHeader.length;
		}
	}
}
