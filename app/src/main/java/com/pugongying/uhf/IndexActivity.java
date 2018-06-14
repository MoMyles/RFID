package com.pugongying.uhf;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class IndexActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBtnSearch, mBtnScan;
    private RecyclerView rvList;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this, 0x0090ff);

        setContentView(R.layout.activity_index);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        mBtnScan = findViewById(R.id.btn_scan);
        mBtnSearch = findViewById(R.id.btn_search);

        mBtnSearch.setOnClickListener(this);
        mBtnScan.setOnClickListener(this);

        rvList = findViewById(R.id.rv_list);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_scan:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_search:
                break;
        }
    }


    @Subscribe
    public void onEvent(MessageEvent messageEvent) {
        if (messageEvent == null) return;
        switch (messageEvent.getType()) {

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
