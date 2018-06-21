package com.pugongying.uhf;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.pugongying.uhf.adapter.ListAdapter;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListActivity extends AppCompatActivity {
    private RecyclerView rvList;
    private ListAdapter adapter;
    private final List<JSONObject> datas = new ArrayList<>();

    private NetUtil.NetTask task;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this);
        setContentView(R.layout.activity_list);


        rvList = findViewById(R.id.rv_list);
        rvList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvList.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        adapter = new ListAdapter(this, datas);
        rvList.setAdapter(adapter);

        task = new NetUtil.NetTask().listen(new NetUtil.NetListener() {
            @Override
            public void success(String data) {
                try {

                } catch (Exception e) {
                    failure();
                }
            }

            @Override
            public void failure() {
                Toast.makeText(ListActivity.this, "领样单列表获取失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void doRequest() {
        Map<String, String> params = new HashMap<>();
        params.put("DATE", getIntent().getStringExtra("date"));
        params.put("Status", getIntent().getStringExtra("status"));
        task.execute("Smp_applyoutlist_RFID", params);
    }
}
