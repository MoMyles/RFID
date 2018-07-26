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

import java.util.Arrays;
import java.util.List;

public class SettingActivity extends AppCompatActivity {

    private static final String[] SPIPOW = new String[]{"500", "600", "700", "800", "900", "1000", "1100",
            "1200", "1300", "1400", "1500", "1600", "1700", "1800", "1900",
            "2000", "2100", "2200", "2300", "2400", "2500", "2600", "2700",
            "2800", "2900", "3000"};

    private ArrayAdapter<String> adapter;
    private List<String> dataList;
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
        dataList = Arrays.asList(SPIPOW);
//        adapter = new ArrayAdapter<>(this, R.layout.item_spinner, R.id.tv_name, dataList);
//        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
//        int position = adapter.getPosition(PrefsUtil.get(this, "power", "1500"));
//        spinner.setAdapter(adapter);
//        spinner.setSelection(position);
//        spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String item = (String) parent.getAdapter().getItem(position);
//                PrefsUtil.set(getApplicationContext(), "power", item);
//            }
//        });
    }

}
