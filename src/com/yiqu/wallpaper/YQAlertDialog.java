package com.yiqu.wallpaper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class YQAlertDialog {
	
	Context c;
	AlertDialog dlg;
	private TextView DialogTitle;
	private Button DialogCloseBtn;
	private Button DialogDlBtn;
	private MyProgressBar DialogDlProgressBar;
	
	public YQAlertDialog(Context c, String TitleText) {
		this.c = c;
		creatDlg(TitleText,null,false);
	}
	
	public YQAlertDialog(Context c, String TitleText, String BtnText) {
		this.c = c;
		creatDlg(TitleText,BtnText,false);
	}
	
	public YQAlertDialog(Context c, String TitleText, String BtnText, Boolean isCanceledOnTouchOutside) {
		this.c = c;
		creatDlg(TitleText,BtnText,isCanceledOnTouchOutside);
	}
	
	private void creatDlg(String TitleText, String BtnText, Boolean isCanceledOnTouchOutside){
		dlg = new AlertDialog.Builder(c).create();
		dlg.show();
		dlg.setCanceledOnTouchOutside(isCanceledOnTouchOutside);//触摸对话框边缘外部，对话框不自动消失
		dlg.setContentView(R.layout.dl_dialog_layout);

		DialogCloseBtn = (Button) dlg.findViewById(R.id.DialogCloseBtn);
		DialogTitle = (TextView) dlg.findViewById(R.id.DialogTitle);
		DialogDlBtn = (Button) dlg.findViewById(R.id.DialogDlBtn);
		DialogDlProgressBar = (MyProgressBar) dlg.findViewById(R.id.DialogDlProgressBar);
		
		DialogTitle.setText(TitleText);
		if(BtnText==null)
			DialogDlBtn.setVisibility(View.GONE);
		else
			DialogDlBtn.setText(BtnText);
		DialogCloseBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					dlg.dismiss();
				}
		});
	}
	
	public void setBtnOnClick(OnClickListener l){
		DialogDlBtn.setOnClickListener(l);
	}
	
	public void setBarProgress(int i){
		DialogDlProgressBar.setProgress(i);
	}
	
	public void setBtnVisibility(int ViewVisibility){
		DialogDlBtn.setVisibility(ViewVisibility);
	}
	
	public void setProgressBarVisibility(int ViewVisibility){
		DialogDlProgressBar.setVisibility(ViewVisibility);
	}
	
	public void closeDlg(){
		dlg.dismiss();
	}
	
	public void setDlgDismissListener(OnDismissListener l){
		dlg.setOnDismissListener(l);
	}

}
