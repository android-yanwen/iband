package com.manridy.iband.view.main;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.manridy.iband.R;
import com.manridy.iband.view.base.BaseActionActivity;

public class CountDownActivity extends BaseActionActivity {

	private TextView mTextView;
	private Animation mAnimation;
	private int count = 4;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0) {
				
				if(count>1)
				{
					count--;
					mTextView.setText("" + count);
					handler.sendEmptyMessageDelayed(0, 1000);
					addMyAnimation();
				}else if(count==1){
					count--;
					mTextView.setText("GO!");
					handler.sendEmptyMessageDelayed(0, 1000);
					addMyAnimation();
				}else{
					mTextView.setText("");
					Intent intent;
					int objectActivity = getIntent().getIntExtra("objectActivity",0);
					switch (objectActivity){
						case 1:
							intent = new Intent(CountDownActivity.this,RunActivity.class);
							startActivity(intent);
							break;
						case 2:
							intent = new Intent(CountDownActivity.this,IndoorRunActivity.class);
							startActivity(intent);
							break;
						case 3:
							intent = new Intent(CountDownActivity.this,BikingActivity.class);
							startActivity(intent);
							break;
					}


					finish();
				}
				
			}
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.count_down_main);
		mAnimation = AnimationUtils.loadAnimation(this, R.anim.count_down_exit);
		
		initView();
	}

	@Override
	protected void initView(Bundle savedInstanceState) {

	}

	@Override
	protected void initVariables() {

	}

	@Override
	protected void initListener() {

	}
@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {
	if (keyCode == KeyEvent.KEYCODE_BACK) {
		return true;
	}
	return super.onKeyDown(keyCode, event);
}
	private void initView() {
		mTextView = (TextView) findViewById(R.id.text);
		mTextView.startAnimation(mAnimation);
		handler.sendEmptyMessageDelayed(0, 1000);
	}

	public void addMyAnimation() {
		mAnimation.reset();
		mTextView.startAnimation(mAnimation);
	}
	
	private void toast(String string) {
		Toast.makeText(CountDownActivity.this,string, Toast.LENGTH_SHORT).show();
	};

	@Override
	public void scrollToFinishActivity() {
	}

	@Override
	protected void initBack() {
	}

}
