package com.gss.justlockscreen;

import android.app.Activity;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.ImageView;

class TVOffAnimation extends Animation {  
  
    private int halfWidth;  
    private int halfHeight;  
  
    @Override  
    public void initialize(int width, int height, int parentWidth,  
            int parentHeight) {  
  
        super.initialize(width, height, parentWidth, parentHeight);  
        setDuration(500);  
        setFillAfter(true);  
        //保存View的中心点  
        halfWidth = width / 2;  
        halfHeight = height / 2;  
        setInterpolator(new AccelerateDecelerateInterpolator());  
          
    }  
  
    @Override  
    protected void applyTransformation(float interpolatedTime, Transformation t) {  
  
        final Matrix matrix = t.getMatrix();  
        if (interpolatedTime < 0.8) {  
            matrix.preScale(1+0.625f*interpolatedTime, 1-interpolatedTime/0.8f+0.01f,halfWidth,halfHeight);  
        }else{  
            matrix.preScale(7.5f*(1-interpolatedTime),0.01f,halfWidth,halfHeight);  
        }  
    }  
}  

public class CloseScreenActivity extends Activity {

	private utils mUtils;
	private Activity mActivity;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.donghua);
		mUtils = utils.GetInstance();
		mUtils.Init(getApplicationContext());
		mActivity = this;
		
		ImageView imageView = (ImageView)findViewById(R.id.imageViewTv);
		//Animation animation = AnimationUtils.loadAnimation(this, R.anim.scale);
		Animation animation = new TVOffAnimation();
		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				mUtils.ScreenLockNow();
				try {
					Thread.sleep(1000);
					mActivity.finish();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				
			}
		});
		imageView.startAnimation(animation);
		imageView.setVisibility(View.INVISIBLE);
		
	
	//	mUtils.ScreenLockNow();
		
	}

}
