package com.xueyu.code;

import com.xueyu.base.BaseActivity;
import com.xueyu.util.FileUtil;
import com.xueyu.view.ProgressWheel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * @author 陈学宇
 * @version 创建时间：2015年5月29日 下午6:57:14
 */
@SuppressLint({ "HandlerLeak", "SetJavaScriptEnabled" })
public class CodeDetailActivity extends BaseActivity implements  OnClickListener{
	private WebView webview;
	private ImageView share;
	private ProgressWheel progressWheel;
	private WebSettings webSettings;
	private RelativeLayout back;
	private String SDPATH = FileUtil.getMainPath() + "/WebCache";
	
	private String webUrl=null;
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_codedetail);
		webview=(WebView)findViewById(R.id.codedetail_webview);
		progressWheel=(ProgressWheel)findViewById(R.id.codedetail_progress);
		back=(RelativeLayout)findViewById(R.id.codedetail__back_rl);
		share=(ImageView)findViewById(R.id.codedetail_share);
		share.setOnClickListener(this);
		back.setOnClickListener(this);

		webUrl=getIntent().getStringExtra("WebUrl");
		
		handler.sendEmptyMessage(0);
		webview.setWebViewClient(new MyWebviewClient());
		webSettings= webview.getSettings();
		
		webSettings.setJavaScriptEnabled(true);
		webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		webSettings.setLoadWithOverviewMode(true);
		webSettings.setRenderPriority(RenderPriority.HIGH);
	    // 建议缓存策略为，判断是否有网络，有的话，使用LOAD_DEFAULT,无网络时，使用LOAD_CACHE_ELSE_NETWORK
		webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); // 设置缓存模式
	    // 设置Application caches缓存目录
	    webSettings.setAppCachePath(SDPATH);
	    // 开启Application Cache功能
	    webSettings.setAppCacheEnabled(true);
	    
		if(Build.VERSION.SDK_INT >= 19) {
			webSettings.setLoadsImagesAutomatically(true);
	    } else {
	    	webSettings.setLoadsImagesAutomatically(false);
	    }
		webview.loadUrl(webUrl);
	}
	
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				progressWheel.setVisibility(View.VISIBLE);//显示进度框
				break;
			case 1:
				progressWheel.setVisibility(View.GONE);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.codedetail__back_rl:
				finish();
				break;
			case R.id.codedetail_share:
				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_TEXT, webUrl);
				sendIntent.setType("text/plain");
				startActivity(sendIntent);
				break;
			default:
				break;
		}
	}

	class MyWebviewClient extends WebViewClient{
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		    startActivity(i);
			return true;
		}
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			handler.sendEmptyMessage(1);
			if(!webSettings.getLoadsImagesAutomatically()) {
				webSettings.setLoadsImagesAutomatically(true);
		    }
		}
	}
}
