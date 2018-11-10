package com.pugongying.uhf.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.pugongying.uhf.CodeUtil;
import com.pugongying.uhf.MessageEvent;
import com.pugongying.uhf.R;
import com.pugongying.uhf.ScanEntity;
import com.uhf.uhf.Common.InventoryBuffer;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class RFIDAdapter extends RecyclerView.Adapter<RFIDAdapter.V> {

    private List<ScanEntity> datas;
    private LayoutInflater inflater;

    public RFIDAdapter(Context context, List<ScanEntity> datas) {
        this.datas = datas;
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public V onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_detail, parent, false);
        return new V(view);
    }

    @Override
    public void onBindViewHolder(@NonNull V holder, int position) {
        final ScanEntity oo = datas.get(position);
        final InventoryBuffer.InventoryTagMap item = datas.get(position).getMap();
        final JSONObject obj = datas.get(position).getObj();
        if (item != null) {
            if ( oo.isShow()) {
                if (obj != null) {
                    holder.tv3.setText((position + 1) + "." + "条码:" + obj.getString("条码"));
                    holder.tv4.setText("产品号:" + obj.getString("产品号"));
                    holder.tv5.setText("品名:" + obj.getString("品名"));
                    holder.tv6.setText("规格:" + obj.getString("规格"));
                    holder.rl1.setVisibility(View.GONE);
                    holder.rl2.setVisibility(View.VISIBLE);
                } else {
                    holder.tv3.setText((position + 1) + "." + "条码:");
                    holder.tv4.setText("产品号:");
                    holder.tv5.setText("品名:");
                    holder.tv6.setText("规格:");
                    holder.rl1.setVisibility(View.GONE);
                    holder.rl2.setVisibility(View.VISIBLE);
                }
            } else {
                holder.tv1.setText((position + 1) + ".");
                holder.tv2.setText(CodeUtil.getDecodeStr(item));
                holder.rl1.setVisibility(View.VISIBLE);
                holder.rl2.setVisibility(View.GONE);
            }
            holder.btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    datas.remove(oo);
                    notifyDataSetChanged();
                    EventBus.getDefault().post(new MessageEvent(0x33, oo.getMap()));
                }
            });
            holder.btn1.setVisibility(View.VISIBLE);
        } else {
            holder.tv1.setText("");
            holder.tv2.setText("");
            holder.tv3.setText((position + 1) + "." +"条码:");
            holder.tv4.setText("产品号:");
            holder.tv5.setText("品名:");
            holder.tv6.setText("规格:");
            holder.btn1.setOnClickListener(null);
            holder.btn1.setVisibility(View.GONE);
            holder.rl1.setVisibility(View.GONE);
            holder.rl2.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return this.datas == null ? 0 : this.datas.size();
    }

    class V extends RecyclerView.ViewHolder {

        View item;
        RelativeLayout rl1, rl2;
        TextView tv1, tv2, tv3, tv4, tv5, tv6;
        Button btn1;

        public V(View itemView) {
            super(itemView);
            this.item = itemView;
            this.tv1 = itemView.findViewById(R.id.tv1);
            this.tv2 = itemView.findViewById(R.id.tv2);
            this.tv3 = itemView.findViewById(R.id.tv3);
            this.tv4 = itemView.findViewById(R.id.tv4);
            this.tv5 = itemView.findViewById(R.id.tv5);
            this.tv6 = itemView.findViewById(R.id.tv6);
            this.rl1 = itemView.findViewById(R.id.rl1);
            this.rl2 = itemView.findViewById(R.id.rl2);
            this.btn1 = itemView.findViewById(R.id.btn1);
        }
    }
}
