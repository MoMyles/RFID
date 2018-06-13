package com.pugongying.uhf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScannerDebugBroadcastReceiver extends BroadcastReceiver {

    private static final String SECRET_CODE_ACTION = "android.provider.Telephony.SECRET_CODE";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(SECRET_CODE_ACTION)) {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setClass(context,MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}
