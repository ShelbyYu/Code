package com.xueyu.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.xueyu.bean.CodeCollect;
import com.xueyu.bean.CodeInfo;
import com.xueyu.code.R;

/**
 * @author 陈学宇
 * @version 创建时间：2015年5月29日 下午2:12:12
 */
public class CodeInfoAdapter extends SimpleBaseAdapter<CodeInfo> {

	private DbUtils db;
	
	public CodeInfoAdapter(Context context, List<CodeInfo> data) {
		super(context, data);
		db = DbUtils.create(context);
		db.configAllowTransaction(false);
		db.configDebug(true);
	}

	@Override
	public int getItemResource() {
		return R.layout.item_listview_codeinfo;
	}

	@Override
	public View getItemView(int position, View convertView,SimpleBaseAdapter<CodeInfo>.ViewHolder holder) {
		final TextView title,description,group,time,isCollect;

		title=holder.getView(R.id.item_codeinfo_title);
		description=holder.getView(R.id.item_codeinfo_description);
		group=holder.getView(R.id.item_codeinfo_group);
		time=holder.getView(R.id.item_codeinfo_time);
		isCollect=holder.getView(R.id.item_codeinfo_collect);
		final SimpleDraweeView simpleDraweeView=(SimpleDraweeView)holder.getView(R.id.item_codeinfo_sdv);
		
		final CodeInfo codeInfo=(CodeInfo) getItem(position);
		
		Uri uri = Uri.parse(codeInfo.getPhotoUrl());
		ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
			@Override
			public void onFinalImageSet(String id,@Nullable ImageInfo imageInfo,@Nullable Animatable anim) {
				simpleDraweeView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
				simpleDraweeView.setAspectRatio((float) imageInfo.getWidth() / imageInfo.getHeight());
			}

			@Override
			public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
				simpleDraweeView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
				simpleDraweeView.setAspectRatio((float) imageInfo.getWidth() / imageInfo.getHeight());
			}

			@Override
			public void onFailure(String id, Throwable throwable) {
			}
		};
		DraweeController controller = Fresco.newDraweeControllerBuilder()
				.setControllerListener(controllerListener)
				.setUri(uri)
			    .setAutoPlayAnimations(true)
			    .build();
		simpleDraweeView.setController(controller);
		
		title.setText(codeInfo.getName());
		description.setText(codeInfo.getContent());
		group.setText(codeInfo.getCodeGroup());
		time.setText(codeInfo.getTime());

		if (codeInfo.getIsCollect()>0){
			isCollect.setText("已收藏");
			isCollect.setTextColor(context.getResources().getColor(R.color.main_color));
		}else {
			isCollect.setText("未收藏");
			isCollect.setTextColor(context.getResources().getColor(R.color.red));
		}

		isCollect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//已经收藏了
				if (codeInfo.getIsCollect()>0){
					codeInfo.setIsCollect(0);
					try {
						db.update(codeInfo);
						db.delete(CodeCollect.class, WhereBuilder.b("codeId", "=", codeInfo.getId()));
						isCollect.setText("未收藏");
						isCollect.setTextColor(context.getResources().getColor(R.color.red));
					} catch (DbException e) {
						e.printStackTrace();
					}
				}else {//还没收藏呢
					codeInfo.setIsCollect(1);
					CodeCollect codeCollect=new CodeCollect();
					codeCollect.setCodeId(codeInfo.getId());
//					codeCollect.setSystemtime(String.valueOf(System.currentTimeMillis()));
					try {
						db.update(codeInfo);
						db.save(codeCollect);
						isCollect.setText("已收藏");
						isCollect.setTextColor(context.getResources().getColor(R.color.main_color));
					} catch (DbException e) {
						e.printStackTrace();
					}
				}
			}
		});

		return convertView;
	}
}
