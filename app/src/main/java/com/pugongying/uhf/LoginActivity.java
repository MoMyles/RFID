package com.pugongying.uhf;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {


    private Button btn1;
    private EditText et1, et2;
    private LoadingDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);


        dialog = LoadingDialog.newInstance();
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
                        JSONArray array = JSON.parseArray(data);
                        if (array != null && !array.isEmpty()) {
                            JSONObject obj = array.getJSONObject(0);
                            if (obj != null) {
                                int succ = obj.getIntValue("succ");
                                if (succ == 1) {
                                    startActivity(new Intent(LoginActivity.this, IndexActivity.class));
                                } else {
                                    Toast.makeText(getApplicationContext(), obj.getString("msg"), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void failure() {
                        dialog.dismiss();
                    }
                }).execute("Login", param);
            }
        });
    }
}
