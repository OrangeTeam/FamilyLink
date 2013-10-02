package org.orange.familylink.util;

import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
/**
 * 反向地址解析，把经纬度转换为位置信息
 * @author Orange Team
 *
 */
public class ConvertUtil {
	public static String getAddress(final double longitude, final double latitude){
		//生成一个异步任务
		FutureTask<String> task = new FutureTask<String>(
				new Callable<String>(){
					public String call() throws Exception{
						//定义一个HttpClient，用于向指定地址发送一个请求
						HttpClient client = new DefaultHttpClient();
						//向指定地址发送一个GET请求
						HttpGet httpGet = new HttpGet("http://maps.google.com/maps/"
								+ "api/geocode/json?latlng="
								+ latitude + "," + longitude
								+ "&sensor=false");

						//用于向该请求为一个简体中文环境，且返回的语言为中文
						httpGet.addHeader("Accept-Charset", "GBK;q=0.7,*;q=0.3");
						httpGet.addHeader("Accept-Language", "zh-CN,zh,;q=0.8");

						StringBuilder sb = new StringBuilder();
						//执行请求
						HttpResponse response = client.execute(httpGet);
						HttpEntity entity = response.getEntity();
						//获取服务器响应的字符串
						InputStreamReader br = new InputStreamReader(entity.getContent(), "utf-8");
						int b;
						while((b = br.read()) != -1){
							sb.append((char)b);
						}
						//把服务器相应的字符串转换为JSONObject
						JSONObject jsonObject = new JSONObject(sb.toString());
						//解析响应结果中的地址数据
						return jsonObject.getJSONArray("results").getJSONObject(0).getString("formatted_address");
					}
				});
		try{
			new Thread(task).start();
			return task.get();
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

}
