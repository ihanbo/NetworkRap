package com.yiche.net;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.yiche.net.adapter2.OkNet;

/**
 * 底层网络中心
 */
public class NetCenter {
    public static final INet inet = new OkNet();

    public static Delivery delivery;

    public static void init(Context context) {
        delivery = new Delivery(new Handler(Looper.getMainLooper()));
        inet.init(context, delivery);
    }

    public static <T> void newRequest(final RequestBody rb, final YCallback<T> yCallback) {
       inet.newRequest(rb,yCallback);
    }


    public static Delivery getDelivery() {
        return delivery;
    }

    public static NetworkResponse newRequest(RequestBody rb) throws Exception {
        return inet.newRequest(rb);
    }


    public static void cancelByTag(Object tag){
        inet.cacelByTag(tag);
    }


}
