package com.pugongying.uhf;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pugongying.uhf.adapter.MoveAdapter;
import com.pugongying.uhf.util.KeyboardUtil;
import com.pugongying.uhf.util.PrefsUtil;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.uhf.uhf.Common.Comm;
import com.uhf.uhf.Common.InventoryBuffer;
import com.uhf.uhf.UHF1.UHF1Function.AndroidWakeLock;
import com.uhf.uhf.UHF1.UHF1Function.SPconfig;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.trinea.android.common.util.ToastUtils;

import static com.uhf.uhf.Common.Comm.Awl;
import static com.uhf.uhf.Common.Comm.checkDevice;
import static com.uhf.uhf.Common.Comm.context;
import static com.uhf.uhf.Common.Comm.isrun;
import static com.uhf.uhf.Common.Comm.lsTagList;
import static com.uhf.uhf.Common.Comm.myapp;
import static com.uhf.uhf.Common.Comm.operateType.nullOperate;
import static com.uhf.uhf.Common.Comm.soundPool;
import static com.uhf.uhf.Common.Comm.tagListSize;


//import com.supoin.wireless.WirelessManager;
public class MoveActivity extends AppCompatActivity { // ActionBarActivity

    // 读线程：
    public static final String SCN_CUST_ACTION_SCODE = "com.android.server.scannerservice.broadcast";
    public static final String SCN_CUST_EX_SCODE = "scannerdata";
    /* defined by MEXXEN */
    public static final String SCN_CUST_ACTION_START = "android.intent.action.SCANNER_BUTTON_DOWN";
    public static final String SCN_CUST_ACTION_CANCEL = "android.intent.action.SCANNER_BUTTON_UP";

    private TextView tv_back, tv_complete;
    private Button btn_scan, btn_clear, btn_search2;
    private EditText et_kw, et_tm;
    private RecyclerView rv_list;
    private final List<JSONObject> dataList = new ArrayList();


    private MoveAdapter adapter;
    private boolean isRFID = false;// 是否为电子扫描状态 默认红外扫描
    private boolean isScaning = false;// 是否在进行高频率识别
    private boolean isShow = false;
    private int scanIndex = 0;
    private int luanmaCount = 0;

    int scanCode = 0;


    @Override
    protected void onStart() {
        super.onStart();
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                InitDevice();
////                pd.dismiss();
//            }
//        }, 600);
    }


    @Override
    public void onStop() {
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
        if (isrun) {
            closeRFID();
        }
//        release();
    }


    @SuppressLint("HandlerLeak")
    public Handler uhfhandler = new Handler() {
        @SuppressWarnings({"unchecked", "unused"})
        @Override
        public void handleMessage(Message msg) {
            try {
                tagListSize = lsTagList.size();
                Bundle bd = msg.getData();
                showlist();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    // 设备连接电子扫描硬件消息结果处理
    @SuppressLint("HandlerLeak")
    public Handler connectH = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                Comm.mInventoryHandler = uhfhandler;

                Bundle bd = msg.getData();
                String strMsg = bd.get("Msg").toString();
                if (!TextUtils.isEmpty(strMsg)) {
                    Comm.SetInventoryTid(false);
                }

                //                 设置功率
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Comm.opeT = Comm.operateType.setPower;
                            String powerValueStr = PrefsUtil.get(MoveActivity.this, "power", "25");
                            Comm.setAntPower(Integer.parseInt(powerValueStr), 0, 0, 0);
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(),
                                    "功率设置异常:" + e.getMessage(), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                }, 600);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("connectH", e.getMessage().toString());
            }
            if(pd != null && pd.isShowing()){
                pd.dismiss();
            }
        }
    };

    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        QMUIStatusBarHelper.translucent(this);

        setContentView(R.layout.activity_move);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        btn_scan = findViewById(R.id.btn_scan);// 开启电子标签按钮
        btn_clear = findViewById(R.id.btn_clear);// 清空按钮
        btn_search2 = findViewById(R.id.btn_search2);// 确定按钮
        tv_back = findViewById(R.id.tv_back);// 返回按钮
        tv_complete = findViewById(R.id.tv_complete);// 右上角确定按钮
        et_kw = findViewById(R.id.et_kw);
        et_tm = findViewById(R.id.et_tm);
        // 返回按钮点击事件
        tv_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isScaning) {
                    ToastUtils.show(getApplicationContext(), "正在操作, 无法返回");
                } else {
                    finish();
                }
            }
        });
        // 右上角确定按钮点击事件
        tv_complete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isScaning) {
                    Toast.makeText(getApplicationContext(), "请先关闭电子扫描", Toast.LENGTH_SHORT).show();
                } else {
                    final View view = getLayoutInflater().inflate(R.layout.dialog_move_new_kw, null);
                    final EditText etKw = view.findViewById(R.id.et);
                    final Button btn = view.findViewById(R.id.btn);
                    final Button btn2 = view.findViewById(R.id.btn2);
                    final AlertDialog ad = new AlertDialog.Builder(MoveActivity.this)
                            .setView(view)
                            .setCancelable(false)
                            .create();
                    btn2.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ad.dismiss();
                        }
                    });
                    btn.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final String kw = etKw.getText().toString().trim();
                            if (TextUtils.isEmpty(kw)) {
                                ToastUtils.show(getApplicationContext(), "新库位不能为空");
                                return;
                            }
                            // 库位更新
                            Map<String, Object> param = new HashMap<>();
                            param.put("positionNo", kw);
                            param.put("P_Code", getData());
                            final LoadingDialog dialog = LoadingDialog.newInstance();
                            dialog.show(getSupportFragmentManager(), "save");
                            new NetUtil.NetTask().listen(new NetUtil.NetListener() {
                                @Override
                                public void success(String data) {
                                    //[{"succ":"-1","msg":"只能在新制状态下修改"}]
                                    JSONArray array = JSON.parseArray(data);
                                    if (array != null && array.size() > 0) {
                                        JSONObject obj = array.getJSONObject(0);
                                        if (obj != null) {
                                            if (!"fail".equals(obj.getString("status"))) {
                                                ToastUtils.show(getApplicationContext(), "移位成功");
                                                ad.dismiss();
                                                finish();
                                            } else {
                                                ToastUtils.show(getApplicationContext(), obj.getString("reason"));
                                            }
                                        } else {
                                            ToastUtils.show(getApplicationContext(), "服务器异常, 请联系管理员");
                                        }
                                    } else {
                                        ToastUtils.show(getApplicationContext(), "服务器异常, 请联系管理员");
                                    }
                                    if (dialog.isVisible()) {
                                        dialog.dismiss();
                                    }
                                }

                                @Override
                                public void failure() {
                                    if (dialog.isVisible()) {
                                        dialog.dismiss();
                                    }
                                    ad.dismiss();
                                    ToastUtils.show(getApplicationContext(), "服务器异常, 请联系管理员");
                                }
                            }).execute("movePosition", param);
                        }
                    });
                    ad.show();
                }
            }
        });
        // 确定按钮事件
        btn_search2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                doSearch();
            }
        });

        rv_list = findViewById(R.id.rv_list);
        rv_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rv_list.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        if (!lsTagList.isEmpty()) {
            lsTagList.clear();
        }
        adapter = new MoveAdapter(this, dataList);
        rv_list.setAdapter(adapter);
//        tv_state = (TextView) findViewById(R.id.textView_invstate);
//        textView_time = (TextView) findViewById(R.id.textView_time);
//        textView_time.setText("00:00:00");

//        button_set.setFocusable(false);
//        tv_state.setText("模块初始化失败");
        Awl = new AndroidWakeLock((PowerManager) getSystemService(Context.POWER_SERVICE));

        btn_scan.setOnClickListener(new OnClickListener() {
            @SuppressWarnings("unused")
            @Override
            public void onClick(View arg0) {
                if (isRFID) {//1.如果是高频识别
                    isRFID = false;
                    btn_scan.setText("开启电子标签");
                    if (isScaning) {
                        // 2.正在进行 则关闭
                        closeRFID();
                    }
                } else {
                    isRFID = true;
                    btn_scan.setText("关闭电子标签");
                }
            }
        });

        btn_clear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isScaning) {
                    ToastUtils.show(getApplicationContext(), "扫描中，请先关闭电子标签!");
                    return;
                }

                dataList.clear();
                lsTagList.clear();
                tagListSize = 0;
                luanmaCount = 0;
                adapter.notifyDataSetChanged();
            }
        });

        // Register receiver
        IntentFilter intentFilter = new IntentFilter(SCN_CUST_ACTION_SCODE);
        registerReceiver(mSamDataReceiver, intentFilter);

        pd = new ProgressDialog(MoveActivity.this);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("初始化中......");
        pd.show();
    }

    private void doSearch() {
        et_kw.clearFocus();
        et_tm.clearFocus();
        KeyboardUtil.closeKeyboard(MoveActivity.this);
        if (isScaning) {
            ToastUtils.show(getApplicationContext(), "请先关闭电子扫描");
            return;
        }
        Map<String, String> param = new HashMap<>();
        final String positionNo = et_kw.getText().toString().trim();
        final String barCode = et_tm.getText().toString().trim();
        param.put("positionNo", positionNo);
        param.put("barCode", barCode);

        new NetUtil.NetTask().listen(new NetUtil.NetListener() {
            @Override
            public void success(String data) {
                //[{"UserCode":"001 ","Name":"admin ","Depart":"总经办","chnCorpName":"绍兴极绎外贸有限公司"
                // ,"engCorpName":"SHAOXING GE","LoginKey":"F9A8926C7AA146BAA67EFE8BFE947941"}]
                try {
                    JSONArray array = JSON.parseArray(data);
                    if (array != null && !array.isEmpty()) {
                        dataList.clear();
                        for (int i = 0; i < array.size(); i++) {
                            dataList.add(array.getJSONObject(i));
                        }
                        adapter.notifyDataSetChanged();

                    } else {
                        ToastUtils.show(getApplicationContext(), "未查询到符合条件的数据");
                    }
                } catch (Exception e) {
                    ToastUtils.show(getApplicationContext(), "未查询到符合条件的数据");
                }
                et_kw.setText("");
                et_tm.setText("");
            }

            @Override
            public void failure() {

                ToastUtils.show(getApplicationContext(), "服务器异常, 请联系管理员");
            }
        }).execute("getProductByPosition", param);
    }

    private void startRFID() {
        // 开启扫描
        try {

            dataList.clear();
            lsTagList.clear();
            tagListSize = 0;
            luanmaCount = 0;
            Awl.WakeLock();
            Comm.startScan();
            isScaning = true;

        } catch (Exception ex) {
            Toast.makeText(MoveActivity.this,
                    "ERR：" + ex.getMessage(), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void closeRFID() {
        // 扫描中 则关闭
        Awl.ReleaseWakeLock();
        Comm.stopScan();
        showlist();

        isScaning = false;

    }

    /**
     * 红外扫描结果接收广播
     */
    private BroadcastReceiver mSamDataReceiver = new BroadcastReceiver() {
        private MediaPlayer mp;
        private MediaPlayer mp2;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isRFID) {// true 则为红外扫描
                if (mp == null) {
                    mp = MediaPlayer.create(MoveActivity.this, R.raw.beep2);
                }
                if (mp2 == null) {
                    mp2 = MediaPlayer.create(MoveActivity.this, R.raw.beep);
                }
                if (intent.getAction().equals(SCN_CUST_ACTION_SCODE)) {
                    String message;
                    try {
                        message = intent.getStringExtra(SCN_CUST_EX_SCODE).toString();
//                        if (!isExists(message)) {
                        Toast.makeText(MoveActivity.this, "扫描成功：" + message, Toast.LENGTH_SHORT).show();
                        if (mp != null) {
                            mp.start();
                        }
                        et_tm.setText(message);
                        doSearch();
//                        } else {
//                            if (mp2 != null) {
//                                mp2.start();
//                            }
//                            Toast.makeText(MoveActivity.this, "该条码" + message + "已扫描，无需重复扫描", Toast.LENGTH_SHORT).show();
//                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                ToastUtils.show(getApplicationContext(), "当前为电子标签模式，请切换模式");
            }
        }
    };

    private String getData() {
        StringBuffer sb = new StringBuffer();
        for (JSONObject obj : dataList) {
            //                    if (!"".equals(sb.toString())){
            //                        sb.append("◆");
            //                    }
            sb.append(obj.getString("条码") + "◆");
        }
        return sb.toString();
    }

    private boolean isExists(String message) {
        if (!lsTagList.isEmpty()) {
            Iterator<InventoryBuffer.InventoryTagMap> iterator = lsTagList.iterator();
            while (iterator.hasNext()) {
                InventoryBuffer.InventoryTagMap ibitm = iterator.next();
                if (CodeUtil.getDecodeStr(ibitm).equals(message)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mSamDataReceiver);
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }


    public void InitDevice() {
        Comm.repeatSound = false;
//        Comm.rfidSleep = 10;
        Comm.app = getApplication();
        Comm.spConfig = new SPconfig(this);

        context = MoveActivity.this;
        soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        soundPool.load(this, R.raw.beep51, 1);

        checkDevice();

        Comm.initWireless(Comm.app);
        Comm.connecthandler = connectH;
        Comm.mOtherHandler = opeHandler;
        Comm.Connect();
    }

    @SuppressLint("HandlerLeak")
    private Handler opeHandler = new Handler() {
        @SuppressWarnings({"unchecked", "unused"})
        @Override
        public void handleMessage(Message msg) {
            try {
                Bundle b;
                switch (Comm.opeT) {
                    case setPower:
                        b = msg.getData();
                        boolean isSetPower = b.getBoolean("isSetPower");
                        if (isSetPower)
                            Toast.makeText(getApplicationContext(),
                                    "功率设置成功", Toast.LENGTH_SHORT)
                                    .show();
                        else
                            Toast.makeText(getApplicationContext(),
                                    "功率设置失败", Toast.LENGTH_SHORT)
                                    .show();
                        if (Comm.setParameters()) {
                            Comm.isQuick = false;
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Comm.opeT = nullOperate;
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
        }
    };

    /**
     * 删除操作,若删除的为高频识别出来的，进行同步删除操作
     *
     * @param messageEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MessageEvent messageEvent) {
        switch (messageEvent.getType()) {
            case 0x34:
                lsTagList.remove(messageEvent.getData());
                tagListSize = lsTagList.size();
                break;
            case 0x35:
                showlist();
                break;
        }
    }

    /**
     * 判断列表中是否已存在该条码
     *
     * @param map
     * @return
     */
    private boolean isExists(InventoryBuffer.InventoryTagMap map) {
//        for (ScanEntity se : dataList) {
//            if (se.getMap().strEPC.equals(map.strEPC)) {
//                return true;
//            }
//        }
        for (JSONObject data : dataList) {
            if (data.getString("条码").equals(CodeUtil.getDecodeStr(map))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 扫描结果显示操作
     */
    private void showlist() {
        if (tagListSize > 0) {
            Pattern pattern = Pattern.compile("^[0-9A-Za-z]+\\s*$");
            for (int i = 0; i < tagListSize; i++) {
                InventoryBuffer.InventoryTagMap map = lsTagList.get(i);
                if (!isExists(map)) {
                    String decode = CodeUtil.getDecodeStr(map);
                    Matcher matcher = pattern.matcher(decode);
                    if (matcher.find()) {
                        JSONObject obj = new JSONObject();
                        obj.put("条码", decode);
                        obj.put("item", map);
                        dataList.add(obj);
                    } else {
                        luanmaCount++;
                    }
                }
            }
        } else {
            luanmaCount = 0;
        }

//        if (isQuick && !Comm.isrun) {
//                tv_state.setText(String.valueOf("正在处理数据，请稍后。。。"));
//        }
        adapter.notifyDataSetChanged();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                rv_list.scrollToPosition(adapter.getItemCount() - 1);
            }
        }, 400);

    }

    /**
     * 物理按键按下
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - myapp.exittime) > 2000) {
                Toast.makeText(getApplicationContext(), "Touch again to exit",
                        Toast.LENGTH_SHORT).show();
                myapp.exittime = System.currentTimeMillis();
            } else {
                release();
                //finish();
                System.exit(0);
            }
            return true;
        }
        scanCode = event.getScanCode();
        if (scanCode == 261 && isRFID && !isrun) {
            startRFID();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 物理按键释放
     *
     * @param keyCode
     * @param event
     * @return
     */
    /* 释放按键事件 */
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        scanCode = event.getScanCode();
//        if (scanCode == 261 && isRFID && !isrun)
//            startRFID();
        if (scanCode == 261 && isRFID && isrun)
            closeRFID();
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        try {
//            scanCode = e.getScanCode();
//            Log.d("UHF","getScanCode "+scanCode);
            return super.dispatchKeyEvent(e);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void release() {
        try {
            if (isrun)
                Comm.stopScan();
            Comm.powerDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
