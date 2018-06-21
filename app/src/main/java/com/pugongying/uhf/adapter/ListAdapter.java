package com.pugongying.uhf.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pugongying.uhf.R;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.V> {

    private Context context;
    private List<JSONObject> datas;

    public ListAdapter(Context context, List<JSONObject> datas) {
        this.context = context;
        this.datas = datas;
    }

    @NonNull
    @Override
    public V onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
        return new V(view);
    }

    @Override
    public void onBindViewHolder(@NonNull V holder, int position) {
        if (holder!=null) {
            JSONObject obj = datas.get(position);
            if (obj != null) {
                holder.tv1.setText("编号:");
                holder.tv2.setText("业务员:");
                holder.tv3.setText("客户:");
                holder.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                holder.item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            } else {
                holder.tv1.setText("编号:");
                holder.tv2.setText("业务员:");
                holder.tv3.setText("客户:");
                holder.btn.setOnClickListener(null);
                holder.item.setOnClickListener(null);
            }
        }
    }

    @Override
    public int getItemCount() {
        return datas == null ? 0 : datas.size();
    }

    class V extends RecyclerView.ViewHolder {

        private View item;
        private TextView tv1, tv2, tv3;
        private Button btn;

        public V(View itemView) {
            super(itemView);
            tv1 = itemView.findViewById(R.id.tv1);
            tv2 = itemView.findViewById(R.id.tv2);
            tv3 = itemView.findViewById(R.id.tv3);
            btn = itemView.findViewById(R.id.btn);
            this.item = itemView;
        }
    }
}
