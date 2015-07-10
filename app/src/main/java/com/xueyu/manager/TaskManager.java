package com.xueyu.manager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.util.LogUtils;
import com.xueyu.bean.CodeInfo;
import com.xueyu.bean.CodeGroup;
import com.xueyu.bean.Task;
import com.xueyu.listener.TaskListener;
import com.xueyu.util.SingleHttpClient;

import android.content.Context;
import android.text.TextUtils;

/**
 * @author 陈学宇
 * @version 创建时间：2015年5月28日 下午9:22:45
 */
public class TaskManager {
	private Context context;
	private static TaskManager taskManager;
	private DbUtils db;
	
	private TaskManager(Context context){
		this.context=context;
		db = DbUtils.create(this.context);
		db.configAllowTransaction(false);
		db.configDebug(true);
	}
	public static TaskManager getInstance(Context context){
		if(taskManager==null){
			taskManager=new TaskManager(context);
		}
		return taskManager;
	}

	public void addTask(Task task, TaskListener taskListener) {
		LogUtils.e("-----------开始--------------");
		boolean isStop = false;  //是否暂停继续抓取网页内容
		int index = task.getI(); //开始的索引

		while (!isStop) {
			LogUtils.e("继续循环:"+index);
			
			String url = task.getUrl() + index;// 拼接请求网址链接
			LogUtils.e("请求的网址链接："+url);
			taskListener.running("正在请求第"+index+"页");
			List<CodeInfo> list = new ArrayList<CodeInfo>();
			list = getCodeList(url);

			if (list != null && list.size() > 0) {
				LogUtils.e("抓取网页内容返回的列表不为空，且大于0");
				taskListener.running("请求的网页内容有东西");
				try {
					// 在这里检查数据库是否已经存在了
					for (CodeInfo code : list) {

						CodeInfo dbCodeInfo = db.findFirst(Selector.from(CodeInfo.class)
								.where("contentUrl", "=",code.getContentUrl()));
						if(dbCodeInfo==null){
							taskListener.running(code.getName()+"：数据库中木有");

							CodeGroup codeGroup=null;
							if(!TextUtils.isEmpty(code.getCodeGroup().trim())){
								codeGroup =db.findFirst(Selector.from(CodeGroup.class).where("groupName", "=", code.getCodeGroup()));
								if(codeGroup==null){
									taskListener.running(code.getCodeGroup()+"：不存在该分组");
									LogUtils.e("数据库不存在此分组:"+code.getCodeGroup());
									LogUtils.e("开始保存此分组");
									codeGroup=new CodeGroup();
									codeGroup.setCount(0);
									codeGroup.setName(code.getCodeGroup());
									db.save(codeGroup);
								}
							}
							//获取这个项目在Github中的地址
							String github=getGithubUrl(code.getContentUrl());
							code.setGithubUrl(github);
							LogUtils.e("Github地址："+github);

							LogUtils.e("数据库不存在此CodeInfo");
							LogUtils.e("开始保存此CodeInfo");
							//把CodeInfo添加到分组中，然后保存
							db.save(code);
							taskListener.running(code.getName()+"：保存好了");
						}else {
							LogUtils.e("数据库已经存在此CodeInfo啦");
							taskListener.running(code.getName()+"：数据库中已存在");
							isStop = true;
							LogUtils.e("停止继续抓取网页");
							taskListener.success("成功");
							break;//跳出这个循环
						}
					}
				} catch (DbException e) {
					e.printStackTrace();
					LogUtils.e(e.toString());
				}
			} else {
				LogUtils.e("抓取网页内容返回的列表为空");
				isStop = true;
				LogUtils.e("停止继续抓取网页");
				taskListener.success("成功");
			}
			index++;
			list.clear();
		}
	}
	/**
	 * 对请求返回的网页内容进行内容抓取，并封装成对象列表返回
	 * @param url 请求的网址链接
	 * @return
	 */
	private List<CodeInfo> getCodeList(String url){
		LogUtils.e("开始抓取网页内容");
		
		List<CodeInfo> codeList=new ArrayList<CodeInfo>();
		
		String response = null;
		StringBuilder sb = new StringBuilder();
		HttpGet get = new HttpGet(url);
		try {
			// 发送GET请求
			HttpResponse httpResponse = SingleHttpClient.getInstance().execute(get);
			HttpEntity entity = httpResponse.getEntity();
			
			if (entity != null) {
				LogUtils.e("获取到的网页内容实体不为空");
				
				BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent(), "GB2312"));
				while ((response = br.readLine()) != null) {
					sb.append(response);
				}
				Document doc = Jsoup.parse(sb.toString());
	   			Elements trs = doc.select("li[class=codeli]");
	   			
	   			CodeInfo code;
	   			for (Element tr : trs) {
	   				LogUtils.e("存在codeli");
	   				
	   				code = new CodeInfo();
	   				
	   				LogUtils.e("图片链接:"+SingleHttpClient.mainHost+tr.getElementsByTag("img").get(0).attr("src"));
	   				code.setPhotoUrl(SingleHttpClient.mainHost + tr.getElementsByTag("img").get(0).attr("src"));
	   				
	   				Elements aElements=tr.getElementsByTag("a");
	   				LogUtils.e("内容链接:" + SingleHttpClient.mainHost + aElements.get(0).attr("href"));
	   				code.setContentUrl(SingleHttpClient.mainHost + aElements.get(0).attr("href"));

	   				LogUtils.e("标题:"+aElements.get(1).text());
	   				code.setName(aElements.get(1).text());
	   				
	   				LogUtils.e("内容:"+tr.select("p[class=codeli-description]").get(0).text());
	   				code.setContent(tr.select("p[class=codeli-description]").get(0).text());
	   				
	   				Elements spanElements=tr.select("div[class=otherinfo]").get(0).getElementsByTag("span");
	   				LogUtils.e("分组:"+spanElements.get(0).text().replaceAll(" ", ""));
	   				code.setCodeGroup(spanElements.get(0).text().replaceAll(" ", ""));

	   				LogUtils.e("时间："+spanElements.get(3).text());
	   				code.setTime(spanElements.get(3).text());
	   				
	   				codeList.add(code);
	   				
	   				LogUtils.e("----------------------------------------");
	   			}
				trs.clear();
			}else {
				LogUtils.e("获取到的网页内容实体为空！！");
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtils.e(e.toString());
		}
		return codeList;
	}
	@SuppressWarnings("finally")
	private String getGithubUrl(String contentUrl){
		String githubUrl="https://github.com/";

		StringBuilder sb = new StringBuilder();
		HttpGet get = new HttpGet(contentUrl);
		try {
			// 发送GET请求
			HttpResponse httpResponse = SingleHttpClient.getInstance().execute(get);
			HttpEntity entity = httpResponse.getEntity();
			if (entity != null) {
				String response = null;
				BufferedReader br = new BufferedReader(new InputStreamReader(
						entity.getContent(), "GB2312"));
				while ((response = br.readLine()) != null) {
					sb.append(response);
				}
				String htmlDataString=sb.toString();
				int index=htmlDataString.indexOf("var address");
				String subsString=htmlDataString.substring(index, index+150);
				htmlDataString=null;
				int firstIndex=subsString.indexOf("\"");
				int secondIndex=subsString.indexOf("\"", firstIndex+1);
				githubUrl=subsString.substring(firstIndex+1, secondIndex);
				subsString=null;
				br.close();
			}
		}catch (Exception e) {
			e.printStackTrace();
			LogUtils.e(e.toString());
		}finally{
			return githubUrl;
		}
	}
}