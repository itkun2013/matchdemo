package com.example.shinelon.myapplication;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.shinelon.myapplication.bean.DataBean;

import java.util.List;

/**
 * 作者：Shinelon on 2018/4/6 10:35
 * 作用：
 */
public class MainAdapter extends BaseAdapter {
    private  Context context;
    private List<DataBean.ResultsBean> list;

    public MainAdapter(Context context, List<DataBean.ResultsBean> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null){
            view = View.inflate(context, R.layout.list_item,null);
        }

        TextView text = view.findViewById(R.id.list_item_text);
        text.setText(list.get(i).getDesc());

        return view;
    }
}
