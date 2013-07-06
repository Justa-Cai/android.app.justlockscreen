package com.gss.justlockscreen;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

public class utils {
	// 
	private final String TAG = "com.gss.justlockscreen";
	private final String LOG_DIR = "justlockscreen";
	private final String LOG_FILE = "log.txt";
	private String LOG_FILE_PATH ="";
			
	// Manager
	private DevicePolicyManager mDevicePolicyManager;
	private WifiManager mWifiManager;
	private ConnectivityManager mConnectivityManager;
	private PowerManager mPowerManager;
	private NotificationManager mNotificationManager;
	
	// handle
	private Context mContext;
	
	// Utils Instance
	static  public utils mUtils = new utils();
	
	private boolean mbInit = false;

	/*! 
	 *  \brief 获取程序运行过程中唯一的实例
	 */
	static utils GetInstance()
	{
		return mUtils;
	}
	
	/*! \brief 初始化
	 * \param[in] activity 主窗口
	 *  \note 
	 */
	void Init(Context context)
	{
		if (mbInit)
			return;
		mContext = context;
		mDevicePolicyManager = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);  
		mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
		mConnectivityManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		mPowerManager = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
		mNotificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		
		// 
		File f = Environment.getExternalStorageDirectory();
		String s =  f.getPath() + "/" + LOG_DIR;
		File f1 = new File(s);
		if (!f1.exists())
		{
			f1.mkdir();
		}
		LOG_FILE_PATH = f1.getPath() + "/" +  LOG_FILE;
		
		mbInit = true;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// 时间 操作
	//////////////////////////////////////////////////////////////////////////////////////////////
	String GetTimeNow()
	{
		String s;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss  ");
		Date curDate = new Date(System.currentTimeMillis());
		s = format.format(curDate);
		return s;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// LOG 操作
	//////////////////////////////////////////////////////////////////////////////////////////////
	void Logx(String s)
	{
		Log.i(TAG, s);
	}

	/** 
	 * @brief   记录LOG至文件中
	 * @param s Log内容
	 */
	void LogF(String s)
	{
		RandomAccessFile rf;
		try {
			File f = new File(LOG_FILE_PATH);
			if (!f.exists()) 
				f.createNewFile();
			
			rf = new RandomAccessFile(LOG_FILE_PATH, "rw");
			rf.seek(rf.length());
			rf.writeBytes(s);
			rf.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// 通知 操作
	//////////////////////////////////////////////////////////////////////////////////////////////
	NotificationManager GetNotificationManager()
	{
		return mNotificationManager;
	}
	///////////////////////////////////////////////////////////////////////////////////////////////
	// WIFI 操作
	//////////////////////////////////////////////////////////////////////////////////////////////
	/*! 
	 * \brief 获取WIFI是否开启
	 */
	boolean WIfiGetStatus()
	{
		return mWifiManager.isWifiEnabled();
	}

	/*! 
	 * \brief 操作WIFI网络
	 */
	void WifiOptService(boolean bOpen)
	{
		mWifiManager.setWifiEnabled(bOpen);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// GPRS 操作
	//////////////////////////////////////////////////////////////////////////////////////////////
	/*! 
	 * \brief 获取GPRS 数据网络是否开启
	 */
	boolean GPRSGetStatus()
	{
		return gprsIsOpen();
	}
	
	/*! 
	 * \brief 操作GPRS网络
	 */
	void GPRSOptService(boolean bOpen)
	{
		gprsEnabled(bOpen);
	}
	

	// 打开或关闭GPRS
	private boolean gprsEnabled(boolean bEnable) {
		Object[] argObjects = null;

		boolean isOpen = gprsIsOpenMethod("getMobileDataEnabled");
		if (isOpen == !bEnable) {
			//Logx("gprsEnabled:" + bEnable);
			setGprsEnabled("setMobileDataEnabled", bEnable);
		}

		return isOpen;
	}
	
	// 开启/关闭GPRS
	private void setGprsEnabled(String methodName, boolean isEnable) {
		Class cmClass = mConnectivityManager.getClass();
		Class[] argClasses = new Class[1];
		argClasses[0] = boolean.class;

		try {
			Method method = cmClass.getMethod(methodName, argClasses);
			method.invoke(mConnectivityManager, isEnable);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean gprsIsOpen() {
		return gprsIsOpenMethod("getMobileDataEnabled");
	}

	// 检测GPRS是否打开
	private boolean gprsIsOpenMethod(String methodName) {
		Class cmClass = mConnectivityManager.getClass();
		Class[] argClasses = null;
		Object[] argObject = null;

		Boolean isOpen = false;
		try {
			Method method = cmClass.getMethod(methodName, argClasses);

			isOpen = (Boolean) method.invoke(mConnectivityManager, argObject);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return isOpen;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// 电源相关 操作
	//////////////////////////////////////////////////////////////////////////////////////////////
	/** 
	 * \brief 获取屏幕状态
	 * @return false 关闭
	 */
	boolean ScreenGetStatus()
	{
		return mPowerManager.isScreenOn();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// 锁屏
	//////////////////////////////////////////////////////////////////////////////////////////////
	/*! \brief 锁屏
	 */
	void ScreenLockNow()
	{
		mDevicePolicyManager.lockNow();
	}

	/*!
	 *  \brief 判断是否有DevicePolicy的操作权限
	 * 
	 */
	Boolean IsHaveDevicePolicyPermission()
	{
		ComponentName componentName;  
		componentName = new ComponentName(mContext, DeviceAdminSampleReceiver.class);  
		boolean active = mDevicePolicyManager.isAdminActive(componentName);  
		return active;
	}

	/*! 
	 * \brief 尝试请求DevicePolicy的操作权限
	 */
	void TryHaveDevicePolicyPermission(Activity activity)
	{
		ComponentName componentName;  
		componentName = new ComponentName(activity, DeviceAdminSampleReceiver.class);  
		 // 启动设备管理(隐式Intent) - 在AndroidManifest.xml中设定相应过滤器  
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);  
        //权限列表  
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);  
        //描述(additional explanation)  
       //intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "------ 其他描述 ------");  
        activity.startActivityForResult(intent, 0);  
	}
	
	 public static class DeviceAdminSampleReceiver extends DeviceAdminReceiver {
	        void showToast(Context context, String msg) {
	            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	        }
	        @Override
	        public void onEnabled(Context context, Intent intent) {
	        	showToast(context, "权限已经获得，请添加桌面控件");
	        }

	        @Override
	        public CharSequence onDisableRequested(Context context, Intent intent) {
	            return ("onDisableRequested");
	        }

	        @Override
	        public void onDisabled(Context context, Intent intent) {
	        	showToast(context, "权限已经移除，请再次尝试卸载!");
	        }
	    }
}
