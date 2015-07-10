package com.xueyu.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.xueyu.bean.CodeGroup;
import com.xueyu.code.R;

/**
 * @author 陈学宇
 * @version 创建时间：2015年5月29日 下午8:03:22
 */
public class GroupAdapter extends SimpleBaseAdapter<CodeGroup> {

	public GroupAdapter(Context context, List<CodeGroup> data) {
		super(context, data);
	}

	@Override
	public int getItemResource() {
		return R.layout.item_listview_group;
	}

	@Override
	public View getItemView(int position, View convertView,SimpleBaseAdapter<CodeGroup>.ViewHolder holder) {
		TextView nameTextView;
		
		nameTextView=holder.getView(R.id.item_group_name);
		
		CodeGroup group=(CodeGroup) getItem(position);
		nameTextView.setText(group.getName()+":"+group.getCount());
		
		return convertView;
	}

}
