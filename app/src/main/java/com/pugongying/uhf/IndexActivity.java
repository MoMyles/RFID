package com.pugongying.uhf;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class IndexActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBtnSearch;
    private TextView mTvBack, mTvDate, mTvStatus;
    private String dateParam, statusParam;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        QMUIStatusBarHelper.translucent(this);

        setContentView(R.layout.activity_index);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        mTvBack = findViewById(R.id.tv_back);
        mTvBack.setOnClickListener(this);

        mBtnSearch = findViewById(R.id.btn_search);
        mBtnSearch.setOnClickListener(this);

        mTvDate = findViewById(R.id.tv_date);
        mTvDate.setOnClickListener(this);
        mTvStatus = findViewById(R.id.tv_status);
        mTvStatus.setOnClickListener(this);

        getDate(true);
        getStatus(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                finish();
                break;
            case R.id.btn_search:
                doSearch();
                break;
            case R.id.tv_date:
                getDate(false);
                break;
            case R.id.tv_status:
                getStatus(false);
                break;
        }
    }

    private void doSearch() {
        Intent intent = new Intent(this, ListActivity.class);
        intent.putExtra("date", dateParam);
        intent.putExtra("status", statusParam);
        startActivity(intent);
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

    public void getDate(final boolean isFirst) {
        new NetUtil.NetTask().listen(new NetUtil.NetListener() {
            @Override
            public void success(String data) {
                try {
                    final JSONArray dateArray = JSON.parseArray(data);
                    if (dateArray != null && dateArray.size() > 0) {
                        if (!isFirst) {
                            //[{"DateListNo":"0502      ","Name":"今天"},{"DateListNo":"0535      ","Name":"最近1月"},{"DateListNo":"0560      ","Name":"最近7天"},{"DateListNo":"0505      ","Name":"本月"},{"DateListNo":"0515      ","Name":"本周"},{"DateListNo":"0520      ","Name":"上周"},{"DateListNo":"0510      ","Name":"上月"},{"DateListNo":"0503      ","Name":"昨天"},{"DateListNo":"0550      ","Name":"最近3月"},{"DateListNo":"0555      ","Name":"最近6月"},{"DateListNo":"0540      ","Name":"本年"},{"DateListNo":"0545      ","Name":"上年"}]
                            QMUIBottomSheet.BottomListSheetBuilder builder = new QMUIBottomSheet.BottomListSheetBuilder(IndexActivity.this);
                            int size = dateArray.size();
                            for (int i = 0; i < size; i++) {
                                JSONObject obj = dateArray.getJSONObject(i);
                                builder.addItem(obj.getString("Name"));
                            }
                            builder.setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                                @Override
                                public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                                    JSONObject obj = dateArray.getJSONObject(position);
                                    dateParam = obj.getString("DateListNo");
                                    mTvDate.setText(obj.getString("Name"));
                                    dialog.dismiss();
                                }
                            });
                            builder.build().show();
                        } else {
                            JSONObject obj = dateArray.getJSONObject(0);
                            dateParam = obj.getString("DateListNo");
                            mTvDate.setText(obj.getString("Name"));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failure() {

            }
        }).execute("GetDate");
    }

    public void getStatus(final boolean isFirst) {
        new NetUtil.NetTask().listen(new NetUtil.NetListener() {
            @Override
            public void success(String data) {
                try {
                    final JSONArray statusArray = JSON.parseArray(data);
                    if (statusArray != null && statusArray.size() > 0) {
                        //[{"CODE":"N","NAME":"未扫描"},{"CODE":"Y","NAME":"已扫描"}]
                        if (!isFirst) {
                            QMUIBottomSheet.BottomListSheetBuilder builder = new QMUIBottomSheet.BottomListSheetBuilder(IndexActivity.this);
                            int size = statusArray.size();
                            for (int i = 0; i < size; i++) {
                                JSONObject obj = statusArray.getJSONObject(i);
                                builder.addItem(obj.getString("NAME"));
                            }
                            builder.setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                                @Override
                                public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                                    JSONObject obj = statusArray.getJSONObject(position);
                                    statusParam = obj.getString("CODE");
                                    mTvStatus.setText(obj.getString("NAME"));
                                    dialog.dismiss();
                                }
                            });
                            builder.build().show();
                        } else {
                            JSONObject obj = statusArray.getJSONObject(0);
                            statusParam = obj.getString("CODE");
                            mTvStatus.setText(obj.getString("NAME"));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failure() {

            }
        }).execute("GetStatus");
    }
}
