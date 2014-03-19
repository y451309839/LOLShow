package com.yiqu.wallpaper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;

import com.baidu.mobstat.StatService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class HelloActivity extends Activity {
	
	class HelloThread extends Thread {
		@Override
		public void run() {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(!isSystemInit){
				String dataPath = Environment.getExternalStorageDirectory()+"/Android/data/com.yiqu.wallpaper/LOLShow/";
				File rootFile = new File(dataPath);
				if (!rootFile.exists() && !rootFile.isDirectory()){
					rootFile.mkdirs();
				}
				copyToSdcard("diana.zip",dataPath);
				
				SharedPreferences.Editor editor = SP.edit();
				editor.putInt("selectedHero", 0);
				editor.putBoolean("dianaisDownload", true);
				editor.putBoolean("isSystemInit", true);
				editor.commit();
			}
			StartMain();
		}
	}
	private SharedPreferences SP;
	private Boolean isSystemInit = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.hello_layout);
		SP = getSharedPreferences(LiveWallpaper.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		isSystemInit = SP.getBoolean("isSystemInit", false);
		if(!isSystemInit){
			DisplayToast("首次启动初始化中......");
		}
		new HelloThread().start();
	}
	
	private void StartMain(){
		Intent main=new Intent();
		main.setClass(HelloActivity.this, MainActivity.class);
		startActivity(main);
		this.finish();
	}
	
	/* 
	 * 将raw里的文件copy到sd卡下 
	 * */ 
	private void copyToSdcard(String name, String path) {
		try {
			BufferedOutputStream bufEcrivain = new BufferedOutputStream(
					(new FileOutputStream(new File(path + name))));
			BufferedInputStream VideoReader = new BufferedInputStream(
					getResources().getAssets().open(name));
			byte[] buff = new byte[20 * 1024];
			int len;
			while ((len = VideoReader.read(buff)) > 0) {
				bufEcrivain.write(buff, 0, len);
			}
			bufEcrivain.flush();
			bufEcrivain.close();
			VideoReader.close();
		} catch (Exception e) {

		}
	}
	
	public void DisplayToast(String str){
		LayoutInflater inflater = getLayoutInflater();  
		View layout = inflater.inflate(R.layout.toast_layout,null); 
		TextView tv = (TextView) layout.findViewById(R.id.ToastText);  
		tv.setText(str);
		Toast toast = new Toast(this);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();
	}
	
	@Override
    protected void onDestroy() {
		super.onDestroy();
	}
	@Override
	protected void onResume(){
    	super.onResume();
    	StatService.onResume(this);//From Baidu
    }
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPause(this);//From Baidu
	}

}