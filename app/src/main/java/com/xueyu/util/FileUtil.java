package com.xueyu.util;

import android.os.Environment;

import com.lidroid.xutils.util.LogUtils;

import java.io.File;

/**
 * @author 陈学宇
 * @version 创建时间：2015年6月9日 下午8:28:37
 */
public class FileUtil {

	/**
	 * 获取应用的主路径
	 * @return
	 */
	public static String getMainPath(){
		String path=null;
		String extStatus=Environment.getExternalStorageState();
		File extFile=Environment.getExternalStorageDirectory();
		if(extStatus.equals(Environment.MEDIA_MOUNTED)){
			LogUtils.e("有外置内存卡");
			path=extFile.getAbsolutePath()+"/AndroidCode";
		}else {
			LogUtils.e("没有外置内存卡");
			path="/mnt/sdcard/AndoridCode";
		}

		return path;
	}

	/**
	 * 获取缓存路径
	 * @return
	 */
	public static File getCacheFile(){

		File file=new File(getMainPath());
		if (!file.exists()){
			file.mkdirs();
		}

		return file;
	}

	/**
	 * 获取文件的类型
	 * @param url
	 * @return
	 */
	public static String getFileType(String url){
		String type=".png";
		
		if(url!=null&&url.length()>0){
			int x=url.lastIndexOf(".");
			if(x>-1&&(x<url.length()-1)){
				type=url.substring(x+1);
				return type;
			}
		}
		return type;
	}
}
