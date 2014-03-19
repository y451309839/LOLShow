package com.yiqu.wallpaper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import net.youmi.android.AdManager;
import net.youmi.android.dev.OnlineConfigCallBack;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HerosGridViewAdapter extends BaseAdapter {
	
	private Context mContext;
	private HeroData mHeroData;
	private ArrayList<Bitmap> Imgs;
	private Bitmap watermark;
	
	public HerosGridViewAdapter(Context c){
		mContext = c;
		mHeroData = new HeroData(mContext); 
		LoadImages();
	}
	
	private void LoadImages(){
		int size = mHeroData.getCount();
		Imgs = new ArrayList<Bitmap>();
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		watermark = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.selected);
		
		for(int i=0;i<size;i++){
			Bitmap tmp = null;
			Hero hero = mHeroData.getHero(i);
			try {
				InputStream is = mContext.getResources().getAssets().open("min/" + hero.getFile() + ".png" );
				tmp = BitmapFactory.decodeStream(is, null ,opt);
				tmp = MyImageHandle.zoomImg(tmp, 240, 320);
			} catch (IOException e) {
				e.printStackTrace();
			}
			Imgs.add(i, tmp);
		}
	}

	@Override
	public int getCount() {
		return mHeroData.getCount();
	}

	@Override
	public Object getItem(int id) {
		return mHeroData.getHero(id);
	}

	@Override
	public long getItemId(int position) {
		return mHeroData.getHero(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView = null;
		Hero mHero = mHeroData.getHero(position);
		Bitmap mBitmap = Imgs.get(position);
		if(!mHero.dataExists()){
			mBitmap = MyImageHandle.convertToBlackWhite(mBitmap);
		}
		if(mHero.isSelect()){
			mBitmap = MyImageHandle.createWatermarkBitmap(mBitmap, watermark );
		}
		ItemHolder item = null;
		if (convertView == null ) {
			LayoutInflater mInflater = LayoutInflater.from(mContext);
			convertView = mInflater.inflate(R.layout.heros_item, null);
			item  = new ItemHolder();
			item.HeroImage = (ImageView) convertView.findViewById(R.id.ItemImage);
			item.HeroName = (TextView) convertView.findViewById(R.id.ItemName);
			convertView.setTag(item);
		} else {
			item = (ItemHolder) convertView.getTag();
		}
		item.HeroImage.setImageBitmap(mBitmap);
		int point = mHero.getPoints();
		if(point>0 && !mHero.isActive()){
			item.HeroName.setText(mHero.getName()+" "+mHero.getPoints()+"积分");
		}else{
			item.HeroName.setText(mHero.getName());
		}
		return convertView;
	}

}
