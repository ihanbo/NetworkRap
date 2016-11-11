package com.yiche.net.adapter2;

import com.yiche.net.RequestBody;
import com.yiche.net.YCallback;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Ok请求基类
 */
public abstract class IOkRequest {

    protected Request.Builder builder = new Request.Builder();
    public RequestBody rb;

    protected IOkRequest(RequestBody rb) {
        this.rb = rb;
        if (rb == null || rb.url == null) {
            throw new RuntimeException("url can not be null.");
        }
        initBuilder();
    }


    /**
     * 初始化一些基本参数 url , tag , headers
     */
    private void initBuilder() {
        builder.url(rb.url).tag(rb);
        appendHeaders();
    }

    /**
     * 构建请求体
     */
    protected abstract okhttp3.RequestBody buildRequestBody();

    /**
     * 包裹请求体，不是必须的，通过包裹请求体 可以通知回调进度
     */
    protected okhttp3.RequestBody wrapRequestBody(okhttp3.RequestBody requestBody, final YCallback callback) {
        return requestBody;
    }

    /**
     * 通过{@link #builder}和请求体RequestBody 生成Okhttp的Request
     */
    protected abstract Request buildRequest(okhttp3.RequestBody requestBody);

    /**
     * 生成okhttp的请求
     *
     * @param callback
     * @return
     */
    public Request generateRequest(YCallback callback) {
        if (callback == null) {
            callback = YCallback.CALLBACK_DEFAULT;
        }
        okhttp3.RequestBody requestBody = buildRequestBody();
        okhttp3.RequestBody wrappedRequestBody = wrapRequestBody(requestBody, callback);
        Request request = buildRequest(wrappedRequestBody);
        return request;
    }


    /**
     * 添加请求head
     */
    protected void appendHeaders() {
        Headers.Builder headerBuilder = new Headers.Builder();
        if (rb.headers == null || rb.headers.isEmpty()) return;

        for (String key : rb.headers.keySet()) {
            headerBuilder.add(key, rb.headers.get(key));
        }
        builder.headers(headerBuilder.build());
    }

}
