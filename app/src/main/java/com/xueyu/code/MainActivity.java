package com.xueyu.code;

import static android.view.Gravity.START;
import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import com.afollestad.materialdialogs.MaterialDialog;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.util.LogUtils;
import com.xueyu.adapter.CodeInfoAdapter;
import com.xueyu.adapter.GroupAdapter;
import com.xueyu.base.BaseActivity;
import com.xueyu.bean.CodeInfo;
import com.xueyu.bean.CodeGroup;
import com.xueyu.bean.Task;
import com.xueyu.listener.TaskListener;
import com.xueyu.manager.TaskManager;
import com.xueyu.view.DrawerArrowDrawable;
import com.xueyu.view.FloatingActionButton;

/**
 * @author 陈学宇
 * @version 创建时间：2015年5月28日 下午9:03:06
 */
@SuppressLint({ "InflateParams", "ClickableViewAccessibility" })
public class MainActivity extends BaseActivity implements OnScrollListener{

	private TextView title,collectBtn;
	private DrawerLayout drawer;
	private ImageView indicatorImageView;
	private DrawerArrowDrawable drawerArrowDrawable;
	private float offset;
	private boolean flipped;

	private ListView mainListview,drawerListview;
	private SwipeRefreshLayout swipeRefreshLayout;
	
	private CodeInfoAdapter adapter;
	private List<CodeInfo> codeInfos;
	
	private GroupAdapter groupAdapter;
	
	private TaskManager taskManager;
	
	private static final int STATE_REFRESH = 0;// 下拉刷新
	private static final int STATE_MORE = 1;// 加载更多
	private int limit = 10;		// 每页的数据是10条
	private int curPage = 0;		// 当前页的编号，从0开始
	private View moreView; //加载更多页面
	private int lastItem;//listview最后一个Item
	private String groupString="全部";
	private DbUtils db;
	
	private FloatingActionButton floatingActionButton;
	private boolean isAddBtnShow;
	private float lastY;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_main);
		taskManager=TaskManager.getInstance(context);
		db = DbUtils.create(this.context);
		db.configAllowTransaction(false);
		db.configDebug(true);
		codeInfos=new ArrayList<CodeInfo>();
		adapter=new CodeInfoAdapter(context, null);
		
		groupAdapter=new GroupAdapter(context, null);
		
		initView();
		initDrawer();//初始化抽屉
		
		swipeRefreshLayout.setColorScheme(R.color.main_color, R.color.green, R.color.red);
		swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				new Thread() {
					@Override
					public void run() {
						Task task = new Task();
						task.setUrl("http://www.jcodecraeer.com/plus/list.php?tid=31&TotalResult=524&PageNo=");
						task.setI(1);
						taskManager.addTask(task, new TaskListener() {
							@Override
							public void failure(String error) {
								swipeRefreshLayout.setRefreshing(false);
							}

							@Override
							public void running(final String info) {
								handler.post(new Runnable() {
									@Override
									public void run() {
										title.setText(info);
									}
								});
							}

							@Override
							public void success(String success) {
								handler.post(new Runnable() {
									@Override
									public void run() {
										// 下拉刷新(从第一页开始装载数据)
										queryData(0, STATE_REFRESH);
										swipeRefreshLayout.setRefreshing(false);
										title.setText(groupString);
									}
								});
							}
						});
					}
				}.start();
			}
		});
		
		mainListview.addFooterView(moreView);//添加底部view，一定要在setAdapter之前添加，否则会报错。
		mainListview.setAdapter(adapter);
		mainListview.setOnScrollListener(this); //设置listview的滚动事件
		mainListview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				CodeInfo codeInfo = (CodeInfo) adapter.getItem(position);
				Intent intent = new Intent(MainActivity.this, CodeDetailActivity.class);
				intent.putExtra("WebUrl", codeInfo.getContentUrl());
				startActivity(intent);
			}
		});
		mainListview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

				new MaterialDialog.Builder(context)
						.content(R.string.ifshare)
						.positiveText(R.string.sure)
						.negativeText(R.string.cancel)
						.backgroundColor(context.getResources().getColor(R.color.white))
						.contentColor(context.getResources().getColor(R.color.black))
						.callback(new MaterialDialog.ButtonCallback() {
							@Override
							public void onPositive(MaterialDialog dialog) {
								CodeInfo codeInfo2 = (CodeInfo) adapter.getItem(position);
								Intent sendIntent = new Intent();
								sendIntent.setAction(Intent.ACTION_SEND);
								sendIntent.putExtra(Intent.EXTRA_TEXT, codeInfo2.getGithubUrl());
								sendIntent.setType("text/plain");
								startActivity(sendIntent);

								dialog.dismiss();
							}

							@Override
							public void onNegative(MaterialDialog dialog) {
								dialog.dismiss();
							}
						})
						.build()
						.show();

				return true;
			}
		});
		floatingActionButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mainListview.smoothScrollToPosition(0);
			}
		});
		
		drawerListview.setAdapter(groupAdapter);
		drawerListview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				CodeGroup group=(CodeGroup) groupAdapter.getItem(position);
				groupString=group.getName();
				title.setText(groupString);
				drawToggle();
				//(从第一页开始装载数据)
				queryData(0, STATE_REFRESH);
			}
		});
		
		handler.post(new Runnable() {
			@Override
			public void run() {
				//(从第一页开始装载数据)
				queryData(0, STATE_REFRESH);
				addGroup();//把抽屉的分组也加载进来
				swipeRefreshLayout.setRefreshing(false);
			}
		});
	}
	Handler handler=new Handler();
	/**
	 * 分页获取数据
	 * @param page	页码
	 * @param actionType	ListView的操作类型（下拉刷新、上拉加载更多）
	 */
	private void queryData(final int page, final int actionType) {
		LogUtils.e("开始从数据库查找，第"+page+"页");
		
		List<CodeInfo> codeInfoList = null;
		try {
			if(groupString.equals("全部")){
				codeInfoList = db.findAll(Selector.from(CodeInfo.class)
						.offset(page*limit).limit(limit).orderBy("time", true));
			}else {
				codeInfoList = db.findAll(Selector.from(CodeInfo.class)
						.where("codeGroup", "=", groupString)
						.offset(page*limit).limit(limit).orderBy("time", true));
			}
		} catch (DbException e) {
			e.printStackTrace();
		}
		
		if (codeInfoList!=null && codeInfoList.size() > 0) {
			if (actionType == STATE_REFRESH) {
				adapter.removeAll();
				codeInfos.clear();
				// 当是下拉刷新操作时，将当前页的编号重置为0，重新添加
				curPage = 0;
			}
			adapter.addAll(codeInfoList);
			codeInfos.addAll(codeInfoList);
			// 这里在每次加载完数据后，将当前页码+1，这样在上拉刷新的onPullUpToRefresh方法中就不需要操作curPage了
			curPage++;
		} else if (actionType == STATE_MORE) {
			toast("没有更多数据了");
		} else if (actionType == STATE_REFRESH) {
			toast("没有数据");
		}
	}
	/**
	 * 添加分组
	 */
	private void addGroup(){
		List<CodeGroup> groups=new ArrayList<CodeGroup>();

		try {
			groups=db.findAll(Selector.from(CodeGroup.class).orderBy("groupName"));

			if (groups!=null){
				List<CodeInfo> codeInfoList=null;
				for(int i=0;i<groups.size();i++){
					codeInfoList = db.findAll(Selector.from(CodeInfo.class)
							.where("codeGroup", "=", groups.get(i).getName()));
					groups.get(i).setCount(codeInfoList.size());
					codeInfoList.clear();
					codeInfoList=null;
				}
			}
		} catch (DbException e) {
			e.printStackTrace();
			LogUtils.e(e.toString());
		}
		if(groups!=null&&groups.size()>0){
			groupAdapter.removeAll();
			
			CodeGroup group=new CodeGroup();
			List<CodeInfo> codeInfoList = null;
			try {
				codeInfoList = db.findAll(CodeInfo.class);
			} catch (DbException e) {
				e.printStackTrace();
			}
			group.setCount(codeInfoList.size());
			codeInfoList.clear();
			codeInfoList=null;
			group.setName("全部");
			groupAdapter.add(group);
			groupAdapter.addAll(groups);
		}
	}
	/**
	 * 初始化抽屉内容
	 */
	private void initDrawer(){
		final Resources resources = getResources();
		drawerArrowDrawable = new DrawerArrowDrawable(resources);
		drawerArrowDrawable.setStrokeColor(resources.getColor(R.color.white));
		indicatorImageView.setImageDrawable(drawerArrowDrawable);
		
		drawer.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
			@Override
			public void onDrawerSlide(View drawerView, float slideOffset) {
				offset = slideOffset;
				if (slideOffset >= .995) {
					flipped = true;
					drawerArrowDrawable.setFlip(flipped);
				} else if (slideOffset <= .005) {
					flipped = false;
					drawerArrowDrawable.setFlip(flipped);
				}
				drawerArrowDrawable.setParameter(offset);
			}
		});
		indicatorImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				drawToggle();
			}
		});
	}
	private long exitTime=0;
	// 按下菜单键时
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {//按下菜单键
			drawToggle();
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_BACK&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if (drawer.isDrawerVisible(START)) {
				drawer.closeDrawer(START);
			}else{
				if (System.currentTimeMillis() - exitTime > 2000) {
					toast("再按一次就退出嗲");
					exitTime = System.currentTimeMillis();
				} else {
					finish();
					System.exit(0);
				}
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	/**
	 * 抽屉开关
	 */
	private void drawToggle(){
		if (drawer.isDrawerVisible(START)) {
			drawer.closeDrawer(START);
		} else {
			drawer.openDrawer(START);
		}
	}
	private void initView(){
		collectBtn=(TextView)findViewById(R.id.main_collect);
		collectBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				forward(CollectActivity.class,false);
			}
		});
		drawer=(DrawerLayout)findViewById(R.id.drawer_layout);
		drawerListview=(ListView)findViewById(R.id.drawer_listView);
		indicatorImageView=(ImageView)findViewById(R.id.drawer_indicator);
		title=(TextView)findViewById(R.id.main_title);
		
		mainListview=(ListView)findViewById(R.id.main_listView);
		swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.main_swipelayout);
		moreView = getLayoutInflater().inflate(R.layout.layout_listview_more, null);
		moreView.setVisibility(View.GONE);
		
		floatingActionButton=(FloatingActionButton)findViewById(R.id.main_floatbutton);
	}
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
        //下拉到空闲是，且最后一个item的数等于数据的总数时，进行更新
       if(lastItem == codeInfos.size()  && scrollState == this.SCROLL_STATE_IDLE){
    	   if(codeInfos.size()>0){
    		   moreView.setVisibility(View.VISIBLE);
    	   }
           handler.post(new Runnable() {
        	   @Override
        	   public void run() {
    			// 上拉刷新(从第一页开始装载数据)
        	    queryData(curPage, STATE_MORE);
                moreView.setVisibility(View.GONE);
				title.setText(groupString);
    		}
    	   });
       }
	}
	private int mLastScrollY;
	private int mPreviousFirstVisibleItem = 0;
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
		lastItem = firstVisibleItem + visibleItemCount - 1;  //减1是因为上面加了个addFooterView


		/**
		 * 以下代码为是否要显示浮动按钮
		 */
		if (totalItemCount == 0)
			return;
		if (firstVisibleItem == mPreviousFirstVisibleItem) {
			int newScrollY = getTopItemScrollY();
			boolean isSignificantDelta = Math.abs(mLastScrollY - newScrollY) > 4;
			if (isSignificantDelta) {
				if (mLastScrollY > newScrollY) {
					// 添加按钮隐藏
					floatingActionButton.hide();
				} else {
					// 添加按钮显示
					floatingActionButton.show();
				}
			}
			mLastScrollY = newScrollY;
		} else {
			if (firstVisibleItem > mPreviousFirstVisibleItem) {
				// 添加按钮隐藏
				floatingActionButton.hide();
			} else {
				// 添加按钮显示
				floatingActionButton.show();
			}
			mLastScrollY = getTopItemScrollY();
			mPreviousFirstVisibleItem = firstVisibleItem;
		}
	}
	private int getTopItemScrollY() {
		if (mainListview == null || mainListview.getChildAt(0) == null)
			return 0;
		View topChild = mainListview.getChildAt(0);
		return topChild.getTop();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (adapter != null) {
			adapter = null;
			if(codeInfos!=null&&codeInfos.size()>0){
				codeInfos.clear();
				codeInfos=null;
			}
		    System.gc();
		}
		db.close();
		handler.removeCallbacksAndMessages(null);
	}
}