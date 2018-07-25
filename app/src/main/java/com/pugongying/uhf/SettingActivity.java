package com.pugongying.uhf;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.pugongying.uhf.util.PrefsUtil;

import java.util.Arrays;
import java.util.List;

public class SettingActivity extends AppCompatActivity {

    private static final String[] SPIPOW = {"500", "600", "700", "800", "900", "1000", "1100",
            "1200", "1300", "1400", "1500", "1600", "1700", "1800", "1900",
            "2000", "2100", "2200", "2300", "2400", "2500", "2600", "2700",
            "2800", "2900", "3000"};

    private ArrayAdapter<String> adapter;
    private List<String> dataList;
    private Spinner spinner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        onBindView(savedInstanceState);
        init();
    }

    private void init() {
        dataList = Arrays.asList(SPIPOW);
        adapter = new ArrayAdapter<>(this, R.layout.item_spinner, R.id.tv_name, dataList);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        int position = adapter.getPosition(PrefsUtil.get(this, "power", "1500"));
        spinner.setAdapter(adapter);
        spinner.setSelection(position);
        spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getAdapter().getItem(position);
                PrefsUtil.set(getApplicationContext(), "power", item);
            }
        });
    }

    private void onBindView(Bundle savedInstanceState) {
        spinner = findViewById(R.id.spinner);
    }

}
