package com.pugongying.uhf;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pugongying.uhf.adapter.ListAdapter;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListActivity extends AppCompatActivity {
    private TextView back;
    private RecyclerView rvList;
    private ListAdapter adapter;
    private final List<JSONObject> datas = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this);
        setContentView(R.layout.activity_list);
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        rvList = findViewById(R.id.rv_list);
        rvList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvList.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        adapter = new ListAdapter(this, datas);
        rvList.setAdapter(adapter);

        doRequest();
    }

    public void doRequest() {
        Map<String, String> params = new HashMap<>();
        params.put("DATE", getIntent().getStringExtra("date"));
        params.put("Status", getIntent().getStringExtra("status"));
        new NetUtil.NetTask().listen(new NetUtil.NetListener() {
            @Override
            public void success(String data) {
                // [{"申请编码":"0000000002","客户":"PANASH","申请人":"admin","领用数量":0.00,"状态":"未扫描"}]
                if (datas != null && !datas.isEmpty()) {
                    datas.clear();
                }
                try {
                    JSONArray arr = JSON.parseArray(data);
                    if (arr != null && !arr.isEmpty()) {
                        int size = arr.size();
                        for (int i = 0; i < size; i++) {
                            JSONObject o = arr.getJSONObject(i);
                            o.put("expand", false);
                            datas.add(o);
                        }
                    }
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    failure();
                }
            }

            @Override
            public void failure() {
                Toast.makeText(ListActivity.this, "领样单列表获取失败", Toast.LENGTH_SHORT).show();
            }
        }).execute("Smp_applyoutlist_RFID", params);
    }
}
