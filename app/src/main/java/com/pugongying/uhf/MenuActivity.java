package com.pugongying.uhf;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pugongying.uhf.util.PrefsUtil;

import cn.trinea.android.common.util.ToastUtils;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tv1;
    private Button btn1, btn2, btn3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        onBindView(savedInstanceState);
        init();
    }

    private void init() {
        NetUtil.NET_ADDRESS = PrefsUtil.get(this, "url", NetUtil.NET_ADDRESS);
    }

    private void onBindView(Bundle savedInstanceState) {
        tv1 = findViewById(R.id.tv1);
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);

        tv1.setOnClickListener(this);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.tv1:
                // 设置界面
                intent = new Intent(this, SettingActivity.class);
                break;
            case R.id.btn1:
                //领样单
                intent = new Intent(this, IndexActivity.class);
                break;
            case R.id.btn2:
                ToastUtils.show(this, "暂未开放");
                break;
            case R.id.btn3:
                intent = new Intent(this, MoveActivity.class);
                break;
        }
        if (intent != null) {
            startActivity(intent);
        }
    }
}
