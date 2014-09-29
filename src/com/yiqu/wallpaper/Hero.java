package com.yiqu.wallpaper;

import java.io.IOException;
import java.util.zip.ZipFile;

import net.youmi.android.AdManager;
import net.youmi.android.offers.OffersManager;
import net.youmi.android.offers.PointsManager;
import net.youmi.android.onlineconfig.OnlineConfigCallBack;

import com.baidu.mobstat.StatService;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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
	
	public void selectHero() {
		StatService.onEvent(mContext, "selectWallpaper", getName());// 百度统计
		AdManager.getInstance(mContext).asyncGetOnlineConfig("OpenCretid",
				new OnlineConfigCallBack() {
					@Override
					public void onGetOnlineConfigSuccessful(String key,
							String value) {
						// 获取在线参数成功
						if (key.equals("OpenCretid")) {
							setSelect(value);
						}
					}

					@Override
					public void onGetOnlineConfigFailed(String key) {
						// 获取在线参数失败，可能原因有：键值未设置或为空、网络异常、服务器异常
						setSelect("true");
					}
				});
	}
	
	private void setSelect(String OpenCretid){
		int myPointBalance = PointsManager.getInstance(mContext).queryPoints();
		//if(OpenCretid.equals("true") && mLiveJiFen > 0 && !SP.getBoolean(mLiveName + "isActive", false)){
		if(OpenCretid.equals("true") && this.getPoints() > myPointBalance){
			Dlg = new YQAlertDialog(mContext,
					"你选择了["+this.getName()+"]，需要达到 "+this.getPoints()+" 积分才能使用，当前积分不足！",
					"获取积分");
			Dlg.setBtnOnClick(new OnClickListener(){
				@Override
				public void onClick(View v) {
					OffersManager.getInstance(mContext).showOffersWall();
					Dlg.closeDlg();
				}
			});
			SharedPreferences.Editor editor = SP.edit();
			editor.remove(this.getFile() + "isActive");
			editor.commit();
		}else{
			SharedPreferences.Editor editor = SP.edit();
			editor.putBoolean(this.getFile() + "isActive", true);
			editor.putInt("selectedHero", this.getId());
			if(editor.commit()){
				DisplayToast("选择"+this.getName()+"成功，谢谢您的支持！");
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