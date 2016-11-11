package com.yiche.net;

/**
 * 包裹类
 */
public class WrapedCallBack<T> extends YCallback<T> {


    protected YCallback<T> wrapedCallBack = YCallback.CALLBACK_DEFAULT;

    public WrapedCallBack() {
    }

    public WrapedCallBack(YCallback<T> wrapedCallBack) {
        this();
        this.wrapedCallBack = wrapedCallBack;
    }

    public void wraped(YCallback cb) {
        if (cb == null) {
            return ;
        }
        this.wrapedCallBack = cb;
    }
    @Override
    public void inProgress(long count, long allcount) {
        wrapedCallBack.inProgress(count, allcount);
    }

    @Override
    public NetRes<T> parseNetworkResponse(NetworkResponse response) {
        return wrapedCallBack.parseNetworkResponse(response);
    }

    @Override
    public void onResponse(NetRes<T> netResPonse) {
        wrapedCallBack.onResponse(netResPonse);
    }

    @Override
    public void onError(Throwable e) {
        wrapedCallBack.onError(e);
    }

    @Override
    public void onSuccess(T result) {
        wrapedCallBack.onSuccess(result);
    }


    @Override
    public boolean isAvailable() {
        return  wrapedCallBack.isAvailable();
    }

}
