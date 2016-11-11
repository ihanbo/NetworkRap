package com.yiche.net.adapter2;

import com.yiche.net.Delivery;
import com.yiche.net.INet;
import com.yiche.net.NetworkResponse;
import com.yiche.net.YCallback;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by zhy on 15/12/15.
 * 对OkHttpRequest的封装，对外提供更多的接口：cancel(),readTimeOut()...
 */
public class YCall {
    private IOkRequest okHttpRequest;
    private Request request;
    private Call call;

    private long readTimeOut;
    private long writeTimeOut;
    private long connTimeOut;

    OkHttpClient clone;

    public YCall(IOkRequest request) {
        this.okHttpRequest = request;
    }

    public YCall readTimeOut(long readTimeOut) {
        this.readTimeOut = readTimeOut;
        return this;
    }

    public YCall writeTimeOut(long writeTimeOut) {
        this.writeTimeOut = writeTimeOut;
        return this;
    }

    public YCall connTimeOut(long connTimeOut) {
        this.connTimeOut = connTimeOut;
        return this;
    }

    private Call buildCall(YCallback callback) {
        request = okHttpRequest.generateRequest(callback);

        if (readTimeOut > 0 || writeTimeOut > 0 || connTimeOut > 0) {
            readTimeOut = readTimeOut > 0 ? readTimeOut : INet.DEFAULT_MILLISECONDS;
            writeTimeOut = writeTimeOut > 0 ? writeTimeOut : INet.DEFAULT_MILLISECONDS;
            connTimeOut = connTimeOut > 0 ? connTimeOut : INet.DEFAULT_MILLISECONDS;

            clone = OkNet.getOkHttpClient(null).newBuilder()
                    .readTimeout(readTimeOut, TimeUnit.MILLISECONDS)
                    .writeTimeout(writeTimeOut, TimeUnit.MILLISECONDS)
                    .connectTimeout(connTimeOut, TimeUnit.MILLISECONDS)
                    .build();

            call = clone.newCall(request);
        } else {
            call = OkNet.getOkHttpClient(null).newCall(request);
        }
        return call;
    }

    public NetworkResponse execute() throws IOException {
        buildCall(null);
        return OkNet.transformResponse(call.execute());
    }

    public <T> void execute(YCallback<T> yCallback, Delivery delivery) {
        if(yCallback==null){
            yCallback = YCallback.CALLBACK_DEFAULT;
        }
        buildCall(yCallback);
        call.enqueue(new OkCallBack(yCallback, delivery, okHttpRequest.rb).startRequest());
    }


    public void cancel() {
        if (call != null) {
            call.cancel();
        }
    }


}
