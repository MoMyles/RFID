package com.pugongying.uhf;

import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.pugongying.uhf.util.PrefsUtil;
import com.uhf.uhf.Common.Comm;
import com.uhf.uhf.UHF1.UHF1Application;
import com.uhf.uhf.UHF1.UHF1Function.SPconfig;

import org.greenrobot.eventbus.EventBus;

import static com.uhf.uhf.Common.Comm.checkDevice;
import static com.uhf.uhf.Common.Comm.context;
import static com.uhf.uhf.Common.Comm.isrun;
import static com.uhf.uhf.Common.Comm.lsTagList;
import static com.uhf.uhf.Common.Comm.operateType.nullOperate;
import static com.uhf.uhf.Common.Comm.soundPool;
import static com.uhf.uhf.Common.Comm.tagListSize;

public class MyApplication  extends UHF1Application {

    @Override
    public void onCreate() {
        super.onCreate();

        InitDevice();

        MultiDex.install(this);
    }

    @SuppressLint("HandlerLeak")
    public Handler uhfhandler = new Handler() {
        @SuppressWarnings({"unchecked", "unused"})
        @Override
        public void handleMessage(Message msg) {
            try {
                tagListSize = lsTagList.size();
                Bundle bd = msg.getData();
//                showlist();
                EventBus.getDefault().post(new MessageEvent(0x36));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void InitDevice() {
        Comm.repeatSound = false;
//        Comm.rfidSleep = 10;
        Comm.app = this;
        Comm.spConfig = new SPconfig(this);

        context = getApplicationContext();
        soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        soundPool.load(this, R.raw.beep51, 1);

        checkDevice();

        Comm.initWireless(Comm.app);
        Comm.connecthandler = connectH;
        Comm.mOtherHandler = opeHandler;
        Comm.Connect();
    }

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
                            String powerValueStr = PrefsUtil.get(getApplicationContext(), "power", "25");
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
        }
    };

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
        }
    };

    @Override
    public void onTerminate() {
        super.onTerminate();
        release();
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
