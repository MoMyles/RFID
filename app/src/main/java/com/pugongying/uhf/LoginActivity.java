package com.pugongying.uhf;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {


    private Button btn1;
    private EditText et1, et2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);


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
                new NetUtil.NetTask().listen(new NetUtil.NetListener() {
                    @Override
                    public void success(String data) {

                    }

                    @Override
                    public void failure() {

                    }
                }).execute("Login", param);
            }
        });
    }
}
