package com.pugongying.uhf;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.pugongying.uhf.util.PrefsUtil;
import com.uhf.uhf.Common.Comm;

import java.util.Arrays;
import java.util.List;

public class SettingActivity extends AppCompatActivity {

    private ArrayAdapter<String> adapter;
    private Spinner spinner;
    private EditText et1;
    private TextView tvBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        onBindView(savedInstanceState);
    }

    private void onBindView(Bundle savedInstanceState) {
        et1 = findViewById(R.id.et1);
        tvBack = findViewById(R.id.tv_back);
        spinner = findViewById(R.id.spinner);

        et1.setText(PrefsUtil.get(this, "url", NetUtil.NET_ADDRESS));
        et1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                final String url = editable.toString().trim();
                PrefsUtil.set(SettingActivity.this, "url", url);
                NetUtil.NET_ADDRESS = url;
            }
        });
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Comm.spipow);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(Integer.valueOf(PrefsUtil.get(this, "power", "25")));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                PrefsUtil.set(getApplicationContext(), "power", spinner.getSelectedItemPosition() + "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

}
