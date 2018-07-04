package com.pugongying.uhf.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.pugongying.uhf.MainActivity;
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
    public void onBindViewHolder(@NonNull final V holder, int position) {
        //[{"申请编码":"0000000002","客户":"PANASH","申请人":"admin","领用数量":0.00,"状态":"未扫描"}]
        if (holder != null) {
            final JSONObject obj = datas.get(position);
            if (obj != null) {
                holder.tv1.setText("编号:" + obj.getString("申请编码"));
                holder.tv2.setText("申请人:" + obj.getString("申请人"));
                holder.tv3.setText("客户:" + obj.getString("客户"));
                holder.tv4.setText("领用数量:" + obj.getString("领用数量"));
                String status = obj.getString("状态");
                holder.tv5.setText(status);
                if ("未扫描".equals(status)) {
                    holder.tv5.setTextColor(Color.RED);
                } else {
                    holder.tv5.setTextColor(Color.GREEN);
                }
                holder.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, MainActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("data", obj);
                        intent.putExtras(bundle);
                        context.startActivity(intent);
                    }
                });
                holder.item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (obj.getBooleanValue("expand")) {
                            holder.btn.setVisibility(View.GONE);
                            obj.put("expand", false);
                        } else {
                            holder.btn.setVisibility(View.VISIBLE);
                            obj.put("expand", true);
                        }
                    }
                });
            } else {
                holder.tv1.setText("编号:");
                holder.tv2.setText("申请人:");
                holder.tv3.setText("客户:");
                holder.tv4.setText("领用数量:");
                holder.tv5.setText("");
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
        private TextView tv1, tv2, tv3, tv4, tv5;
        private Button btn;

        public V(View itemView) {
            super(itemView);
            tv1 = itemView.findViewById(R.id.tv1);
            tv2 = itemView.findViewById(R.id.tv2);
            tv3 = itemView.findViewById(R.id.tv3);
            tv4 = itemView.findViewById(R.id.tv4);
            tv5 = itemView.findViewById(R.id.tv5);
            btn = itemView.findViewById(R.id.btn);
            this.item = itemView;
        }
    }
}
