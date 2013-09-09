package org.orange.familylink.util;

import org.orange.familylink.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

/**
 * 网络操作类，用来判断是否有网络，有哪些类型网络可用，无网络时询问是否打开网络设置
 * @author Orange Team
 *
 */
public class Network {
	/**
	 * 
	 */
	public static final String FRAGMENT_TAG_NO_CONNECTION_DIALOG =
			Network.class.getName() + "noConnectionDialog";
	/**
	 * 禁止实例化（因为本类只有静态方法，实例无用）
	 */
	private Network(){}

	/**
	 * 获取网络连接服务管理
	 * @param context 应用程序环境
	 * @return ConnetivityManager
	 */
	public static ConnectivityManager getConnectivityManager(Context context){
		return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	/**
	 * 取得当前正在使用的网络类型。
	 * @return null for no Internet connection or one of TYPE_MOBILE, TYPE_WIFI, TYPE_WIMAX,
	 * TYPE_ETHERNET, TYPE_BLUETOOTH, or other types defined by ConnectivityManager
	 */
	public static Integer getActiveNetworkType(Context context){
		NetworkInfo networkInfo = getConnectivityManager(context).getActiveNetworkInfo();
		return networkInfo==null||!networkInfo.isConnected() ? null : networkInfo.getType();
	}
	/**
	 * 是否可以建立Internet连接。
	 * @return 可以返回true；不可能建立返回false
	 */
	public static boolean isConnected(Context context){
		return getActiveNetworkType(context) != null;
	}
	/**
	 * 查看数据连接是否可用
	 * @return 是返回true；不是返回false
	 */
	public static boolean isMobileConnected(Context context){
		Integer type = getActiveNetworkType(context);
		return type!=null && type==ConnectivityManager.TYPE_MOBILE;
	}
	/**
	 * 查看wifi连接是否可用
	 * @return 是返回true；不是返回false
	 */
	public static boolean isWifiConnected(Context context){
		Integer type = getActiveNetworkType(context);
		return type!=null && type==ConnectivityManager.TYPE_WIFI;
	}

	/**
	 * 打开网络设置Activity。
	 * @return 成功打开返回true，失败返回false
	 */
	public static boolean openWirelessSettings(Context context){
		Intent intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
		// 验证此intent可被响应
		if(context.getPackageManager().queryIntentActivities(intent, 0).size() > 0){
			context.startActivity(intent);
			return true;
		}else
			return false;
	}

	/**
	 * 打开“无网络连接”对话框，提问是否打开网络设置Activity。
	 */
	public static void openNoConnectionDialog(FragmentManager manager){
		NoConnectionDialogFragment newDialog = new NoConnectionDialogFragment();
		newDialog.show(manager, FRAGMENT_TAG_NO_CONNECTION_DIALOG);
	}

	/**
	 * “无网络连接”对话框。提问是否打开网络设置Activity。
	 * @author Orange Team
	 */
	public static class NoConnectionDialogFragment extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.no_connection_title)
			.setMessage(R.string.no_connection_message)
			.setPositiveButton(android.R.string.yes, new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					openWirelessSettings(getActivity());
				}
			})
			.setNegativeButton(android.R.string.cancel, new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			});
			return builder.create();
		}
	}

}
