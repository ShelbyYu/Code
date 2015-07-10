package com.xueyu.listener;

/**
 * @author 陈学宇
 * @version 创建时间：2015年5月28日 下午9:19:45
 */
public abstract interface TaskListener {
	
	public abstract void success(String success);
	
	public abstract void failure(String error);

	public abstract void running(String info);
}
