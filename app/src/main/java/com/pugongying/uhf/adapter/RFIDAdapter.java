package com.pugongying.uhf.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.pugongying.uhf.R;
import com.uhf.uhf.Common.InventoryBuffer;

import java.util.List;

public class RFIDAdapter extends RecyclerView.Adapter<RFIDAdapter.V> {

    private List<InventoryBuffer.InventoryTagMap> datas;
    private LayoutInflater inflater;

    public RFIDAdapter(Context context, List<InventoryBuffer.InventoryTagMap> datas) {
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
        final InventoryBuffer.InventoryTagMap item = datas.get(position);
        if (item != null) {
            holder.tv1.setText(item.strTID + ".");
            holder.tv2.setText(item.strEPC.replace(" ", ""));
            holder.btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    datas.remove(item);
                    notifyDataSetChanged();
                }
            });
            holder.btn1.setVisibility(View.VISIBLE);
        } else {
            holder.tv1.setText("");
            holder.tv2.setText("");
            holder.btn1.setOnClickListener(null);
            holder.btn1.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return this.datas == null ? 0 : this.datas.size();
    }

    class V extends RecyclerView.ViewHolder {

        View item;
        TextView tv1, tv2;
        Button btn1;

        public V(View itemView) {
            super(itemView);
            this.item = itemView;
            this.tv1 = itemView.findViewById(R.id.tv1);
            this.tv2 = itemView.findViewById(R.id.tv2);
            this.btn1 = itemView.findViewById(R.id.btn1);
        }
    }
}
