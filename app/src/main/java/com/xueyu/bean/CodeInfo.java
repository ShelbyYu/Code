package com.xueyu.bean;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Foreign;
import com.lidroid.xutils.db.annotation.Table;

/**
 * @author 陈学宇
 * @version 创建时间：2015年5月28日 下午9:04:35
 */

@Table(name="CodeInfo")
public class CodeInfo extends EntityBase{
	@Column(column="photoUrl")
	private String photoUrl;
	
	@Column(column="contentUrl")
	private String contentUrl;
	
	@Column(column="name")
	private String name;
	
	@Column(column="content")
	private String content;
	
	@Column(column="label")
	private String label;
	
	@Column(column="codeGroup")
	private String codeGroup;
	
	@Column(column="time")
	private String time;

	@Column(column="githubUrl")
	private String githubUrl;

	@Column(column = "isCollect")
	private int isCollect=0;

	public String getPhotoUrl() {
		return photoUrl;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public String getContentUrl() {
		return contentUrl;
	}

	public void setContentUrl(String contentUrl) {
		this.contentUrl = contentUrl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}


	public String getCodeGroup() {
		return codeGroup;
	}

	public void setCodeGroup(String codeGroup) {
		this.codeGroup = codeGroup;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getGithubUrl() {
		return githubUrl;
	}

	public void setGithubUrl(String githubUrl) {
		this.githubUrl = githubUrl;
	}

	public int getIsCollect() {
		return isCollect;
	}

	public void setIsCollect(int isCollect) {
		this.isCollect = isCollect;
	}
}
