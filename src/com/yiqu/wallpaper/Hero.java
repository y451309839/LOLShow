package com.yiqu.wallpaper;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import net.youmi.android.AdManager;
import net.youmi.android.dev.OnlineConfigCallBack;
import net.youmi.android.offers.OffersManager;
import net.youmi.android.offers.PointsManager;

import com.baidu.mobstat.StatService;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

class Hero {

	private Context mContext;
	private int id;
	private String name;
	private String file;
	private String url;
	private int count;
	private float size;
	private int points;
	private SharedPreferences SP;
	public String dataPath = Environment.getExternalStorageDirectory()+"/Android/data/com.yiqu.wallpaper/LOLShow/";
	private YQAlertDialog Dlg;
	
	public Hero(Context c){
		mContext = c;
		SP = mContext.getSharedPreferences(LiveWallpaper.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
	}
	
	//是否已购买
	public Boolean isActive(){
		if(this.getPoints() == 0 || (this.getPoints() > 0 && SP.getBoolean(this.getFile() + "isActive", false))){
			return true;
		}
		return false;
	}
	
	public Boolean isSelect(){
		return this.id == SP.getInt("selectedHero", 0);
	}
	
	public void selectHero(){
		StatService.onEvent(mContext, "selectWallpaper", getName());//百度统计
		AdManager.getInstance(mContext).asyncGetOnlineConfig("OpenCretid", new OnlineConfigCallBack() {
		        @Override
		        public void onGetOnlineConfigSuccessful(String key, String value) {
		            //获取在线参数成功
		        	if(key.equals("OpenCretid")){
		        		setSelect(value);
		        	}
		        }       
		        @Override
		        public void onGetOnlineConfigFailed(String key) {
		            //获取在线参数失败，可能原因有：键值未设置或为空、网络异常、服务器异常
		        	setSelect("true");
		        }
		    });
	}
	
	private void setSelect(String OpenCretid){
		final Hero mHero = this;
		final String mLiveName = this.getFile();
		final int mLiveJiFen = this.getPoints();
		if(OpenCretid.equals("true") && mLiveJiFen > 0 && !SP.getBoolean(mLiveName + "isActive", false)){
			Dlg = new YQAlertDialog(mContext,
					"你选择了["+mHero.getName()+"]，需要消费 "+mLiveJiFen+" 积分，购买后可永久使用，确认购买吗？",
					"确认购买");
			Dlg.setBtnOnClick(new OnClickListener(){
				@Override
				public void onClick(View v) {
					mHero.getPoints();
                	if(LiveWallpaper.APP_DEBUG || PointsManager.getInstance(mContext).spendPoints(mLiveJiFen)){
	    				SharedPreferences.Editor editor = SP.edit();
	    				editor.putBoolean(mLiveName + "isActive", true);
	    				editor.putInt("selectedHero", mHero.getId());
	    				if(editor.commit()){
	    					DisplayToast("选择"+mHero.getName()+"成功，已扣除" + mLiveJiFen + "积分！");
	    					StatService.onEvent(mContext, "buyWallpaper", mHero.getName());//百度统计
	    					Dlg.closeDlg();
	    				}
	    			} else {
	    				NowGetJifen.show(mContext);
				    }
				}
			});
		}else{
			SharedPreferences.Editor editor = SP.edit();
			editor.putInt("selectedHero", mHero.getId());
			if(editor.commit()){
				DisplayToast("选择"+mHero.getName()+"成功，谢谢您的支持！");
			}
		}
	}
	
	public void DisplayToast(String str){
		LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();  
		View layout = inflater.inflate(R.layout.toast_layout,null); 
		TextView tv = (TextView) layout.findViewById(R.id.ToastText);  
		tv.setText(str);
		Toast toast = new Toast(mContext);
		//toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();
	}
	
	public Boolean dataExists() {
		return SP.getBoolean(file + "isDownload", false);
	}
	
	public String getFilePath(){
		return dataPath+file+".zip";
	}
	
	public ZipFile getZipFile(){
		ZipFile mZipFile = null;
		try {
			return new ZipFile(getFilePath());
		} catch (IOException e) {
			SharedPreferences.Editor editor = SP.edit();
			editor.putBoolean(file + "isDownload", true);
			editor.commit();
			e.printStackTrace();
		}
		return mZipFile;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public int getId(){
		return id;
	}

	public void setName(String name){
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setFile(String file){
		this.file = file;
	}

	public String getFile() {
		return file;
	}
	
	public void setUrl(String url){
		this.url = url;
	}
	
	public String getUrl(){
		return url;
	}
	
	public void setCount(int count){
		this.count = count;
	}

	public int getCount() {
		return count;
	}
	
	public void setSize(float size){
		this.size = size;
	}

	public float getSize() {
		return size;
	}

	public void setPoints(int points){
		this.points = points;
	}
	
	public int getPoints() {
		return points;
	}

}