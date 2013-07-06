package com.gss.justlockscreen;

import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.MutableContextWrapper;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class widgetProvider extends AppWidgetProvider {
	private DevicePolicyManager policyManager;  
	private final String TAG = "widgetProvider";
	private static RemoteViews rv;
	private utils mUtils;
	
	void MLog(String str)
	{
		Log.i(TAG, str);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		super.onReceive(context, intent);
		if(intent.getAction().equals("com.gss.justlockscreen.widget.click")) {
		}
		MLog(intent.getAction());
	}


	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		
		mUtils = utils.GetInstance();
		
		rv = new RemoteViews(context.getPackageName(), R.layout.main);
		
		Intent intentService = new Intent(context, LockScreenService.class);
        context.startService(intentService);
        
		Intent intent= new Intent("com.gss.justlockscreen.widget.click");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,  
                intent, 0);  
        rv.setOnClickPendingIntent(R.id.imageView1, pendingIntent);

		appWidgetManager.updateAppWidget(appWidgetIds, rv);
	}
}
