package com.yiche.net;

import android.content.Context;
import com.yiche.net.adapter2.PersistentCookieStore;

/**
 * 网络引擎
 */
public interface INet {
    long DEFAULT_MILLISECONDS = 10000;

    void init(Context context, Delivery delivery);

    <T> void newRequest(RequestBody rb, YCallback<T> callback);

    NetworkResponse newRequest(RequestBody rb) throws Exception;

    void cacelByTag(Object tag);
    void clearCookie();
    void authCookie();
    PersistentCookieStore getCookieStore();
}
