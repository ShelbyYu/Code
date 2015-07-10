package com.xueyu.adapter;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.lidroid.xutils.util.LogUtils;
import com.xueyu.bean.Collection;
import com.xueyu.code.R;
import com.xueyu.util.ScreenTools;

import java.util.List;

/**
 * Created by Shey on 2015/6/21 00:35.
 * Email:1768037936@qq.com
 */
public class CollectAdapter extends SimpleBaseAdapter<Collection>{

    private int mChildWidth;
    private ScreenTools screenTools;
    public CollectAdapter(Context context, List<Collection> data) {
        super(context, data);
        screenTools=ScreenTools.instance(context);
        mChildWidth = (screenTools.getScreenWidth()-screenTools.dip2px(10)*3)/2;
    }

    @Override
    public int getItemResource() {
        return R.layout.item_gridview_collect;
    }

    @Override
    public View getItemView(int position, View convertView, ViewHolder holder) {

        TextView name;

        name=holder.getView(R.id.item_collect_name);
        final SimpleDraweeView simpleDraweeView=(SimpleDraweeView)holder.getView(R.id.item_collect_sdv);

        final Collection collection=(Collection)getItem(position);

        name.setText(collection.getCodeInfo().getName());

        if (!TextUtils.isEmpty(collection.getCodeInfo().getPhotoUrl())) {
            Uri uri = Uri.parse(collection.getCodeInfo().getPhotoUrl());
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
        }

        return convertView;
    }
}
