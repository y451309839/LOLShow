package com.yiqu.wallpaper;

import net.youmi.android.offers.OffersManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class NowGetJifen {
	
	public static void show(final Context c) {
		//OffersManager.getInstance(this).showOffersWall(); //有米广告全屏
    	//OffersManager.getInstance(this).showOffersWallDialog(this); //有米广告Dialog
    	AlertDialog luckDialog = new AlertDialog.Builder(c).create();
    	luckDialog.setTitle("提示");
    	luckDialog.setMessage("您的积分不足以进行该操作，是否立即免费赚取积分？");
    	luckDialog.setCancelable(false);
    	luckDialog.setButton(DialogInterface.BUTTON_POSITIVE, "赚取积分",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//OffersManager.getInstance(c).showOffersWallDialog((Activity) c); //有米广告Dialog
						OffersManager.getInstance(c).showOffersWall();
						dialog.dismiss();
					}
				});
    	luckDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
    	luckDialog.show();
	}

}
