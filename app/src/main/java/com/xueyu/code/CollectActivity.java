package com.xueyu.code;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.util.LogUtils;
import com.xueyu.adapter.CollectAdapter;
import com.xueyu.base.BaseActivity;
import com.xueyu.bean.CodeCollect;
import com.xueyu.bean.CodeInfo;
import com.xueyu.bean.Collection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shey on 2015/6/21 00:13.
 * Email:1768037936@qq.com
 */
public class CollectActivity extends BaseActivity implements View.OnClickListener{
    private RelativeLayout back;
    private CollectAdapter collectAdapter;
    private GridView gridView;
    private DbUtils db;
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_collect);
        db = DbUtils.create(context);
        db.configAllowTransaction(false);
        db.configDebug(true);
        initView();

        collectAdapter=new CollectAdapter(context,null);
        gridView.setAdapter(collectAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Collection collection = (Collection) collectAdapter.getItem(position);
                Intent intent = new Intent(CollectActivity.this, CodeDetailActivity.class);
                intent.putExtra("WebUrl", collection.getCodeInfo().getContentUrl());
                startActivity(intent);
            }
        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
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
                                Collection collection = (Collection) collectAdapter.getItem(position);
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, collection.getCodeInfo().getGithubUrl());
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

        handler.post(new Runnable() {
            @Override
            public void run() {
                queryData();
            }
        });
    }
    Handler handler=new Handler();
    private void queryData(){
        List<Collection> collectionList=new ArrayList<Collection>();
        List<CodeCollect> codeCollectList=new ArrayList<CodeCollect>();
        try {
            codeCollectList=db.findAll(CodeCollect.class);
            Collection collection;
            for (CodeCollect codeCollect:codeCollectList){
                CodeInfo codeInfo=db.findFirst(Selector.from(CodeInfo.class)
                        .where("id","=",codeCollect.getCodeId()));
                if (codeInfo!=null){
                    collection=new Collection();
                    collection.setCodeCollect(codeCollect);
                    collection.setCodeInfo(codeInfo);

                    collectionList.add(collection);
                }
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
        if (collectionList!=null && collectionList.size() > 0) {
            collectAdapter.removeAll();
            collectAdapter.addAll(collectionList);
        }
    }
    private void initView(){
        back=(RelativeLayout)findViewById(R.id.collect__back_rl);
        back.setOnClickListener(this);
        gridView=(GridView)findViewById(R.id.collect_gridview);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.collect__back_rl:
                finish();
                break;
            default:
                break;
        }
    }
}
