package com.yiqu.wallpaper;

import java.io.File;
import java.util.List;

import com.baidu.mobstat.StatService;

import net.youmi.android.AdManager;
import net.youmi.android.offers.OffersManager;
import net.youmi.android.onlineconfig.OnlineConfigCallBack;
import net.youmi.android.update.CheckAppUpdateCallBack;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MyPagerAdapter extends PagerAdapter implements SharedPreferences.OnSharedPreferenceChangeListener {
	
	private static final String[] mDrawLevelData={"低质","普通","高质"};
	private static final String[] mDrawLevelVar={"low","middle","high"};
	public Context mContext;
	public List<View> mListViews;
	public int mCount;
	private ListView mList;
	private Button mGetJifen,mOpenWallpaperChooser,mCheckNew;
	private SeekBar mDrawLevel;
	private TextView mDrawLevelText,mTvOnline;
	private SharedPreferences SP;
	private String LevelStr;
	private String LevelVar;
	private GridView gridview;
	private HeroData mHeroData;
	private HerosAdapter mLocalAdapter;
	private HerosAdapter mOnlineAdapter;
	private YQAlertDialog Dlg;
	private Handler DlHandler;
	private PackageDownload downZipPkg;

	public MyPagerAdapter(List<View> mListViews, Context c) {
		mContext = c;
		this.mListViews = mListViews;
		mCount = mListViews.size();
		SP = mContext.getSharedPreferences(LiveWallpaper.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		SP.registerOnSharedPreferenceChangeListener(this);
		mHeroData = new HeroData(mContext);
		mLocalAdapter = new HerosAdapter(mContext, HerosAdapter.LOCAL_DATA);
		mOnlineAdapter = new HerosAdapter(mContext, HerosAdapter.ONLINE_DATA);
	}
	
	public void destroyItem(View collection, int position, Object arg2) {
		((ViewPager) collection).removeView(mListViews.get(position));
	}
	
	public void finishUpdate(View arg0) {
	}
	
	@Override
	public int getCount() {
		return mCount;
	}
	
	public Object instantiateItem(View collection, int position) {
		((ViewPager) collection).addView(mListViews.get(position), 0);
		if(position==MainActivity.PAGER_INDEX_LOC_HERO){
			gridview = (GridView) collection.findViewById(R.id.gridView1); 
			gridview.setAdapter(mLocalAdapter);
			gridview.setOnItemClickListener(new GridItemClick(mLocalAdapter));
			gridview.setOnItemLongClickListener(new GridItemLongClick(mLocalAdapter));
		}
		else if(position==MainActivity.PAGER_INDEX_OL_HERO){
			gridview = (GridView) collection.findViewById(R.id.gridView1); 
			gridview.setAdapter(mOnlineAdapter);
			gridview.setOnItemClickListener(new GridItemClick(mOnlineAdapter));
			gridview.setOnItemLongClickListener(new GridItemLongClick(mOnlineAdapter));
		}
		else if(position==MainActivity.PAGER_INDEX_HELP){
			InitBtnEvent(mListViews.get(position));
		}
		return mListViews.get(position);
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == (arg1);
	}
	
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
	}
	 
	public Parcelable saveState() {
		return null;
	}
	 
	public void startUpdate(View arg0) {
	}
	
	private void InitBtnEvent(View v){
		mOpenWallpaperChooser = (Button) v.findViewById(R.id.OpenWallpaperChooser);
		mGetJifen = (Button) v.findViewById(R.id.getJifen);
		mCheckNew = (Button) v.findViewById(R.id.checkNew);
		mDrawLevel = (SeekBar) v.findViewById(R.id.DrawLevelSeekBar);
		mDrawLevelText = (TextView) v.findViewById(R.id.DrawLevelText);
		mTvOnline = (TextView) v.findViewById(R.id.tvOnline);
		
		//获取顶部最新通知
		AdManager.getInstance(mContext).asyncGetOnlineConfig("tvOnline",
				new OnlineConfigCallBack() {
					@Override
					public void onGetOnlineConfigSuccessful(String key, String value) {
						// 获取在线参数成功
						if (key.equals("tvOnline") && !value.equals("")) {
							mTvOnline.setText(value);
							LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);  
							lp.setMargins(0, 0, 0, 10);
							mTvOnline.setLayoutParams(lp);
						}
					}

					@Override
					public void onGetOnlineConfigFailed(String key) {
						// 获取在线参数失败，可能原因有：键值未设置或为空、网络异常、服务器异常
					}
				});
		
		mOpenWallpaperChooser.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Toast.makeText(mContext, "请选择“英雄联盟秀”", Toast.LENGTH_SHORT).show();
					Intent in = new Intent();
					in.setAction(WallpaperManager.getInstance(mContext).ACTION_LIVE_WALLPAPER_CHOOSER);  
					mContext.startActivity(in);	//打开动态壁纸选择器
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(v.getContext(), "动态壁纸选择器打开错误，可能是您的设备不支持，您可以手动尝试。", Toast.LENGTH_SHORT).show();
				}
			}
		});
		mGetJifen.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//OffersManager.getInstance(mContext).showOffersWallDialog((Activity) mContext); //有米广告Dialog
				OffersManager.getInstance(mContext).showOffersWall();
			}
		});
		mCheckNew.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AdManager.getInstance(mContext).asyncCheckAppUpdate((CheckAppUpdateCallBack) mContext);//调用检查更新接口，异步。
			}
		});
		mDrawLevel.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				LevelStr = mDrawLevelData[progress];
				LevelVar = mDrawLevelVar[progress];
				mDrawLevelText.setText(LevelStr);
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				SharedPreferences.Editor editor = SP.edit();
				editor.putString("drawLevel", LevelVar);
				editor.commit();
			}
		});
		LevelVar = SP.getString("drawLevel", "middle");
		for(int i=0;i<mDrawLevelVar.length;i++){
			if(mDrawLevelVar[i].equals(LevelVar)){
				LevelStr = mDrawLevelData[i];
				mDrawLevelText.setText(LevelStr);
				mDrawLevel.setProgress(i);
				break;
			}
		}
	}
	
	class GridItemClick implements OnItemClickListener {
		
		private HerosAdapter a;
		
		public GridItemClick(HerosAdapter A){
			this.a = A;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View v, int id, long i) {
			final Hero mHero = (Hero) this.a.getItem(id);
			
			if(mHero.dataExists()){
				mHero.selectHero();
			}else{
				Dlg = new YQAlertDialog(mContext,
						"["+mHero.getName()+"]\n需要先下载动画包才能使壁纸有动态效果，动画包大小："+mHero.getSize()+"MB。",
						"开始下载");
				DlHandler = new Handler(){
					public void handleMessage(Message msg) {
						super.handleMessage(msg);
						if(msg!=null){
							String str = msg.obj.toString();
							switch (msg.what) {
							case 0:
								if(!str.equals(""))DisplayToast(str);
								break;
							case 1:
								//取消下载后清除所有下载信息
								if(!str.equals(""))DisplayToast(str);
								break;
							case 2:
								//数据包下载完成
								if(!str.equals(""))DisplayToast(str);
								Dlg.closeDlg();
								SharedPreferences.Editor editor = SP.edit();
			    				editor.putBoolean(mHero.getFile() + "isDownload", true);
			    				editor.commit();
			    				break;
							case 3:
								//下载进度
								int progress = Integer.parseInt(str);
								Dlg.setBarProgress(progress);
								break;
							case 4:
								//下载失败
								if(!str.equals(""))DisplayToast(str);
								Dlg.closeDlg();
								break;
							}
						}
					}
				};
				downZipPkg = new PackageDownload(mContext,mHero,DlHandler);
				Dlg.setBtnOnClick(new OnClickListener(){
					@Override
					public void onClick(View v) {
						StatService.onEvent(mContext, "downPackage", mHero.getName());//百度统计
						Dlg.setProgressBarVisibility(View.VISIBLE);
						Dlg.setBtnVisibility(View.GONE);
						downZipPkg.start();
					}
				});
				Dlg.setDlgDismissListener(new OnDismissListener(){
					@Override
					public void onDismiss(DialogInterface dialog) {
						if(downZipPkg!=null){
							downZipPkg.cancelUpdate = true;
						}
					}
				});
			}
		}
	}
	
	class GridItemLongClick implements OnItemLongClickListener {
		
		private HerosAdapter a;
		
		public GridItemLongClick(HerosAdapter A){
			this.a = A;
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View v, int id, long i) {
			final Hero mHero = (Hero) this.a.getItem(id);
			if(mHero.dataExists()){
				if(mHero.isSelect()){
					Dlg = new YQAlertDialog(mContext,
							"["+mHero.getName()+"]\n这个壁纸正在使用中，不能删除该动画包。");
					Dlg.setBtnOnClick(new OnClickListener(){
						@Override
						public void onClick(View v) {
							Dlg.dlg.dismiss();
						}
					});
				}else{
					Dlg = new YQAlertDialog(mContext,
							"["+mHero.getName()+"]\n要删除这个壁纸的动画包吗？大小："+mHero.getSize()+"MB。",
							"确认删除");
					Dlg.setBtnOnClick(new OnClickListener(){
						@Override
						public void onClick(View v) {
							File mFile = new File(mHero.getFilePath());
							if (mFile.exists()){
								mFile.delete();
								SharedPreferences.Editor editor = SP.edit();
			    				editor.putBoolean(mHero.getFile() + "isDownload", false);
			    				editor.commit();
			    				Dlg.dlg.dismiss();
							}
						}
					});
				}
			}
			return true;//返回TRUE，防止执行OnItemClick
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

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key!=null){
			if(mLocalAdapter!=null) mLocalAdapter.notifyDataSetChanged();
			if(mOnlineAdapter!=null) mOnlineAdapter.notifyDataSetChanged();
		}
	}

}
