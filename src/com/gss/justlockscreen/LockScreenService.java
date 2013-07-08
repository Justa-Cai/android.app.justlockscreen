package com.gss.justlockscreen;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.view.animation.Animation;
import android.widget.Toast;

public class LockScreenService extends Service implements OnSharedPreferenceChangeListener {
	private final String TAG = "LockScreenService";
	private utils mUtils = utils.GetInstance();

	// 屏幕超时
	private TimeoutTask mTimeoutTask;

	// 屏幕状态
	private boolean mbScreenOpen = false;
	private boolean mbScreenLockByWidgets = false, mbScreenWake = false;
	private int miTick = 0;

	private boolean mbWifiStatus, mbGprsStatus; // WIFI GPRS 状态
	private boolean mbWifiStatusChange=false, mbGprsStatusChange=false; // WIFI GPRS 状态 是否被软件操作过
	
	// 配置
	private SharedPreferences mSharedPreferences;
	private boolean mbSettingAutoClose=false, mbSettingAutoOpen=false;
	private boolean mbSettingWifi = false, mbSettingGPRS=false;
	private int miSettingAutoCloseTimeout = 10;
	private int miSettingAutoOpenTimeout = 5;
	private boolean mbSettingOnkeyInStatusbar = false;
	private boolean mbSettingEffectCRT = false;

	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {// 广播消息的处理
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction()
					.equals("com.gss.justlockscreen.widget.click")) {
				//if (mUtils.GPRSGetStatus())
				//	mUtils.GPRSOptService(false);
				if (mbSettingEffectCRT)
				{
					Intent intentEffect = new Intent(getApplicationContext(), CloseScreenActivity.class);
					intentEffect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intentEffect);
					//overridePendingTransition(Animation.INFINITE, Animation.INFINITE);
				}
				else
					mUtils.ScreenLockNow();
			}

			else if (intent.getAction().equals(
					"android.intent.action.SCREEN_OFF")) {
				// 锁屏
				mUtils.Logx("SCREEN OFF");
				miTick = 0;
				mbScreenWake = false;
				
				if (mbSettingAutoOpen && (mbWifiStatusChange|| mbGprsStatusChange))
				{
					// 在5秒以内未恢复 不要关闭网络状态
					// 或者 是网络都未开启
					
					// 检查下网络是否在5秒内被开启
					if (mUtils.WifiGetStatus() || mUtils.GPRSGetStatus()) {
						mbWifiStatus = mUtils.WifiGetStatus() | mbWifiStatus;
						mbGprsStatus = mUtils.GPRSGetStatus() | mbGprsStatus;
						mbScreenLockByWidgets = true;
					}
					else
						mbScreenLockByWidgets = false;
					mUtils.Logx("Not need restore status");
					// 
				}
				else {
					if (!mbSettingAutoClose)
						return;
					// 获取锁屏前的状态
					mbWifiStatus = mUtils.WifiGetStatus() | mbWifiStatus;;
					mbGprsStatus = mUtils.GPRSGetStatus() | mbGprsStatus;;
					// 开始计数
					if (mbWifiStatus || mbGprsStatus)
						mbScreenLockByWidgets = true;
					else
						mbScreenLockByWidgets = false;
				}
	
			} else if (intent.getAction().equals(
					"android.intent.action.SCREEN_ON")) {
				// 亮屏
				mUtils.Logx("SCREEN ON");
				mbScreenLockByWidgets = false;
				miTick = 0;
				
				if (!mbSettingAutoOpen)
					return;

				// 获取锁屏后 是否对网络进行了操控
				if (mbWifiStatusChange || mbGprsStatusChange) {
					mbScreenWake = true;
				}
			}
			else if (intent.getAction().equals("com.gss.justlockscreen.statusbar.click"))
			{
				mUtils.ScreenLockNow();
			}
			else {
				mUtils.Logx(intent.getAction());
			}

		}
	};

	private class TimeoutTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			int tick_dst = 10;
			while (!isCancelled()) {
				try {
					if (mbScreenLockByWidgets) {
						// 锁屏逻辑
						miTick += 1;
						mUtils.Logx("TICK:" + miTick);
						//mUtils.LogF(mUtils.GetTimeNow() + "TICK:" + miTick
						//		+ "\n");
						if (miTick == miSettingAutoCloseTimeout) {
							if (mbSettingGPRS && mUtils.GPRSGetStatus()) {
								mUtils.GPRSOptService(false);
								mbGprsStatusChange = true;
							}
							// mUtils.WifiOptService(false);
							// mbScreenLockByWidgets = false;
						} else if (miTick > miSettingAutoCloseTimeout) {
							if (!mUtils.GPRSGetStatus()) {
								if (mbSettingWifi)
									mUtils.WifiOptService(false);
								mbScreenLockByWidgets = false;
								miTick = 0;
								mbWifiStatusChange = true;
							}
						}
					} else if (mbScreenWake) {
						// 亮屏逻辑
						mUtils.Logx("TICK:" + miTick);
						if (miTick++ >= miSettingAutoOpenTimeout) {
							if (mbSettingWifi && mbWifiStatusChange)
								mUtils.WifiOptService(mbWifiStatus);
							if (mbSettingGPRS && mbGprsStatusChange)
								mUtils.WifiOptService(mbGprsStatus);
							mbScreenWake = false;
							mbWifiStatusChange = mbGprsStatusChange = false;
							mbWifiStatus = mbGprsStatus =  false;
						}
					}
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
			return null;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.gss.justlockscreen.widget.click");
		intentFilter.addAction("com.gss.justlockscreen.statusbar.click");
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		intentFilter.addAction(Intent.ACTION_SCREEN_ON);
		getBaseContext().registerReceiver(mIntentReceiver, intentFilter);
		
		mUtils.Init(getApplicationContext());
		InitSetting();
		
		// 状态栏
		CreateNotication();

		// locale status
		mbScreenOpen = false;
		mbScreenLockByWidgets = false;
		
		mTimeoutTask = new TimeoutTask();
		mTimeoutTask.execute();
	}
	
	private void InitSetting()
	{
		mSharedPreferences = mUtils.GetDefaultSharedPreferences();
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
		
		LoadSetting();

	}
	
	private void LoadSetting()
	{
		// 功能设置
		mbSettingAutoClose = mSharedPreferences.getBoolean("auto_close", false);
		mbSettingAutoOpen = mSharedPreferences.getBoolean("auto_open", false);
		mbSettingWifi = mSharedPreferences.getBoolean("auto_close_wifi", false);
		mbSettingGPRS = mSharedPreferences.getBoolean("auto_close_gprs", false);
		// 参数设置
		miSettingAutoCloseTimeout = Integer.parseInt(mSharedPreferences.getString("screen_after_close_timeout", "10"));
		miSettingAutoOpenTimeout = Integer.parseInt(mSharedPreferences.getString("screen_after_open_timeout", "5"));
		//  一键锁屏设置
		mbSettingOnkeyInStatusbar = mSharedPreferences.getBoolean("onekey_statusbar", false);
		mbSettingEffectCRT = mSharedPreferences.getBoolean("onekey_Effects_crt", false);
	}
	
	private void CreateNotication()
	{
		if (!mbSettingOnkeyInStatusbar)
			return;
		NotificationManager notificationManager = mUtils.GetNotificationManager();
		
		//定义Notification的各种属性
		int icon = R.drawable.lock; //通知图标
		CharSequence tickerText = "JUST LOCK"; //状态栏显示的通知文本提示
		long when = System.currentTimeMillis(); //通知产生的时间，会在通知信息里显示
		Notification notification = new Notification(icon,tickerText,when);
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.flags |= Notification.FLAG_NO_CLEAR;
		
		Intent intent= new Intent("com.gss.justlockscreen.statusbar.click");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,  
                intent, 0);  
        
        Context context = getApplicationContext(); //上下文
        CharSequence contentTitle = "Just Lock Screen!"; //通知栏标题
        CharSequence contentText = "Click me to Close Screen!"; //通知栏内容
        
        notification.setLatestEventInfo(context, contentTitle, contentText, pendingIntent);	
        notificationManager.notify(0,notification);
	}
	
	private void RemoveNotication()
	{
		NotificationManager notificationManager = mUtils.GetNotificationManager();
		notificationManager.cancel(0);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mTimeoutTask.cancel(true);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
	//	mUtils.Logx("onSharedPreferenceChanged");
		if (key.equals("onekey_statusbar"))
		{
			mbSettingOnkeyInStatusbar = mSharedPreferences.getBoolean("onekey_statusbar", false);
			if (mbSettingOnkeyInStatusbar)
				CreateNotication();
			else
				RemoveNotication();
		}
		else if (key.equals("auto_open"))
		{
			mbWifiStatus = mbGprsStatus = false;
			mbWifiStatusChange = mbGprsStatusChange = false;
		}
		
		LoadSetting();
	}
}
