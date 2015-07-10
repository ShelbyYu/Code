package com.xueyu.bean;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Finder;
import com.lidroid.xutils.db.annotation.Table;
import com.lidroid.xutils.db.sqlite.FinderLazyLoader;

/**
 * @author 陈学宇
 * @version 创建时间：2015年5月28日 下午9:11:39
 */
@Table(name="CodeGroup")
public class CodeGroup extends EntityBase{
	@Column(column="groupName")
	private String groupName;

	@Column(column = "count")
	private int count;

	public String getName() {
		return groupName;
	}

	public void setName(String name) {
		this.groupName = name;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
