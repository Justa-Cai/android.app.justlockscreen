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
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class LockScreenService extends Service {
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

	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {// 广播消息的处理
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction()
					.equals("com.gss.justlockscreen.widget.click")) {
				if (mUtils.GPRSGetStatus())
					mUtils.GPRSOptService(false);
				mUtils.ScreenLockNow();
				mbScreenLockByWidgets = true;
			}

			else if (intent.getAction().equals(
					"android.intent.action.SCREEN_OFF")) {
				mUtils.Logx("SCREEN OFF");
				miTick = 0;
				mbScreenWake = false;
				
				//mbWifiStatusChange = mbGprsStatusChange = false;
				if (mbWifiStatusChange|| mbGprsStatusChange)
				{
					// 在5秒以内未恢复 不要关闭网络状态
					mbScreenLockByWidgets = false;
					mUtils.Logx("Not need restore status");
				}
				else {
				// 获取锁屏前的状态
					mbWifiStatus = mUtils.WIfiGetStatus();
					mbGprsStatus = mUtils.GPRSGetStatus();
					// 开始计数
					mbScreenLockByWidgets = true;
				}
	
			} else if (intent.getAction().equals(
					"android.intent.action.SCREEN_ON")) {
				mUtils.Logx("SCREEN ON");
				mbScreenLockByWidgets = false;
				miTick = 0;

				// 获取锁屏后 是否对网络进行了操控
				if (mbWifiStatusChange || mbGprsStatusChange) {
					mbScreenWake = true;
				}
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
						miTick += 1;
						mUtils.Logx("TICK:" + miTick);
						mUtils.LogF(mUtils.GetTimeNow() + "TICK:" + miTick
								+ "\n");
						if (miTick == tick_dst) {
							if (mUtils.GPRSGetStatus()) {
								mUtils.GPRSOptService(false);
								mbGprsStatusChange = true;
							}
							// mUtils.WifiOptService(false);
							// mbScreenLockByWidgets = false;
						} else if (miTick > tick_dst) {
							if (!mUtils.GPRSGetStatus()) {
								mUtils.WifiOptService(false);
								mbScreenLockByWidgets = false;
								miTick = 0;
								mbWifiStatusChange = true;
							}
						}
					} else if (mbScreenWake) {
						mUtils.Logx("TICK:" + miTick);
						if (miTick++ >= 5) {
							if (mbWifiStatusChange)
								mUtils.WifiOptService(mbWifiStatus);
							if (mbGprsStatusChange)
								mUtils.WifiOptService(mbGprsStatus);
							mbScreenWake = false;
							mbWifiStatusChange = mbGprsStatusChange = false;
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
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		intentFilter.addAction(Intent.ACTION_SCREEN_ON);
		getBaseContext().registerReceiver(mIntentReceiver, intentFilter);
		
		mUtils.Init(getApplicationContext());
		
		// 状态栏
		CreateNotication();

		// locale status
		mbScreenOpen = false;
		mbScreenLockByWidgets = false;
		
		mTimeoutTask = new TimeoutTask();
		mTimeoutTask.execute();
	}
	
	private void CreateNotication()
	{
		NotificationManager notificationManager = mUtils.GetNotificationManager();
		
		//定义Notification的各种属性
		int icon = R.drawable.lock; //通知图标
		CharSequence tickerText = "JUST LOCK"; //状态栏显示的通知文本提示
		long when = System.currentTimeMillis(); //通知产生的时间，会在通知信息里显示
		Notification notification = new Notification(icon,tickerText,when);
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.flags |= Notification.FLAG_NO_CLEAR;
		
		Intent intent= new Intent("com.gss.justlockscreen.widget.click");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,  
                intent, 0);  
        
        Context context = getApplicationContext(); //上下文
        CharSequence contentTitle = "Just Lock Screen!"; //通知栏标题
        CharSequence contentText = "Click me to Close Screen!"; //通知栏内容
        
        notification.setLatestEventInfo(context, contentTitle, contentText, pendingIntent);	
        notificationManager.notify(0,notification);
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
}
