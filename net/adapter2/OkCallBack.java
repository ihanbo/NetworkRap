package com.yiche.net.adapter2;

import android.os.SystemClock;
import com.yiche.net.Delivery;
import com.yiche.net.NetRes;
import com.yiche.net.NetworkResponse;
import com.yiche.net.RequestBody;
import com.yiche.net.YCallback;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Response;

/**
 * Ok的回调
 */
public class OkCallBack<T> implements okhttp3.Callback {
    private long startTime;
    YCallback<T> yCallback;
    Delivery mDelivery;
    RequestBody rb;

    public OkCallBack(YCallback<T> yCallback, Delivery delivery, RequestBody rb) {
        this.yCallback = yCallback;
        this.mDelivery = delivery;
        this.rb = rb;
        if (this.yCallback == null) {
            this.yCallback = YCallback.CALLBACK_DEFAULT;
        }
    }

    /**
     * 标记请求开始 计时用
     */
    public OkCallBack startRequest() {
        startTime = SystemClock.elapsedRealtime();
        return this;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        NetRes<T> res = NetRes.error(e,null);
        res.setRb(rb);
        postResult(res);

    }

    @Override
    public void onResponse(Call call, Response response) {
        NetRes<T> res;
        if (response.code() >= 400 && response.code() <= 599) {
            String content = " wrong statuscode ,code >=400&&code<=599 -->content: ";
            try {
                content += response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            res = NetRes.error(new IOException(content),OkNet.transformResponse(response));
            res.setRb(rb);
            postResult(res);
            return;
        }
        NetworkResponse networkResponse = OkNet.transformResponse(response);
        res = yCallback.parseNetworkResponse(networkResponse);
        res.setRb(rb);
        postResult(res);
    }



    /** 主线程发送结果*/
    public void postResult(NetRes<T> res) {
        mDelivery.postResponse(new ResponseRunable(res, yCallback));
    }


    public static class ResponseRunable<T> implements Runnable {
        public ResponseRunable(NetRes<T> res, YCallback<T> yCallback) {
            this.res = res;
            this.yCallback = yCallback;
        }

        NetRes<T> res;
        YCallback<T> yCallback;

        @Override
        public void run() {
            if (res.isSuccess()) {
                if (yCallback.isAvailable()) {
                    yCallback.onResponse(res);
                    yCallback.onSuccess(res.result);
                }
            } else {
                if (yCallback.isAvailable()) {
                    yCallback.onError(res.error);
                }
            }
        }
    }

}
