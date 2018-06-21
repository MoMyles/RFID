package com.pugongying.uhf;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.uhf.uhf.Common.Comm;
import com.uhf.uhf.UHF1.UHF001;
import com.uhf.uhf.UHF1Function.AndroidWakeLock;
import com.uhf.uhf.UHF1Function.SPconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Button btn_scan;

    private boolean isScaning = false;

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

//                Bundle bd = msg.getData();
//                strMsg = bd.get("Msg").toString();
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
        btn_scan = findViewById(R.id.btn_scan);
        tv_back = findViewById(R.id.tv_back);
        tv_complete = findViewById(R.id.tv_complete);
        tv_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_complete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 把识别结果对相应表单进行更新操作
            }
        });

//        tv_state = (TextView) findViewById(R.id.textView_invstate);
        tv_tags = (TextView) findViewById(R.id.textView_readallcnt);
//        textView_time = (TextView) findViewById(R.id.textView_time);
//        textView_time.setText("00:00:00");

//        button_set.setFocusable(false);
//        tv_state.setText("模块初始化失败");
        Awl = new AndroidWakeLock((PowerManager) getSystemService(Context.POWER_SERVICE));
        btn_scan.setOnClickListener(new OnClickListener() {
            @SuppressWarnings("unused")
            @Override
            public void onClick(View arg0) {
                if (!isScaning) {
                    // 开启扫描
                    try {
//                        startTimerTask();
//                        button_clean.performClick();
//                        tv_state.setText("开始读取...");
                        Awl.WakeLock();
                        Comm.startScan();
                        isScaning = true;
                        btn_scan.setText("电子标签关闭");
//                        ReadHandleUI();
                    } catch (Exception ex) {
                        Toast.makeText(MainActivity.this,
                                "ERR：" + ex.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                } else {
                    // 扫描中 则关闭
                    Awl.ReleaseWakeLock();
                    Comm.stopScan();
                    showlist();

                    isScaning = false;
                    btn_scan.setText("电子标签开启");
//                    StopHandleUI();
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
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SCN_CUST_ACTION_SCODE)) {
                String message;
                try {
                    message = intent.getStringExtra(SCN_CUST_EX_SCODE).toString();
                    Log.e("TAG", "扫描到的结果：" + message);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("in", e.toString());
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mSamDataReceiver);
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
        } else {
        }
        Comm.connecthandler = connectH;
        Comm.Connect();
    }

    String[] Coname = new String[]{"NO", "                    EPC ID ", "Count"};

    private void showlist() {
        try {
            int index = 1;
            int ReadCnt = 0;//个数
            List<Map<String, ?>> list = new ArrayList<Map<String, ?>>();
            Map<String, String> h = new HashMap<String, String>();
            for (int i = 0; i < Coname.length; i++)
                h.put(Coname[i], Coname[i]);
            list.add(h);
            String epcstr = "";//epc

            int ListIndex = 0;
            if (tagListSize > 0)
                tv_tags.setText(String.valueOf(tagListSize));
            if (isQuick && !Comm.isrun)
//                tv_state.setText(String.valueOf("正在处理数据，请稍后。。。"));

                if (!isQuick || !Comm.isrun) {
                    while (tagListSize > 0) {
                        if (index < 100) {
                            epcstr = lsTagList.get(ListIndex).strEPC.replace(" ", "");
//                        if (epcstr.length() > 4) {
                            Map<String, String> m = new HashMap<String, String>();
                            m.put(Coname[0], String.valueOf(index));
                            m.put(Coname[1], epcstr);
                            ReadCnt = lsTagList.get(ListIndex).nReadCount;
                            m.put(Coname[2], "1");
                            //int mRSSI=Integer.parseInt(lsTagList.get(ListIndex).strRSSI);
                            index++;
                            list.add(m);

//                        }
                        }
                        ListIndex++;
                        tagListSize--;
                    }

                }
            if (!isQuick || !Comm.isrun) {
                ListAdapter adapter = new MyAdapter(this, list,
                        R.layout.listitemview_inv, Coname, new int[]{
                        R.id.textView_readsort, R.id.textView_readepc,
                        R.id.textView_readcnt});

                // layout为listView的布局文件，包括三个TextView，用来显示三个列名所对应的值
                // ColumnNames为数据库的表的列名
                // 最后一个参数是int[]类型的，为view类型的id，用来显示ColumnNames列名所对应的值。view的类型为TextView
//                listView.setAdapter(adapter);

            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
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
