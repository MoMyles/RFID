package com.pugongying.uhf;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pugongying.uhf.adapter.RFIDAdapter;
import com.pugongying.uhf.util.PrefsUtil;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.uhf.uhf.Common.Comm;
import com.uhf.uhf.Common.InventoryBuffer;
import com.uhf.uhf.UHF1.UHF001;
import com.uhf.uhf.UHF1Function.AndroidWakeLock;
import com.uhf.uhf.UHF1Function.SPconfig;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.trinea.android.common.util.ToastUtils;

import static com.uhf.uhf.Common.Comm.Awl;
import static com.uhf.uhf.Common.Comm.checkDevice;
import static com.uhf.uhf.Common.Comm.context;
import static com.uhf.uhf.Common.Comm.isQuick;
import static com.uhf.uhf.Common.Comm.isrun;
import static com.uhf.uhf.Common.Comm.lsTagList;
import static com.uhf.uhf.Common.Comm.myapp;
import static com.uhf.uhf.Common.Comm.rfidOperate;
import static com.uhf.uhf.Common.Comm.soundPool;
import static com.uhf.uhf.Common.Comm.tagListSize;
import static com.uhf.uhf.UHF1.UHF001.UHF1handler;


//import com.supoin.wireless.WirelessManager;
public class MainActivity extends AppCompatActivity { // ActionBarActivity

    // 读线程：
    public static final String SCN_CUST_ACTION_SCODE = "com.android.server.scannerservice.broadcast";
    public static final String SCN_CUST_EX_SCODE = "scannerdata";
    /* defined by MEXXEN */
    public static final String SCN_CUST_ACTION_START = "android.intent.action.SCANNER_BUTTON_DOWN";
    public static final String SCN_CUST_ACTION_CANCEL = "android.intent.action.SCANNER_BUTTON_UP";

    private TextView tv_tags, tv_back, tv_complete;
    private Button btn_scan, btn_clean, btn_detail;
    private RecyclerView rv_list;
    private final List<ScanEntity> dataList = new ArrayList();


    private RFIDAdapter rfidAdapter;
    private boolean isRFID = true;// 默认高频识别
    private boolean isScaning = false;// 是否在进行高频率识别
    private boolean isShow = false;
    private int scanIndex = 0;

    int scanCode = 0;


    @Override
    protected void onStart() {
        super.onStart();
        InitDevice();
    }

    @Override
    public void onStop() {
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
        release();
    }

    @SuppressLint("HandlerLeak")
    public Handler uhfhandler = new Handler() {
        @SuppressWarnings({"unchecked", "unused"})
        @Override
        public void handleMessage(Message msg) {
            try {
                tagListSize = lsTagList.size();
                Bundle bd = msg.getData();

                if (tagListSize > 0) {
                    showlist();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @SuppressLint("HandlerLeak")
    public Handler connectH = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                UHF001.mhandler = uhfhandler;
                if (null != rfidOperate)
                    rfidOperate.mHandler = uhfhandler;
                if (null != Comm.uhf6)
                    Comm.uhf6.UHF6handler = uhfhandler;

                Bundle bd = msg.getData();
                String strMsg = bd.get("Msg").toString();
                if (TextUtils.isEmpty(strMsg)) {
                    Log.e("connectH", "模块初始化失败");
                }
//                if (!TextUtils.isEmpty(strMsg)) {
//                    tv_state.setText(strMsg);
//                } else
//                    tv_state.setText("模块初始化失败");

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("connectH", e.getMessage().toString());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        QMUIStatusBarHelper.translucent(this);

        setContentView(R.layout.activity_detail);

//        button_read = (Button) findViewById(R.id.button_start);
//        button_stop = (Button) findViewById(R.id.button_stop);
//        button_stop.setEnabled(false);
//        button_clean = (Button) findViewById(R.id.button_readclear);
//        button_set = (Button) findViewById(R.id.button_set);
//        listView = (ListView) findViewById(R.id.listView_epclist);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        btn_scan = findViewById(R.id.btn_scan);
        btn_clean = findViewById(R.id.btn_clean);
        btn_detail = findViewById(R.id.btn_detail);
        tv_back = findViewById(R.id.tv_back);
        tv_complete = findViewById(R.id.tv_complete);
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
        tv_complete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isScaning) {
                    Toast.makeText(getApplicationContext(), "请先关闭电子标签", Toast.LENGTH_SHORT).show();
                } else {
                    // 1.16进制转ASCII值
                    // 2.ASCII值在找对应的数字字母
                    Bundle bundle = getIntent().getExtras();
                    if (bundle != null) {
                        //{"申请编码":"0000000002","客户":"PANASH","申请人":"admin","领用数量":0.00,"状态":"未扫描"}
                        // Map<String, Object> data = (Map<String, Object>) bundle.getSerializable("data");
                        String soId = getIntent().getStringExtra("soId");
                        if (!TextUtils.isEmpty(soId)) {
                            //SoID 申请编码
                            //Takeman 联系人
                            //customNo
                            //customName 客户名 客户ID
                            //ContactNumber 联系电话
                            //strDetail 产品号◆产品号◆产品号◆产品号◆产品号
                            //remark 备注
                            Map<String, Object> param = new HashMap<>();
                            SharedPreferences sp = getSharedPreferences("pgy_rfid", MODE_PRIVATE);
                            param.put("UserCode", sp.getString("UserCode", ""));
                            param.put("SoID", soId);
                            param.put("strDetail", getData());
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
                                            if (obj.getIntValue("succ") == 0) {
                                                ToastUtils.show(getApplicationContext(), "保存成功");
                                                finish();
                                            } else {
                                                ToastUtils.show(getApplicationContext(), obj.getString("msg"));
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
                                    ToastUtils.show(getApplicationContext(), "服务器异常, 请联系管理员");
                                }
                            }).execute("Smp_applyoutAdd_RFID", param);

                        } else {
                            Toast.makeText(getApplicationContext(), "此领样单有问题, 请找相关人员确认!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "此领样单有问题, 请找相关人员确认!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        tv_tags = (TextView) findViewById(R.id.textView_readallcnt);
        rv_list = findViewById(R.id.rv_list);
        rv_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rv_list.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        if (!lsTagList.isEmpty()) {
            lsTagList.clear();
        }
        rfidAdapter = new RFIDAdapter(this, dataList);
        rv_list.setAdapter(rfidAdapter);
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
                    btn_scan.setText("关闭红外扫描");
                    if (isScaning) {
                        // 2.正在进行 则关闭
                        closeRFID();
                    }
                } else {
                    isRFID = true;
                    btn_scan.setText("开启红外扫描");
                }
            }
        });

        btn_clean.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isScaning) {
                    ToastUtils.show(getApplicationContext(), "扫描中，请先关闭扫描!");
                    return;
                }
                dataList.clear();
                lsTagList.clear();
                tagListSize = 0;
                rfidAdapter.notifyDataSetChanged();
                tv_tags.setText("合计: 0 个");
            }
        });

        btn_detail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isScaning) {
                    ToastUtils.show(getApplicationContext(), "扫描中，请先关闭扫描!");
                    return;
                }
                if (isShow) {
                    isShow = false;
                    btn_detail.setText("查看详情");
                    for (ScanEntity se : dataList) {
                        se.setShow(false);
                    }
                    rfidAdapter.notifyDataSetChanged();
                } else {
                    isShow = true;
                    btn_detail.setText("取消查看");
                    Map<String, Object> param = new HashMap<>();

                    param.put("barcode", getData());
                    new NetUtil.NetTask().listen(new NetUtil.NetListener() {
                        @Override
                        public void success(String data) {
                            //[{"条码":"0000018923","产品号":"ADK-JQ-3901B        ","品名":"全涤条子 ADK-JQ-3901B","规格":""},{"条码":"0000022066","产品号":"CEY014              "
                            // ,"品名":"200DSSY单面麻","规格":"75D/72FFDY/(30DFDY+30DPOY)×200D/48FSSY"}
                            // ,{"条码":"0000CRP001","产品号":"CRP001              ","品名":"乱麻印花","规格":""},{"条码":"0000CRP002","产品号":"CRP002              ","品名":"乱麻印花","规格":""},{"条码":"0000CRP003","产品号":"CRP003              ","品名":"乱麻印花","规格":""},{"条码":"0000CRP004","产品号":"CRP004              ","品名":"乱麻印花","规格":""},{"条码":"0000CRP006","产品号":"CRP006              ","品名":"乱麻印花","规格":""},{"条码":"0000CRP007","产品号":"CRP007              ","品名":"乱麻印花","规格":""},{"条码":"0000CRP008","产品号":"CRP008              ","品名":"乱麻印花","规格":""}]
                            JSONArray array = JSON.parseArray(data);
                            int size = array == null ? 0 : array.size();
                            for (ScanEntity se : dataList) {
                                for (int i = 0; i < size; i++) {
                                    JSONObject obj = array.getJSONObject(i);
                                    if (CodeUtil.getDecodeStr(se.getMap()).equals(obj.getString("条码"))) {
                                        se.setObj(obj);
                                    }
                                }
                                se.setShow(true);
                            }
                            rfidAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void failure() {
                            ToastUtils.show(getApplicationContext(), "查看详情失败");
                        }
                    }).execute("QueryBarcode", param);
                }
            }
        });

//        button_stop.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                Awl.ReleaseWakeLock();
////                stopTimerTask();
////                tv_state.setText("停止读取");
//                Comm.stopScan();
//                showlist();
//                StopHandleUI();
//            }
//        });

//        button_clean.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                tv_tags.setText("0");
//                tv_state.setText("清空完成,等待操作...");
//                Comm.clean();
//                showlist();
//            }
//        });
        // 设置功率
        try {
            Comm.opeT = Comm.operateType.setPower;
            String powerValueStr = PrefsUtil.get(this, "power", "1500");
            Comm.setAntPower(Integer.valueOf(powerValueStr), 0, 0, 0);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "功率设置异常:" + e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
        }
        // Register receiver
        IntentFilter intentFilter = new IntentFilter(SCN_CUST_ACTION_SCODE);
        registerReceiver(mSamDataReceiver, intentFilter);
//        button_set.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    onTouchButton();
//                } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                    onReleaseButton();
//                }
//                return false;
//            }
//        });
//        this.listView.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
//                                    long arg3) {
//                arg1.setBackgroundColor(Color.YELLOW);
//
//                @SuppressWarnings("unchecked")
//                HashMap<String, String> hm = (HashMap<String, String>) listView
//                        .getItemAtPosition(arg2);
//                String epc = hm.get("EPC ID");
//                myapp.Curepc = epc;
//
//                for (int i = 0; i < listView.getCount(); i++) {
//                    if (i != arg2) {
//                        View v = listView.getChildAt(i);
//                        if (v != null) {
//                            ColorDrawable cd = (ColorDrawable) v
//                                    .getBackground();
//                            if (Color.YELLOW == cd.getColor()) {
//                                int[] colors = {Color.WHITE,
//                                        Color.rgb(219, 238, 244)};// RGB颜色
//                                v.setBackgroundColor(colors[i % 2]);// 每隔item之间颜色不同
//                            }
//                        }
//                    }
//                }
//            }
//
//        });
//        tw.getChildAt(0).setBackgroundColor(Color.parseColor("#FF8C00"));
    }

    private void startRFID() {
        // 开启扫描
        try {
//                        startTimerTask();
//                        button_clean.performClick();
//                        tv_state.setText("开始读取...");
            Awl.WakeLock();
            Comm.startScan();
            isScaning = true;
//            btn_scan.setText("电子标签关闭");
//                        ReadHandleUI();
            Toast.makeText(getApplicationContext(), "开启了高频识别", Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Toast.makeText(MainActivity.this,
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
        Toast.makeText(getApplicationContext(), "关闭了高频识别", Toast.LENGTH_SHORT).show();
//        btn_scan.setText("电子标签开启");
    }

//    private void onTouchButton() {
//        Intent scannerIntent = new Intent(SCN_CUST_ACTION_START);
//        sendBroadcast(scannerIntent);
//    }
//
//    private void onReleaseButton() {
//        Intent scannerIntent = new Intent(SCN_CUST_ACTION_CANCEL);
//        sendBroadcast(scannerIntent);
//    }

    private BroadcastReceiver mSamDataReceiver = new BroadcastReceiver() {
        private MediaPlayer mp;
        private MediaPlayer mp2;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (isRFID) {
                if (isScaning) {
                    closeRFID();
                } else {
                    startRFID();
                }
            } else {
                if (mp == null) {
                    mp = MediaPlayer.create(MainActivity.this, R.raw.beep2);
                }
                if (mp2 == null) {
                    mp2 = MediaPlayer.create(MainActivity.this, R.raw.beep);
                }
                if (intent.getAction().equals(SCN_CUST_ACTION_SCODE)) {
                    String message;
                    try {
                        message = intent.getStringExtra(SCN_CUST_EX_SCODE).toString();
                        if (!isExists(message)) {
                            Toast.makeText(MainActivity.this, "扫描成功：" + message, Toast.LENGTH_SHORT).show();
                            if (mp != null) {
                                mp.start();
                            }
                            InventoryBuffer.InventoryTagMap itm = new InventoryBuffer.InventoryTagMap();
                            itm.strTID = "扫描获得" + scanIndex++;
                            itm.strEPC = message;
                            lsTagList.add(itm);
                            tagListSize = lsTagList.size();
                            tv_tags.setText("合计: " + tagListSize + " 个");
                            showlist();
                        } else {
                            if (mp2 != null) {
                                mp2.start();
                            }
                            Toast.makeText(MainActivity.this, "该条码" + message + "已存在，无需重复扫描", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("TAG_ER", e.toString());
                    }
                }
            }
        }
    };

    private String getData() {
        StringBuffer sb = new StringBuffer();
        for (InventoryBuffer.InventoryTagMap map : lsTagList) {
            //                    if (!"".equals(sb.toString())){
            //                        sb.append("◆");
            //                    }
            sb.append(CodeUtil.getDecodeStr(map) + "◆");
        }
        return sb.toString();
    }

    private boolean isExists(String message) {
        if (!lsTagList.isEmpty()) {
            Iterator<InventoryBuffer.InventoryTagMap> iterator = lsTagList.iterator();
            while (iterator.hasNext()) {
                InventoryBuffer.InventoryTagMap ibitm = iterator.next();
//                Log.e("TAG", CodeUtil.getDecodeStr(ibitm) + "-" + message);
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

    //开始计时
//    public void startTimerTask() {
//        if (null == timer) {
//            if (null == task) {
//                task = new TimerTask() {
//                    @Override
//                    public void run() {
//                        if (null == msg) {
//                            msg = new Message();
//                        } else {
//                            msg = Message.obtain();
//                        }
//                        msg.what = 1;
//                        handlerTimerTask.sendMessage(msg);
//                    }
//                };
//            }
//            timer = new Timer(true);
//            timer.schedule(task, mlTimerUnit, mlTimerUnit); // set timer duration
//        }
//    }

    //停止计时
//    public void stopTimerTask() {
//        if (null != timer) {
//            task.cancel();
//            task = null;
//            timer.cancel(); // Cancel timer
//            timer.purge();
//            timer = null;
//            handlerTimerTask.removeMessages(msg.what);
//        }
//        mlCount = 0;
//    }

    //异步处理计时数据
//    @SuppressLint("HandlerLeak")
//    public Handler handlerTimerTask = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case 1:
//                    mlCount++;
//                    totalSec = 0;
//                    // 100 millisecond
//                    totalSec = (int) (mlCount / 100);
//                    yushu = (int) (mlCount % 100);
//                    // Set time display
//                    min = (totalSec / 60);
//                    sec = (totalSec % 60);
//                    try {
//                        // 100 millisecond
//                        textView_time.setText(String.format("%1$02d:%2$02d:%3$d", min, sec, yushu));
//                    } catch (Exception e) {
//                        textView_time.setText("" + min + ":" + sec + ":" + yushu);
//                        e.printStackTrace();
//                    }
//                    break;
//
//                default:
//                    break;
//            }
//            super.handleMessage(msg);
//        }
//
//    };


    public void InitDevice() {
        Comm.repeatSound = false;
        Comm.rfidSleep = 100;
        Comm.app = getApplication();
        Comm.spConfig = new SPconfig(this);
        context = MainActivity.this;
        soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        soundPool.load(this, R.raw.beep51, 1);

        checkDevice();
        if (!Comm.powerUp()) {
            Comm.powerDown();
            Toast.makeText(MainActivity.this, R.string.powerUpFalse,
                    Toast.LENGTH_SHORT).show();
        }
        Comm.connecthandler = connectH;
        Comm.Connect();
    }

//    String[] Coname = new String[]{"NO", "                    EPC ID ", "Count"};

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MessageEvent messageEvent) {
        switch (messageEvent.getType()) {
            case 0x33:
                tagListSize = lsTagList.size();
                tv_tags.setText("合计: " + tagListSize + " 个");
                break;
        }
    }


    private void showlist() {
        if (tagListSize > 0) {
            dataList.clear();
            for (int i = 0; i < tagListSize; i++) {
                dataList.add(new ScanEntity(lsTagList.get(i)));
            }
            tv_tags.setText("合计: " + tagListSize + " 个");
        }
        if (isQuick && !Comm.isrun) {
//                tv_state.setText(String.valueOf("正在处理数据，请稍后。。。"));
        }
        rfidAdapter.notifyDataSetChanged();
//        try {
//            int index = 1;
//            int ReadCnt = 0;//个数
//            List<Map<String, ?>> list = new ArrayList<Map<String, ?>>();
//            Map<String, String> h = new HashMap<String, String>();
//            for (int i = 0; i < Coname.length; i++)
//                h.put(Coname[i], Coname[i]);
//            list.add(h);
//            String epcstr = "";//epc
//
//            int ListIndex = 0;
//            if (tagListSize > 0)
//                tv_tags.setText(String.valueOf(tagListSize));
//            if (isQuick && !Comm.isrun)
////                tv_state.setText(String.valueOf("正在处理数据，请稍后。。。"));
//
//                if (!isQuick || !Comm.isrun) {
//                    while (tagListSize > 0) {
//                        if (index < 100) {
//                            epcstr = lsTagList.get(ListIndex).strEPC.replace(" ", "");
////                        if (epcstr.length() > 4) {
//                            Map<String, String> m = new HashMap<String, String>();
//                            m.put(Coname[0], String.valueOf(index));
//                            m.put(Coname[1], epcstr);
//                            ReadCnt = lsTagList.get(ListIndex).nReadCount;
//                            m.put(Coname[2], "1");
//                            //int mRSSI=Integer.parseInt(lsTagList.get(ListIndex).strRSSI);
//                            index++;
//                            list.add(m);
//
////                        }
//                        }
//                        ListIndex++;
//                        tagListSize--;
//                    }
//
//                }
//            if (!isQuick || !Comm.isrun) {
//                ListAdapter adapter = new MyAdapter(this, list,
//                        R.layout.listitemview_inv, Coname, new int[]{
//                        R.id.textView_readsort, R.id.textView_readepc,
//                        R.id.textView_readcnt});
//
//                // layout为listView的布局文件，包括三个TextView，用来显示三个列名所对应的值
//                // ColumnNames为数据库的表的列名
//                // 最后一个参数是int[]类型的，为view类型的id，用来显示ColumnNames列名所对应的值。view的类型为TextView
////                listView.setAdapter(adapter);
//
//            }
//        } catch (NumberFormatException e) {
//            e.printStackTrace();
//        }
//        if (!isrun)
//            tv_state.setText(String.valueOf("等待操作..."));
    }

//    private void ReadHandleUI() {
//        this.button_read.setEnabled(false);
//        this.button_stop.setEnabled(true);
//        this.button_set.setEnabled(false);
//        this.button_clean.setEnabled(false);
//        TabWidget tw = Comm.supoinTabHost.getTabWidget();
//        tw.getChildAt(1).setEnabled(false);
//        tw.getChildAt(2).setEnabled(false);
//    }

//    private void StopHandleUI() {
//        button_read.setEnabled(true);
//        button_stop.setEnabled(false);
//        this.button_set.setEnabled(true);
//        this.button_clean.setEnabled(true);
//        TabWidget tw = Comm.supoinTabHost.getTabWidget();
//        tw.getChildAt(1).setEnabled(true);
//        tw.getChildAt(2).setEnabled(true);
//    }

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
        return super.onKeyDown(keyCode, event);
    }

    public void release() {
        if (null != UHF1handler)
            Comm.stopScan();
        if (null != myapp.Mreader)
            myapp.Mreader.CloseReader();
        if (null != myapp.Rpower)
            myapp.Rpower.PowerDown();
        if (null != Comm.baseTabFragment.mReader) {
            Comm.baseTabFragment.mReader.free();
        }
        Comm.powerDown();
    }

    /* 释放按键事件 */
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (scanCode == 261 && isrun)
            btn_scan.performClick();
        else if (scanCode == 261 && !isrun)
            btn_scan.performClick();
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


}
