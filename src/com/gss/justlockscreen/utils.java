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
	 *  \brief ��ȡ�������й�����Ψһ��ʵ��
	 */
	static utils GetInstance()
	{
		return mUtils;
	}
	
	/*! \brief ��ʼ��
	 * \param[in] activity ������
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
	// ʱ�� ����
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
	// LOG ����
	//////////////////////////////////////////////////////////////////////////////////////////////
	void Logx(String s)
	{
		Log.i(TAG, s);
	}

	/** 
	 * @brief   ��¼LOG���ļ���
	 * @param s Log����
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
	// ֪ͨ ����
	//////////////////////////////////////////////////////////////////////////////////////////////
	NotificationManager GetNotificationManager()
	{
		return mNotificationManager;
	}
	///////////////////////////////////////////////////////////////////////////////////////////////
	// WIFI ����
	//////////////////////////////////////////////////////////////////////////////////////////////
	/*! 
	 * \brief ��ȡWIFI�Ƿ���
	 */
	boolean WIfiGetStatus()
	{
		return mWifiManager.isWifiEnabled();
	}

	/*! 
	 * \brief ����WIFI����
	 */
	void WifiOptService(boolean bOpen)
	{
		mWifiManager.setWifiEnabled(bOpen);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// GPRS ����
	//////////////////////////////////////////////////////////////////////////////////////////////
	/*! 
	 * \brief ��ȡGPRS ���������Ƿ���
	 */
	boolean GPRSGetStatus()
	{
		return gprsIsOpen();
	}
	
	/*! 
	 * \brief ����GPRS����
	 */
	void GPRSOptService(boolean bOpen)
	{
		gprsEnabled(bOpen);
	}
	

	// �򿪻�ر�GPRS
	private boolean gprsEnabled(boolean bEnable) {
		Object[] argObjects = null;

		boolean isOpen = gprsIsOpenMethod("getMobileDataEnabled");
		if (isOpen == !bEnable) {
			//Logx("gprsEnabled:" + bEnable);
			setGprsEnabled("setMobileDataEnabled", bEnable);
		}

		return isOpen;
	}
	
	// ����/�ر�GPRS
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

	// ���GPRS�Ƿ��
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
	// ��Դ��� ����
	//////////////////////////////////////////////////////////////////////////////////////////////
	/** 
	 * \brief ��ȡ��Ļ״̬
	 * @return false �ر�
	 */
	boolean ScreenGetStatus()
	{
		return mPowerManager.isScreenOn();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// ����
	//////////////////////////////////////////////////////////////////////////////////////////////
	/*! \brief ����
	 */
	void ScreenLockNow()
	{
		mDevicePolicyManager.lockNow();
	}

	/*!
	 *  \brief �ж��Ƿ���DevicePolicy�Ĳ���Ȩ��
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
	 * \brief ��������DevicePolicy�Ĳ���Ȩ��
	 */
	void TryHaveDevicePolicyPermission(Activity activity)
	{
		ComponentName componentName;  
		componentName = new ComponentName(activity, DeviceAdminSampleReceiver.class);  
		 // �����豸����(��ʽIntent) - ��AndroidManifest.xml���趨��Ӧ������  
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);  
        //Ȩ���б�  
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);  
        //����(additional explanation)  
       //intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "------ �������� ------");  
        activity.startActivityForResult(intent, 0);  
	}
	
	 public static class DeviceAdminSampleReceiver extends DeviceAdminReceiver {
	        void showToast(Context context, String msg) {
	            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	        }
	        @Override
	        public void onEnabled(Context context, Intent intent) {
	        	showToast(context, "Ȩ���Ѿ���ã����������ؼ�");
	        }

	        @Override
	        public CharSequence onDisableRequested(Context context, Intent intent) {
	            return ("onDisableRequested");
	        }

	        @Override
	        public void onDisabled(Context context, Intent intent) {
	        	showToast(context, "Ȩ���Ѿ��Ƴ������ٴγ���ж��!");
	        }
	    }
}
