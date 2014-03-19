package com.yiqu.wallpaper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import net.youmi.android.offers.OffersManager;
import net.youmi.android.offers.PointsManager;

import org.xmlpull.v1.XmlPullParser;

import com.baidu.mobstat.StatService;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Xml;
import android.widget.Toast;

/**
 * Created by 覃奕 on 13-8-13.
 */
public class HeroData {

	Resources res;
	Float version;
	List<Hero> mHeros;
	private Context mContext;

	public HeroData(Context c) {
		this.mContext = c;
		this.res = mContext.getResources();
		try {
			mHeros = readHeros(res.openRawResource(R.raw.heros));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<Hero> getData() {
		return mHeros;
	}
	
	//取得已经下载好并购买的包
	public List<Hero> getLocalHeros(){
		List<Hero> heros = new LinkedList<Hero>();
		for(int i = 0;i < mHeros.size();i++){
			Hero hero = mHeros.get(i);
			if(hero.dataExists() && hero.isActive()){
				heros.add(hero);
			}
		}
		return heros;
	}
	
	//还没有下载的
	public List<Hero> getOnlineHeros(){
		List<Hero> heros = new LinkedList<Hero>();
		for(int i = 0;i < mHeros.size();i++){
			Hero hero = mHeros.get(i);
			if(!hero.dataExists() || !hero.isActive()){
				heros.add(hero);
			}
		}
		return heros;
	}
	
	public Hero getHero(int id){
		for(int i = 0;i < mHeros.size();i++){
			if(mHeros.get(i).getId() == id){
				return mHeros.get(i);
			}
		}
		return mHeros.get(0);
	}
	
	//是否已购买
	public Boolean isActive(int id){
		return mHeros.get(id).isActive();
	}
	
	//选择英雄操作，修改数据
	public void SelectHero(int id){
		mHeros.get(id).selectHero();
	}
	
	private List<Hero> readHeros(InputStream is) throws Exception {
		List<Hero> heros = null;
		Hero currentHero = null;
		String tagName = null;
		
		XmlPullParser pullParser = Xml.newPullParser();
		pullParser.setInput(is, "UTF-8");
		int event = pullParser.getEventType();
		
		while(event != XmlPullParser.END_DOCUMENT){
			switch(event){
				case XmlPullParser.START_DOCUMENT:
					heros = new ArrayList<Hero>();
					break;
				case XmlPullParser.START_TAG:
					tagName = pullParser.getName();
					if(tagName.equals("hero")){
						int id = Integer.parseInt(pullParser.getAttributeValue(0));
						currentHero = new Hero(mContext);
						currentHero.setId(id);
					}
					if(tagName.equals("name")){
						currentHero.setName(pullParser.nextText());
					}
					if(tagName.equals("file")){
						currentHero.setFile(pullParser.nextText());
					}
					if(tagName.equals("url")){
						currentHero.setUrl(pullParser.nextText());
					}
					if(tagName.equals("count")){
						currentHero.setCount(Integer.parseInt(pullParser.nextText()));
					}
					if(tagName.equals("size")){
						currentHero.setSize(Float.parseFloat(pullParser.nextText()));
					}
					if(tagName.equals("points")){
						currentHero.setPoints(Integer.parseInt(pullParser.nextText()));
					}
					break;
				case XmlPullParser.END_TAG:
					tagName = pullParser.getName();
					if(tagName.equals("hero")){
						heros.add(currentHero);
						currentHero = null;
					}
					break;
			}
			event = pullParser.next();
		}
		return heros;
	}

	public int getCount() {
		return mHeros.size();
	}
	
}
