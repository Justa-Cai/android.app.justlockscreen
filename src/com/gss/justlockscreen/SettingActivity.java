package com.gss.justlockscreen;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class SettingActivity extends PreferenceActivity implements
		OnPreferenceClickListener,DialogInterface.OnClickListener {

	private Button mBtnActiveDevice;
	private utils mUtils;
	private Activity mActivity;
	private boolean mbActive = false;// �豸����Ȩ��
	
	private EditTextPreference mEditTextPreference;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setting);

		mActivity = this;
		mUtils = utils.GetInstance();
		mUtils.Init(getApplicationContext());

		mBtnActiveDevice = new Button(this);
		if (mUtils.IsHaveDevicePolicyPermission())
			mBtnActiveDevice.setText("ȡ���豸����Ȩ��");
		else
			mBtnActiveDevice.setText("�����豸����Ȩ��");
		setListFooter(mBtnActiveDevice);

		mBtnActiveDevice.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// �����ȡ�� �豸����Ȩ��
				if (mUtils.IsHaveDevicePolicyPermission()) {
					mUtils.TryRemoveDevicePolicyPermission(mActivity);
					mBtnActiveDevice.setText("�����豸����Ȩ��");
				} else {
					mUtils.TryHaveDevicePolicyPermission(mActivity);
				}
			}
		});
		
		// �ؼ�����
		mEditTextPreference = (EditTextPreference)findPreference("about");
		//mEditTextPreference.setOnPreferenceClickListener(this);
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0) {
			mbActive = resultCode == 0 ? false : true;
			if (mbActive)
				mBtnActiveDevice.setText("ȡ���豸����Ȩ��");
		}
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		mUtils.Logx("key:" + preference.getKey());
		return false;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		mUtils.Logx("which:" + which);
	}
}

class AboutDailogPreference extends DialogPreference
{

	public AboutDailogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
}