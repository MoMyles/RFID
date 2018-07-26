package com.pugongying.uhf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.trinea.android.common.util.ToastUtils;

public class LoginActivity extends AppCompatActivity {

    private TextView tvSetting;
    private Button btn1;
    private EditText et1, et2;
    private LoadingDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        dialog = LoadingDialog.newInstance();
        tvSetting = findViewById(R.id.tv_setting);
        btn1 = findViewById(R.id.btn1);
        et1 = findViewById(R.id.et1);
        et2 = findViewById(R.id.et2);


        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> param = new HashMap<>();
                final String name = et1.getText().toString().trim();
                final String pass = et2.getText().toString().trim();
                param.put("Name", name);
                param.put("PassWord", pass);
                dialog.show(getSupportFragmentManager(), "login");
                new NetUtil.NetTask().listen(new NetUtil.NetListener() {
                    @Override
                    public void success(String data) {
                        //[{"UserCode":"001 ","Name":"admin ","Depart":"总经办","chnCorpName":"绍兴极绎外贸有限公司"
                        // ,"engCorpName":"SHAOXING GE","LoginKey":"F9A8926C7AA146BAA67EFE8BFE947941"}]
                        JSONArray array = JSON.parseArray(data);
                        if (array != null && !array.isEmpty()) {
                            JSONObject obj = array.getJSONObject(0);
                            if (obj != null) {
                                SharedPreferences sp = getSharedPreferences("pgy_rfid", MODE_PRIVATE);
                                sp.edit().putString("UserCode", obj.getString("UserCode")).apply();
                                startActivity(new Intent(LoginActivity.this, IndexActivity.class));
                            } else {
                                ToastUtils.show(getApplicationContext(), "服务器异常, 请联系管理员");
                            }
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void failure() {
                        dialog.dismiss();
                        ToastUtils.show(getApplicationContext(), "服务器异常, 请联系管理员");
                    }
                }).execute("Login", param);
            }
        });

        tvSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, SettingActivity.class);
                startActivity(i);
            }
        });
    }
}
