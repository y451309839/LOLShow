package com.yiqu.wallpaper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;

public class LiveWallpaper extends WallpaperService {

	public static final boolean APP_DEBUG = false;
	public static final String SHARED_PREFS_NAME = "com.yiqu.wallpaper";

	private final Handler handler = new Handler();

	@Override
	public Engine onCreateEngine() {
		return new QyWallpaperEngine(getResources());
	}

	class QyWallpaperEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {
		
		SharedPreferences prefs;
		DisplayMetrics dm;
		Bitmap mframeBitmap = null;
		int selectedHero; //选择壁纸
		HeroData mHeroData;
		Hero mHero;
		String drawLevel; //绘图质量
		ZipFile zipFile;
		ArrayList<ZipEntry> pics = null;
		
		Rect src;// 图片
        Rect dst;// 屏幕位置及尺寸
        int imgWidth, imgHeight;
        
        private boolean canDraw = true;
		private int mFrameCount = 0;
		private int mPlayID = 0;
		private int mFrameTime = 30; //每帧显示时间
		private int mWidth,mHeight;
		private int virtualWidth,virtualHeight;

		private final Runnable drawRequest = new Runnable() {
			@Override
			public void run() {
				drawDroid();
			}
		};

		private Runnable checkResMount = new Runnable() {
			@Override
			public void run() {
				if(mHero.dataExists()){
					LoadZipData();
				}
			}
		};
		private int textLeft;
		private int textTop;
		private FontMetrics fm;
		private final String text = "没有发现动画数据包！";
		private Paint paintText;
		private Paint paintImage;
		
		public QyWallpaperEngine(Resources r) {
			 prefs = LiveWallpaper.this.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
	         prefs.registerOnSharedPreferenceChangeListener(this);
	         onSharedPreferenceChanged(prefs, null);
	         selectedHero = prefs.getInt("selectedHero", 0);
	         drawLevel = prefs.getString("drawLevel", "middle");
	         mHeroData = new HeroData(getBaseContext());
		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			setTouchEventsEnabled(false);
			
			WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		    dm=new DisplayMetrics();
		    wm.getDefaultDisplay().getMetrics(dm);
		 	mWidth=dm.widthPixels;
		 	mHeight=dm.heightPixels;
		 	
			virtualWidth = getDesiredMinimumWidth();
			virtualHeight = getDesiredMinimumHeight();
			
			if(virtualWidth<mWidth)virtualWidth = mWidth;
			if(virtualHeight<mHeight)virtualHeight = mHeight;
			loadDrawRes();
			handler.post(checkResMount);
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			handler.removeCallbacks(drawRequest);
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			canDraw = visible;
			if (visible) {
				drawDroid(); // 自己的绘屏函数
			} else {
				handler.removeCallbacks(drawRequest); // 不可见时，移除回调
			}
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			canDraw = false;
			handler.removeCallbacks(drawRequest);
		}

		@Override
		public void onSurfaceCreated(SurfaceHolder holder) {
			super.onSurfaceCreated(holder);
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			super.onSurfaceChanged(holder, format, width, height);
			mWidth = width;
			mHeight = height;
			initSize();
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep,
				float yOffsetStep, int xPixelOffset, int yPixelOffset) {
			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep,
					xPixelOffset, yPixelOffset);
			if(xPixelOffset != 0){
				dst.left = xPixelOffset;
				dst.right = virtualWidth+xPixelOffset;
			}
		}
		
		private void initSize(){
			src = new Rect();// 图片
	        dst = new Rect();// 屏幕
	        
	        //src 这个是表示截取图片的位置
	        src.left = 0;
	        src.top = 0;
	        src.right = imgWidth;
	        src.bottom = imgHeight;
	        
	        // dst 是表示屏幕绘画的位置
	        dst.left = 0;    //绘图的起点X位置
	        dst.top = 0;    // 绘画起点的Y坐标
	        dst.right = virtualWidth;    // 表示需绘画的图片的右上角
	        dst.bottom = mHeight;    // 表示需绘画的图片的右下角

	        paintImage = new Paint(Paint.FILTER_BITMAP_FLAG);
			paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
            paintText.setColor(Color.WHITE);
            paintText.setTextSize(18);
            int textLen = (int) paintText.measureText(text);
	        fm = paintText.getFontMetrics();
	        textLeft = (mWidth - textLen ) / 2;
	        textTop = (int) (mHeight - Math.abs(fm.ascent));
		}
		
		private void loadDrawRes(){
			mHero = mHeroData.getHero(selectedHero);
			mFrameCount = mHero.getCount();
			mPlayID = 0;
			mframeBitmap = readAssetBitmap("yuan/" + mHero.getFile()+".png");
	
			imgWidth = mframeBitmap.getWidth();
			imgHeight = mframeBitmap.getHeight();
			initSize();
		}
		
		public void LoadZipData(){
			zipFile = mHero.getZipFile();
			if(zipFile==null){
				pics=null;
				return;
			}
			Enumeration<ZipEntry> e = (Enumeration<ZipEntry>) zipFile.entries(); // 获取zip文件中的目录及文件列表
			ZipEntry entry = null;
			pics = new ArrayList<ZipEntry>();
			while (e.hasMoreElements()) {
			    entry = e.nextElement();
			    String fileName = entry.getName();
			    if (!entry.isDirectory()) {
			        //如果文件不是目录，则添加到列表中
			        pics.add(entry);
			    }
			}
		}

		private void drawDroid() {
			final SurfaceHolder holder = getSurfaceHolder();
			Canvas canvas = null;
			try {
				canvas = holder.lockCanvas();
				if (canvas != null) {
					if(pics!=null && pics.size()==mFrameCount){
						Bitmap mBitmap = getZipBitmap(mPlayID);
						if(mBitmap != null){
							canvas.drawBitmap(mBitmap, src, dst, paintImage);
							mPlayID++;
							if (mPlayID >= mFrameCount) {
								mPlayID = 0;
							}
						}else{
							pics = null;
						}
					}else{
						canvas.drawBitmap(mframeBitmap, src, dst, paintImage);
		                //canvas.drawText(text, textLeft, textTop, paintText);
						handler.post(checkResMount);
					}
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}
			handler.removeCallbacks(drawRequest);
			if (canDraw) handler.postDelayed(drawRequest,mFrameTime);
		}
		
		private Bitmap getZipBitmap(int pictureid) {
	        BitmapFactory.Options opt = new BitmapFactory.Options();
			if(drawLevel.equals("high")){
				opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
			}else if(drawLevel.equals("middle")){
				opt.inPreferredConfig = Bitmap.Config.RGB_565;
			}else if(drawLevel.equals("low")){
				opt.inSampleSize = 2;
				opt.inPreferredConfig = Bitmap.Config.RGB_565;
			}
			opt.inPurgeable = true;
			opt.inInputShareable = true;
			
			Bitmap image;
	        try {
	            InputStream is = zipFile.getInputStream(pics.get(pictureid));
				image = BitmapFactory.decodeStream(is, null ,opt);
	        } catch (IOException e) {
	            //e.printStackTrace();
	            return null;
	        }
	        return image;
	    }
		
		public Bitmap readAssetBitmap(String fileName){
			BitmapFactory.Options opt = new BitmapFactory.Options();
			if(drawLevel.equals("high")){
				opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
			}else if(drawLevel.equals("middle")){
				opt.inPreferredConfig = Bitmap.Config.RGB_565;
			}else if(drawLevel.equals("low")){
				opt.inSampleSize = 2;
				opt.inPreferredConfig = Bitmap.Config.RGB_565;
			}
			opt.inPurgeable = true;
			opt.inInputShareable = true;
			
			Bitmap image = null;
			try {
				InputStream is = getResources().getAssets().open( fileName );
				image = BitmapFactory.decodeStream(is, null ,opt);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return image;
		}
		
		public Bitmap ReadBitMap(int resId) {
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inPreferredConfig = Bitmap.Config.RGB_565;
			opt.inPurgeable = true;
			opt.inInputShareable = true;
			// 获取资源图片
			InputStream is = getResources().openRawResource(resId);
			return BitmapFactory.decodeStream(is, null, opt);
		}
		
		public void recycle(Bitmap bitmap){
			if(bitmap!=null && !bitmap.isRecycled()){
				bitmap.recycle();
				System.gc();//提醒系统及时回收
			}
		}

		@Override
		public void onTouchEvent(MotionEvent event) {
			/* 添加触屏效果 */
			super.onTouchEvent(event);
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if(key!=null){
				if(key.equals("selectedHero")){
					selectedHero = prefs.getInt("selectedHero", 0);
					loadDrawRes();
					handler.post(checkResMount);
					
				}else if(key.equals("drawLevel")){
					drawLevel = prefs.getString("drawLevel", "middle");
					loadDrawRes();
					handler.post(checkResMount);
				}
			}
		}
	}
}
