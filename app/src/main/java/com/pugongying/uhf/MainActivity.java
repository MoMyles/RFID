package com.pugongying.uhf;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
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
import android.widget.ImageView;
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
public class MainActivity extends AppCompatActivity { // ActionBarActivity

    // 读线程：
    public static final String SCN_CUST_ACTION_SCODE = "com.android.server.scannerservice.broadcast";
    public static final String SCN_CUST_EX_SCODE = "scannerdata";
    /* defined by MEXXEN */
    public static final String SCN_CUST_ACTION_START = "android.intent.action.SCANNER_BUTTON_DOWN";
    public static final String SCN_CUST_ACTION_CANCEL = "android.intent.action.SCANNER_BUTTON_UP";

    private ImageView iv_status;
    private TextView tv_tags, tv_back, tv_complete;
    private Button btn_scan, btn_clean, btn_detail;
    private RecyclerView rv_list;
    private final List<ScanEntity> dataList = new ArrayList();


    private RFIDAdapter rfidAdapter;
    private boolean isRFID = true;// 默认高频识别
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
                            String powerValueStr = PrefsUtil.get(MainActivity.this, "power", "25");
                            Comm.setAntPower(Integer.parseInt(powerValueStr), 0, 0, 0);
                        } catch (Exception e) {
                            e.printStackTrace();
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

        setContentView(R.layout.activity_detail);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        btn_scan = findViewById(R.id.btn_scan);
        iv_status = findViewById(R.id.iv_status);
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
                                    //fastjson 
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
                    btn_scan.setText("开启条码扫描");
                }
            }
        });

        btn_clean.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isScaning) {
                    ToastUtils.show(getApplicationContext(), "扫描中，请先关闭电子标签!");
                    return;
                }
                if (isShow) {
                    ToastUtils.show(getApplicationContext(), "请先取消查看!");
                    return;
                }
                dataList.clear();
                lsTagList.clear();
                tagListSize = 0;
                luanmaCount = 0;
                rfidAdapter.notifyDataSetChanged();
                tv_tags.setText("合计: 0 个");
            }
        });

        btn_detail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isShow && isScaning) {
                    ToastUtils.show(getApplicationContext(), "扫描中，请先关闭电子标签!");
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
                                try {
                                    for (int i = 0; i < size; i++) {
                                        JSONObject obj = array.getJSONObject(i);
                                        if (CodeUtil.getDecodeStr(se.getMap()).equals(obj.getString("条码").trim())) {
                                            se.setObj(obj);
                                        }
                                    }
                                    se.setShow(true);
                                } catch (Exception e){
                                  e.printStackTrace();
                                }
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

        // Register receiver
        IntentFilter intentFilter = new IntentFilter(SCN_CUST_ACTION_SCODE);
        registerReceiver(mSamDataReceiver, intentFilter);

//        pd = new ProgressDialog(MainActivity.this);
//        pd.setCanceledOnTouchOutside(false);
//        pd.setMessage("初始化中......");
//        pd.show();

    }

    /**
     * 开启高频识别
     */
    private void startRFID() {
        // 开启扫描
        try {
//                        startTimerTask();
//                        button_clean.performClick();
//                        tv_state.setText("开始读取...");
            dataList.clear();
            lsTagList.clear();
            tagListSize = 0;
            luanmaCount = 0;
            Awl.WakeLock();
            Comm.startScan();
            isScaning = true;
//            btn_scan.setText("电子标签关闭");
//                        ReadHandleUI();
            iv_status.setImageResource(R.drawable.circle_green);
//            Toast.makeText(getApplicationContext(), "开启了高频识别", Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Toast.makeText(MainActivity.this,
                    "ERR：" + ex.getMessage(), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    /**
     * 关闭高频识别
     */
    private void closeRFID() {
        // 扫描中 则关闭
        Awl.ReleaseWakeLock();
        Comm.stopScan();
        showlist();

        isScaning = false;
        iv_status.setImageResource(R.drawable.circle_red);

    }

    /**
     * 红外扫描结果处理广播
     */
    private BroadcastReceiver mSamDataReceiver = new BroadcastReceiver() {
        private MediaPlayer mp;
        private MediaPlayer mp2;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isRFID) {
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
            } else {
                ToastUtils.show(getApplicationContext(), "当前为电子标签模式，请切换模式");
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

    /**
     * 红外扫描结果判断是否存在
     * @param message
     * @return
     */
    private boolean isExists(String message) {
        if (message == null || "".equals(message)) return true;
        if (!lsTagList.isEmpty()) {
            Iterator<InventoryBuffer.InventoryTagMap> iterator = lsTagList.iterator();
            while (iterator.hasNext()) {
                InventoryBuffer.InventoryTagMap ibitm = iterator.next();
//                Log.e("TAG", CodeUtil.getDecodeStr(ibitm) + "-" + message);
                if (CodeUtil.getDecodeStr(ibitm).equals(message.trim())) {
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

    /**
     * 驱动初始化
     */
    public void InitDevice() {
        Comm.repeatSound = false;
//        Comm.rfidSleep = 10;
        Comm.app = getApplication();
        Comm.spConfig = new SPconfig(this);

        context = MainActivity.this;
        soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        soundPool.load(this, R.raw.beep51, 1);

        checkDevice();

        Comm.initWireless(Comm.app);
        Comm.connecthandler = connectH;
        Comm.mOtherHandler = opeHandler;
        Comm.Connect();
    }

    @SuppressLint("HandlerLeak")
    private android.os.Handler opeHandler = new android.os.Handler() {
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
//            if (pd != null && pd.isShowing()) {
//                pd.dismiss();
//            }
        }
    };


    /**
     * 删除操作,若删除的为高频识别出来的，进行同步删除操作
     * @param messageEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MessageEvent messageEvent) {
        switch (messageEvent.getType()) {
            case 0x33:
                lsTagList.remove(messageEvent.getData());
                tagListSize = lsTagList.size();
                tv_tags.setText("合计: " + tagListSize + " 个");
                break;
            case 0x36:
                showlist();
                break;
        }
    }

    /**
     * 判断条码是否已存在
     * @param map
     * @return
     */
    private boolean isExists(InventoryBuffer.InventoryTagMap map) {
        for (ScanEntity se : dataList) {
            if (se.getMap().strEPC.equals(map.strEPC)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 识别结果显示处理
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
                        dataList.add(new ScanEntity(map));
                    } else {
                        luanmaCount++;
                    }
                }
            }
        } else {
            luanmaCount = 0;
        }
        tv_tags.setText("合计: " + (tagListSize - luanmaCount) + " 个");
//        if (isQuick && !Comm.isrun) {
//                tv_state.setText(String.valueOf("正在处理数据，请稍后。。。"));
//        }
        rfidAdapter.notifyDataSetChanged();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                rv_list.scrollToPosition(rfidAdapter.getItemCount() - 1);
            }
        }, 400);

    }

    /**
     * 物理按键按下监听
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
     * 物理按键释放监听
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

    /**
     * 释放高频识别资源
     */
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
