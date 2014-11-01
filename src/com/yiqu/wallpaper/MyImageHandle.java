package com.yiqu.wallpaper;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.widget.ImageView;

public class MyImageHandle {

	/**
	 * 处理图片
	 * 
	 * @param bm
	 *            所要转换的bitmap
	 * @param newWidth新的宽
	 * @param newHeight新的高
	 * @return 指定宽高的bitmap
	 */
	public static Bitmap zoomImg(Bitmap bm, int newWidth, int newHeight) {
		// 获得图片的宽高
		int width = bm.getWidth();
		int height = bm.getHeight();
		// 计算缩放比例
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 取得想要缩放的matrix参数
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		// 得到新的图片
		Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
		return newbm;
	}

	public static Bitmap drawTextToBitmap(Bitmap bitmap, String gText) {
		Bitmap.Config bitmapConfig = bitmap.getConfig();
		// set default bitmap config if none
		if (bitmapConfig == null) {
			bitmapConfig = Bitmap.Config.ARGB_8888;
		}
		// resource bitmaps are imutable,
		// so we need to convert it to mutable one
		bitmap = bitmap.copy(bitmapConfig, true);

		Canvas canvas = new Canvas(bitmap);
		// new antialised Paint
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		// text color
		paint.setColor(Color.rgb(200, 200, 200));
		// text size in pixels
		paint.setTextSize(18);
		// text shadow
		paint.setShadowLayer(0.5f, 0f, 1f, Color.BLACK);// 阴影制作

		// draw text to the Canvas center
		Rect bounds = new Rect();
		paint.getTextBounds(gText, 0, gText.length(), bounds);
		int x = (bitmap.getWidth() - bounds.width()) / 2;
		int y = (bitmap.getHeight() + bounds.height()) / 2;

		canvas.drawText(gText, x, y, paint);

		return bitmap;
	}
	
	/**
	 * 创建倒影
	 * @param originalImage
	 * @return
	 */
	public static Bitmap reflectedImage(Bitmap originalImage) {
		// 倒影图和原图之间的距离
		final int reflectionGap = 4;
		// 返回原图解码之后的bitmap对象
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();
		// 创建矩阵对象
		Matrix matrix = new Matrix();

		// 指定一个角度以0,0为坐标进行旋转
		// matrix.setRotate(30);

		// 指定矩阵(x轴不变，y轴相反)
		matrix.preScale(1, -1);

		// 将矩阵应用到该原图之中，返回一个宽度不变，高度为原图1/2的倒影位图
		Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0,
							height / 2, width, height / 2, matrix, false);

		// 创建一个宽度不变，高度为原图+倒影图高度的位图
		Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
							(height + height / 2), Config.ARGB_8888);

		// 将上面创建的位图初始化到画布
		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(originalImage, 0, 0, null);

		Paint deafaultPaint = new Paint();
		deafaultPaint.setAntiAlias(false);
		// canvas.drawRect(0, height, width, height +
		// reflectionGap,deafaultPaint);
		canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);
		Paint paint = new Paint();
		paint.setAntiAlias(false);
		/**
		* 参数一:为渐变起初点坐标x位置， 参数二:为y轴位置， 参数三和四:分辨对应渐变终点， 最后参数为平铺方式，
		* 这里设置为镜像Gradient是基于Shader类，所以我们通过Paint的setShader方法来设置这个渐变
		*/
		LinearGradient shader = new LinearGradient(0,
							originalImage.getHeight(), 0,
							bitmapWithReflection.getHeight() + reflectionGap,
							0x70ffffff, 0x00ffffff, TileMode.MIRROR);
		// 设置阴影
		paint.setShader(shader);
		paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN));
		// 用已经定义好的画笔构建一个矩形阴影渐变效果
		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
							+ reflectionGap, paint);

		return bitmapWithReflection;
	}

	/**
	 * 右下角加上图片水印
	 * @param src
	 * @param watermark
	 * @return
	 */
	public static Bitmap createWatermarkBitmap(Bitmap src, Bitmap watermark) {
		String tag = "createBitmap";
		// Log.d( tag, "create a new bitmap" );
		if (src == null) {
			return null;
		}
		int w = src.getWidth();
		int h = src.getHeight();
		int ww = watermark.getWidth();
		int wh = watermark.getHeight();
		
		// create the new blank bitmap
		Bitmap newb = Bitmap.createBitmap(w, h, Config.RGB_565);
		// 创建一个新的和SRC长度宽度一样的位图
		Canvas cv = new Canvas(newb);
		// draw src into
		cv.drawBitmap(src, 0, 0, null);// 在 0，0坐标开始画入src
		// draw watermark into
		cv.drawBitmap(watermark, w - ww, h - wh, null);// 在src的右下角画入水印
		// save all clip
		cv.save(Canvas.ALL_SAVE_FLAG);// 保存
		// store
		cv.restore();// 存储
		return newb;
	}

	/**
	 * 将彩色图转换为黑白图
	 * 
	 * @param 位图
	 * @return 返回转换好的位图
	 */
	public static Bitmap convertToBlackWhite(Bitmap bmp) {
		int width = bmp.getWidth(); // 获取位图的宽
		int height = bmp.getHeight(); // 获取位图的高
		int[] pixels = new int[width * height]; // 通过位图的大小创建像素点数组
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		int alpha = 0xFF << 24;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int grey = pixels[width * i + j];
				int red = ((grey & 0x00FF0000) >> 16);
				int green = ((grey & 0x0000FF00) >> 8);
				int blue = (grey & 0x000000FF);
				grey = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
				grey = alpha | (grey << 16) | (grey << 8) | grey;
				pixels[width * i + j] = grey;
			}
		}
		Bitmap newBmp = Bitmap.createBitmap(width, height, Config.RGB_565);
		newBmp.setPixels(pixels, 0, width, 0, 0, width, height);
		return newBmp;
	}

	/**
	 * 转换成圆角
	 * 
	 * @param bmp
	 * @param roundPx
	 * @return
	 */
	public static Bitmap convertToRoundedCorner(Bitmap bmp, float roundPx) {
		Bitmap newBmp = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(),
				Config.ARGB_8888);
		// 得到画布
		Canvas canvas = new Canvas(newBmp);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
		final RectF rectF = new RectF(rect);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		// 第二个和第三个参数一样则画的是正圆的一角，否则是椭圆的一角
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bmp, rect, rect, paint);
		return newBmp;
	}

	/**
	 * 高斯模糊
	 * 
	 * @param bmp
	 * @return
	 */
	public static Bitmap convertToBlur(Bitmap bmp) {
		// 高斯矩阵
		int[] gauss = new int[] { 1, 2, 1, 2, 4, 2, 1, 2, 1 };
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap newBmp = Bitmap.createBitmap(width, height,
				Bitmap.Config.RGB_565);
		int pixR = 0;
		int pixG = 0;
		int pixB = 0;
		int pixColor = 0;
		int newR = 0;
		int newG = 0;
		int newB = 0;
		int delta = 16; // 值越小图片会越亮，越大则越暗
		int idx = 0;
		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		for (int i = 1, length = height - 1; i < length; i++) {
			for (int k = 1, len = width - 1; k < len; k++) {
				idx = 0;
				for (int m = -1; m <= 1; m++) {
					for (int n = -1; n <= 1; n++) {
						pixColor = pixels[(i + m) * width + k + n];
						pixR = Color.red(pixColor);
						pixG = Color.green(pixColor);
						pixB = Color.blue(pixColor);
						newR = newR + pixR * gauss[idx];
						newG = newG + pixG * gauss[idx];
						newB = newB + pixB * gauss[idx];
						idx++;
					}
				}
				newR /= delta;
				newG /= delta;
				newB /= delta;
				newR = Math.min(255, Math.max(0, newR));
				newG = Math.min(255, Math.max(0, newG));
				newB = Math.min(255, Math.max(0, newB));
				pixels[i * width + k] = Color.argb(255, newR, newG, newB);
				newR = 0;
				newG = 0;
				newB = 0;
			}
		}
		newBmp.setPixels(pixels, 0, width, 0, 0, width, height);
		return newBmp;
	}

}
