package com.yiqu.wallpaper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HerosAdapter extends BaseAdapter {
	
	public static final int LOCAL_DATA  = 1;
	public static final int ONLINE_DATA = 2;
	
	private Context mContext;
	private HeroData mHeroData;
	private List<Hero> mHeros;
	private ArrayList<Bitmap> Imgs;
	private Bitmap watermark;
	
	public HerosAdapter(Context c, int data){
		mContext = c;
		mHeroData = new HeroData(mContext);
		if(data == HerosAdapter.LOCAL_DATA){
			mHeros = mHeroData.getLocalHeros();
		}else if(data == HerosAdapter.ONLINE_DATA){
			mHeros = mHeroData.getOnlineHeros();
		}else{
			mHeros = mHeroData.getData();
		}
		Collections.sort(mHeros,new Comparator<Hero>(){
            public int compare(Hero l, Hero r) {
                return r.getId() - l.getId();
            }
        });
		LoadImages();
	}
	
	private void LoadImages(){
		Imgs = new ArrayList<Bitmap>();
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		watermark = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.selected);

		int size = mHeros.size();
		for(int i=0;i<size;i++){
			Bitmap tmp = null;
			Hero hero = mHeros.get(i);
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
		return mHeros.size();
	}

	@Override
	public Object getItem(int position) {
		return mHeros.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mHeros.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Hero mHero = mHeros.get(position);
		Bitmap mBitmap = Imgs.get(position);
		if(!mHero.dataExists()){
			//mBitmap = MyImageHandle.convertToBlackWhite(mBitmap);
			mBitmap = MyImageHandle.drawTextToBitmap(mBitmap, "点击下载动画包");
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
		if(!mHero.isActive()){
			item.HeroName.setText(mHero.getName()+" "+mHero.getPoints()+"$");
		}else{
			item.HeroName.setText(mHero.getName());
		}
		return convertView;
	}

}
