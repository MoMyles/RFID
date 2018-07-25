package com.pugongying.uhf;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.Map;
import java.util.Set;

public class NetUtil {
//    public static String NET_ADDRESS = "http://47.97.201.219:8668/wm/WebService/WebRFID.asmx";
    public static String NET_ADDRESS = "http://192.168.1.89:8008/RFID/webservice/webrfid.asmx";
    private static final String NAMESPACE = "http://tempuri.org/";

    private static Object get(String method, Map<String, String> params) {
        if (TextUtils.isEmpty(method)) return null;
        SoapObject request = new SoapObject(NAMESPACE, method);
        if (params != null) {
            Set<Map.Entry<String, String>> entries = params.entrySet();
            for (Map.Entry<String, String> e : entries) {
                PropertyInfo pi = new PropertyInfo();
                pi.setName(e.getKey());
                pi.setValue(e.getValue());
                request.addProperty(pi);
            }
        }
        try {
            final HttpTransportSE httpTransportSE = new HttpTransportSE(NET_ADDRESS);
            httpTransportSE.debug = false;
            final SoapSerializationEnvelope soapSerialize = new SoapSerializationEnvelope(SoapEnvelope.VER12);
            soapSerialize.bodyOut = request;
            soapSerialize.dotNet = true;
            soapSerialize.setOutputSoapObject(request);
            httpTransportSE.call(NAMESPACE + method, soapSerialize);
            return soapSerialize.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class NetTask extends AsyncTask<Object, Void, Object> {

        private NetListener netListener;

        @Override
        protected Object doInBackground(Object... objects) {
            if (objects == null || objects.length == 0) return null;
            if (objects.length == 1) {
                String method = (String) objects[0];
                return NetUtil.get(method, null);
            } else if (objects.length == 2) {
                String method = (String) objects[0];
                Map<String, String> params = (Map<String, String>) objects[1];
                return NetUtil.get(method, params);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (o == null) {
                return;
            }
            try {
//                SoapObject so = (SoapObject) o;
//                StringBuilder sb = new StringBuilder();
//                for (int i = 0; i < so.getPropertyCount(); i++) {
//                    sb.append(so.getProperty(i));
//                }
                String result = o.toString();
                Log.e("TAG", result);
                if (netListener != null) {
                    netListener.success(result);
                }
            } catch (Exception e) {
                if (netListener != null) {
                    netListener.failure();
                }
            }
        }

        public NetTask listen(NetListener netListener) {
            this.netListener = netListener;
            return this;
        }
    }

    public interface NetListener {
        void success(String data);

        void failure();
    }
}
