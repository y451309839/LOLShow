package com.yiqu.wallpaper;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mobstat.StatService;

import net.youmi.android.AdManager;
import net.youmi.android.offers.OffersManager;
import net.youmi.android.offers.PointsChangeNotify;
import net.youmi.android.offers.PointsManager;
import net.youmi.android.onlineconfig.OnlineConfigCallBack;
import net.youmi.android.update.AppUpdateInfo;
import net.youmi.android.update.CheckAppUpdateCallBack;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity
	implements PointsChangeNotify, CheckAppUpdateCallBack {

	public static final int PAGER_INDEX_LOC_HERO = 0;
	public static final int PAGER_INDEX_OL_HERO = 1;
	public static final int PAGER_INDEX_HELP = 2;
	private String yourJifen;
	private ViewPager mPager;// 页卡内容
	private List<View> listViews; // Tab页面列表
	private ImageView cursor;// 动画图片
	private TextView t1, t2, t3;// 页卡头标
	private TextView mMyPoint;
	private int offset = 0;// 动画图片偏移量
	private int currIndex = 0;// 当前页卡编号
	private int bmpW;// 动画图片宽度
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		InitViewPager();
		InitTextView();
		
		//有米广告初始化代码
		AdManager.getInstance(this).init("fc986eb1a9390900","e1e8f56adb4446c0", false);
		AdManager.getInstance(this).setEnableDebugLog(false);//关闭有米广告SDK相关的log
		OffersManager.getInstance(this).onAppLaunch();
		PointsManager.getInstance(this).registerNotify(this);//调用registerNotify来注册监听器，否则将得不到积分账户余额变动的通知。
		
		//调用检查更新接口，异步。
		//AdManager.getInstance(MainActivity.this).asyncCheckAppUpdate(MainActivity.this);
	}
	
	private void InitViewPager(){
		listViews = new ArrayList<View>();
		mPager = (ViewPager) findViewById(R.id.vPager);
		LayoutInflater mInflater = getLayoutInflater();
		listViews.add(mInflater.inflate(R.layout.heros, null));
		listViews.add(mInflater.inflate(R.layout.heros, null));
		listViews.add(mInflater.inflate(R.layout.help, null));
		mPager.setAdapter(new MyPagerAdapter(listViews, this));
		mPager.setCurrentItem(PAGER_INDEX_LOC_HERO);
		mPager.setOnPageChangeListener(new MyOnPageChangeListener());
	}
	
	private void InitTextView(){
		int myPointBalance = PointsManager.getInstance(this).queryPoints();
		yourJifen = getString(R.string.yourJifen);
		
		t1 = (TextView) findViewById(R.id.t1);
		t2 = (TextView) findViewById(R.id.t2);
		t3 = (TextView) findViewById(R.id.t3);
		mMyPoint = (TextView) findViewById(R.id.MyPoint);
		getOpenCretid("OpenCretid");
		
		t1.setOnClickListener(new MyOnClickListener(PAGER_INDEX_LOC_HERO));
		t2.setOnClickListener(new MyOnClickListener(PAGER_INDEX_OL_HERO));
		t3.setOnClickListener(new MyOnClickListener(PAGER_INDEX_HELP));
		mMyPoint.setText(yourJifen + myPointBalance);
	}
	
	//从服务器获取在线设置
	private void getOpenCretid(String myKey){
		AdManager.getInstance(this).asyncGetOnlineConfig(myKey, new OnlineConfigCallBack() {
	        @Override
	        public void onGetOnlineConfigSuccessful(String key, String value) {
	            //获取在线参数成功
	        	if(key.equals("OpenCretid") && !value.equals("true")){
	        		mMyPoint.setVisibility(View.GONE);
	        	}
	        }       
	        @Override
	        public void onGetOnlineConfigFailed(String key) {
	            //获取在线参数失败，可能原因有：键值未设置或为空、网络异常、服务器异常
	        	if(key.equals("OpenCretid")){
	        		mMyPoint.setVisibility(View.VISIBLE);
	        	}
	        }
	    });
	}
	
	/**
	 * 头标点击监听
	 */
	public class MyOnClickListener implements View.OnClickListener {
		
		private int index = 0;
		public MyOnClickListener(int i) {
			index = i;
		}
		public void onClick(View v) {
			mPager.setCurrentItem(index);
		}
	};
	
	/**
	 * 页卡切换监听
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {

		public void onPageSelected(int index) {
			if(index==PAGER_INDEX_LOC_HERO){
				t1.setBackgroundResource(R.drawable.bar_select_bg);
				t2.setBackgroundResource(0);
				t3.setBackgroundResource(0);
			}
			else if(index==PAGER_INDEX_OL_HERO){
				t1.setBackgroundResource(0);
				t2.setBackgroundResource(R.drawable.bar_select_bg);
				t3.setBackgroundResource(0);
			}else if(index==PAGER_INDEX_HELP){
				t1.setBackgroundResource(0);
				t2.setBackgroundResource(0);
				t3.setBackgroundResource(R.drawable.bar_select_bg);
			}
		}

		 
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		 
		public void onPageScrollStateChanged(int arg0) {
		}
	}
	
	//重写以返回主界面
	@Override
	public void onBackPressed(){
		super.onBackPressed();//finish();
	}

    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	//注意:请务必在onDestroy中调用unRegisterNotify来注销积分监听
        PointsManager.getInstance(this).unRegisterNotify(this);
    	//有米广告销毁代码
    	OffersManager.getInstance(this).onAppExit();
    }
    
    @Override
    protected void onResume(){
    	super.onResume();
    	StatService.onResume(this);//From Baidu
    }
    
    @Override
    public void onPause() {
		super.onPause();
		StatService.onPause(this);//From Baidu
	}

    @Override
    public void onPointBalanceChange(int pointsBalance) {
        //当该方法被调用时，表示积分账户余额已经变动了，这里的pointsBalance是积分的当前余额数
    	mMyPoint.setText(yourJifen + pointsBalance);
        //注:您可以在这里进行更新界面显示等操作，
    }
	
	public void DisplayToast(String str){
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 检查应用更新的回调方法
	 */
	@Override
	public void onCheckAppUpdateFinish(AppUpdateInfo updateInfo) {
		//检查更新回调，注意，这里是在UI线程回调的，因此您可以直接与UI交互，但不可以进行长时间的操作(如在这里访问网络  是不允许的)

    	if(updateInfo != null && updateInfo.getUrl() != null){	//有更新信息
        	final String url = updateInfo.getUrl();
        	new AlertDialog.Builder(this)
            .setTitle("发现新版本")
            .setMessage(updateInfo.getUpdateTips())//这里是版本更新信息
            .setPositiveButton("马上升级",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                int which) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url)); 
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
                            startActivity(intent);
                            //这里示例点击"马上升级"按钮之后简单地调用系统浏览器进行新版本的下载，
                            //但强烈建议开发者实现自己的下载管理流程，这样可以获得更好的用户体验。
                        }
                    })
            .setNegativeButton("下次再说",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                int which) { 
                            dialog.cancel();
                        }
                    })
            .create().show(); 
        }else{
        	DisplayToast("已经是最新版本！");
        }
	}
	
	public static Boolean sdCardMounted(){
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			return true;
		}
		return false;
	}
    
}
