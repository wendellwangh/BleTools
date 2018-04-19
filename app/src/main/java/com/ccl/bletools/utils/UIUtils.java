package com.ccl.bletools.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;

import com.ccl.bletools.App;

public class UIUtils {

	/**
	 * 获得上下文
	 * 
	 * @return
	 */
	public static Context getContext() {
		return App.getContext();
	}

	/**
	 * 获得资源
	 * 
	 * @return
	 */
	public static Resources getResources() {
		return getContext().getResources();
	}

	/**
	 * 获得string类型的数据
	 * 
	 * @param resId
	 * @return
	 */
	public static String getString(int resId) {
		return getContext().getResources().getString(resId);
	}

	/**
	 * 获取string类型
	 * 
	 * @param resId
	 * @param formatArgs
	 * @return
	 */
	public static String getString(int resId, Object... formatArgs) {
		return getContext().getResources().getString(resId, formatArgs);
	}

	/**
	 * 获得数组集合
	 * 
	 * @param resId
	 * @return
	 */
	public static String[] getStringArray(int resId) {
		return getResources().getStringArray(resId);
	}

	/**
	 * 获得颜色值
	 * 
	 * @param resId
	 * @return
	 */
	public static int getColor(int resId) {
		return getResources().getColor(resId);
	}

	/**
	 * 获得handler
	 *
	 * @return
	 */
	public static Handler getMainHandler() {
		return App.getHandler();
	}

	/**
	 * 在主线程中执行任务
	 *
	 * @param task
	 */
	public static void post(Runnable task) {
		getMainHandler().post(task);
	}

	/**
	 * 延时执行任务
	 * 
	 * @param task
	 * @param delayMillis
	 */
	public static void postDelayed(Runnable task, long delayMillis) {
		getMainHandler().postDelayed(task, delayMillis);
	}

	/**
	 * 从消息队列中移除任务
	 * 
	 * @param task
	 */
	public static void removeCallbacks(Runnable task) {
		getMainHandler().removeCallbacks(task);
	}

	/**
	 * 像素转dp
	 * 
	 * @param px
	 * @return
	 */
	public static int px2dp(int px) {
		final float scale = getResources().getDisplayMetrics().density;
		return (int) (px / scale + 0.5f);
	}

	/**
	 * dp转px
	 * 
	 * @param dp
	 * @return
	 */
	public static int dp2px(int dp) {
		final float scale = getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}


    /**
	 * 获得包名
	 * 
	 * @return
	 */
	public static String getPackageName() {
		return getContext().getPackageName();
	}

	/**得到主线程id*/
	public static long getMainThreadid() {
		return App.getMainThreadId();
	}

	/**安全的执行一个任务*/
	public static void postTaskSafely(Runnable task) {
		int curThreadId = android.os.Process.myTid();

		if (curThreadId == getMainThreadid()) {// 如果当前线程是主线程
			task.run();
		} else {// 如果当前线程不是主线程
			getMainHandler().post(task);
		}

	}

	/**
	 * 缩放图片
	 */
	public static Bitmap scaleBitmap(Bitmap bgimage, int newWidth, int newHeight) {
		int width = bgimage.getWidth();
		int height = bgimage.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, width, height, matrix, true);
		return bitmap;
	}

}
