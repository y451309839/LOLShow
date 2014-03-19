package com.yiqu.wallpaper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.youmi.android.AdManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

class PackageDownload extends Thread {
	boolean cancelUpdate = false;
	int download_precent = 0;
	Handler myHandler;
	Context mContext;
	Hero mHero;
	
	public PackageDownload(Context context, Hero hero, Handler handler){
		mHero = hero;
		mContext = context;
		myHandler = handler;
	}

	@Override
	public void run() {
		String host = AdManager.getInstance(mContext).syncGetOnlineConfig("host", null);
		if(host == null){
			Message message = myHandler.obtainMessage(4, "下载服务器连接失败！请稍候重试。");
			myHandler.sendMessage(message);
			return;
		}
		String url = host + mHero.getUrl();
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(url);
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			long length = entity.getContentLength();
			InputStream is = entity.getContent();
			File zipFile = null;
			if(response.getStatusLine().getStatusCode() == 404){
				throw new YQException("您要下载的文件找不到了~~~请稍候重试。");
			}else if (is != null) {
				Message message = myHandler.obtainMessage(0, "["+mHero.getName()+"]动画包开始下载！");
				myHandler.sendMessage(message);
				
				File rootFile = new File(mHero.dataPath);
				if (!rootFile.exists() && !rootFile.isDirectory())
					rootFile.mkdirs();

				zipFile = new File(mHero.getFilePath());
				if (zipFile.exists())
					zipFile.delete();
				zipFile.createNewFile();

				// 已读出流作为参数创建一个带有缓冲的输出流
				BufferedInputStream bis = new BufferedInputStream(is);

				// 创建一个新的写入流，将读取到的数据写入到文件中
				FileOutputStream fos = new FileOutputStream(zipFile);
				// 已写入流作为参数创建一个带有缓冲的写入流
				BufferedOutputStream bos = new BufferedOutputStream(fos);

				int read;
				long count = 0;
				int precent = 0;
				byte[] buffer = new byte[1024];
				while ((read = bis.read(buffer)) != -1 && !cancelUpdate) {
					bos.write(buffer, 0, read);
					count += read;
					precent = (int) (((double) count / length) * 100);

					// 每下载完成1%就通知任务栏进行修改下载进度
					if (precent - download_precent  >= 1) {
						download_precent = precent;
						message = myHandler.obtainMessage(3,precent);
						myHandler.sendMessage(message);
					}
				}
				if (!cancelUpdate) {
					message = myHandler.obtainMessage(2, "["+mHero.getName()+"]已下载完成！");
					myHandler.sendMessage(message);
				} else {
					message = myHandler.obtainMessage(1, "["+mHero.getName()+"]下载已取消！");
					myHandler.sendMessage(message);
					zipFile.delete();
				}
				bos.flush();
				bos.close();
				fos.flush();
				fos.close();
				bis.close();
				is.close();
			}

		} catch (YQException yq){
			yq.printStackTrace();
			Message message = myHandler.obtainMessage(4, yq.getMessage());
			myHandler.sendMessage(message);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			Message message = myHandler.obtainMessage(4, "网络连接失败！请稍候重试。");
			myHandler.sendMessage(message);
		} catch (IOException e) {
			e.printStackTrace();
			Message message = myHandler.obtainMessage(4, "文件写入失败！请稍候重试。");
			myHandler.sendMessage(message);
		} catch (Exception e) {
			e.printStackTrace();
			Message message = myHandler.obtainMessage(4, "下载失败！请稍候重试。");
			myHandler.sendMessage(message);
		}
	}
}