package com.gss.justlockscreen;

import android.os.Bundle;
import android.app.Activity;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private utils mUtils = utils.GetInstance();
	private TextView mTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mUtils.Init(getApplicationContext());
		
		//if (!mUtils.IsHaveDevicePolicyPermission())
		//	mUtils.TryHaveDevicePolicyPermission(this);
		
		mTextView = (TextView)findViewById(R.id.textview1);
		mTextView.append("\n");
	//	if (!mUtils.WIfiGetStatus())
	//		mUtils.WifiOptService(true);
		
	//	if (!mUtils.GPRSGetStatus()) {
	//		mTextView.append("GPRS OFF");
	//		mUtils.GPRSOptService(true);
	//	}
		
		Intent intent = new Intent(this, SettingActivity.class);
		startActivityForResult(intent, 0);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
