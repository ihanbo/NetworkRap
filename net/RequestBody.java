package com.yiche.net;

import android.text.TextUtils;

import com.yiche.net.decorator.LogNetDecorator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 请求的内容体，包括：url 参数 head
 */
public class RequestBody {
    public boolean shouldEncode = true;
    public int method;
    public String url;
    public Map<String, String> headers;
    public NetParams params;

    public List<WrapedCallBack> funcs;

    /**额外数据*/
    public RequestObjectTag requestTag;

    /** 缓存相关 */
    public String cacheKey;
    public long cacheTime = -1;
    public CacheMethod cacheMethod;

    /** 每一个请求一个UUID */
    public String req_uuid;

    private RequestBody() {
        requestTag = new RequestObjectTag();
        funcs =new ArrayList<>(3);
        funcs.add(LogNetDecorator.getInstnace());
        req_uuid = UUID.randomUUID().toString().toUpperCase();
    }


    public static RequestBody get() {
        RequestBody rb = new RequestBody();
        rb.method = NetMethod.GET;
        return rb;
    }

    public static RequestBody post() {
        RequestBody rb = new RequestBody();
        rb.method = NetMethod.POST;
        return rb;
    }

    public RequestBody url(String url) {
        this.url = url;
        return this;
    }

    public RequestBody setObjectTag(Object tag) {
        this.requestTag.requestCancelKey = tag;
        return this;
    }
    //表单参数
    public RequestBody setParams(NetParams params) {
        this.params = params;
        return this;
    }

    public RequestBody addParams(String key, String val) {
        if (this.params == null) {
            params = new NetParams();
        }
        params.put(key, val);
        return this;
    }

    public RequestBody addParams(String key, int value) {
        return addParams(key, String.valueOf(value));
    }

    public RequestBody addParams(String key, float value) {
        return addParams(key, String.valueOf(value));
    }

    public RequestBody addParams(String key, double value) {
        return addParams(key, String.valueOf(value));
    }

    public RequestBody addParams(String key, long value) {
        return addParams(key, String.valueOf(value));
    }

    //文件参数
    public RequestBody addParams(String key, File file, String contentType,
                                 String customFileName) {
        if (file == null || !file.exists()) {
            return this;
        }
        if (this.params == null) {
            params = new NetParams();
        }
        try {
            params.put(key, file, contentType,
                    customFileName);
        } catch (Exception e) {
        }
        return this;
    }

    public RequestBody addParams(String key, File file) throws FileNotFoundException {
        return addParams(key, file, null, null);
    }

    public RequestBody addParams(String key, String customFileName, File file)
            throws FileNotFoundException {
        return addParams(key, file, null, customFileName);
    }

    public RequestBody addParams(String key, File file, String contentType)
            throws FileNotFoundException {
        return addParams(key, file, contentType, null);
    }

    //byte[]参数
    public RequestBody addParams(String key, byte[] bytes, String contentType,
                                 String customFileName) {
        if (bytes == null ) {
            return this;
        }
        if (this.params == null) {
            params = new NetParams();
        }
        try {
            params.put(key, bytes, contentType,
                    customFileName);
        } catch (Exception e) {
        }
        return this;
    }
    public RequestBody addParams(String key, byte[] bytes) throws FileNotFoundException {
        return addParams(key, bytes, null, null);
    }

    //head
    public RequestBody headers(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public RequestBody addHeader(String key, String val) {
        if (this.headers == null) {
            headers = new LinkedHashMap<>();
        }
        headers.put(key, val);
        return this;
    }


    /** 设置参数是否encode */
    public RequestBody shouldEncode(boolean shouldEncode) {
        this.shouldEncode = shouldEncode;
        return this;
    }

    /** 设置缓存。假如传了参数cacheKey，可以任意位置调用，
     * 没传的话需在设置完URL和参数后调用，否则会抛异常。
     * 因为生成默认缓存key的方法是用url和参数*/
    public RequestBody cache(long cacheTime, CacheMethod cacheMethod, String cacheKey) {
        this.cacheTime = cacheTime;
        this.cacheMethod = cacheMethod;
        if (TextUtils.isEmpty(cacheKey)) {
            this.cacheKey = geneRateOnlyKey();
        } else {
            this.cacheKey = cacheKey;
        }
        if (this.cacheKey == null || (cacheMethod != CacheMethod.ONLY_CUN && cacheTime <= 0)) {
            throw new RuntimeException("RequestBody cacheKey is null or cacheTime wrong");
        }
        //TODO 框架处理方式
        addHeader("Cache-Control: public", "max-age=" + cacheTime);
        return this;
    }


    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("===Request:");
        buffer.append("\nUrl :--> ");
        buffer.append(url);
        buffer.append("\nPara:--> ");
        buffer.append(params == null ? "No Params" : params.toString());
        buffer.append("\nHead:--> ");
        buffer.append(headString());
        return buffer.toString();
    }


    /**
     * 凭借请求的head
     */
    public String headString() {
        boolean noCustonHeaders = headers == null || headers.isEmpty();
        if (noCustonHeaders) {
            return "No Heads";
        }
        StringBuilder result = new StringBuilder();
        if(!noCustonHeaders){
            Iterator<Map.Entry<String, String>> iter = headers.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> entry = iter.next();
                String key = entry.getKey();
                String val = entry.getValue();
                result.append(key).append(": ").append(val).append(" | ");
            }
        }
        return result.toString();
    }

    /** 生成默认缓存的key*/
    public String geneRateOnlyKey() {
        if(url==null){
            throw new RuntimeException("生成默认缓存key需要在设置完url和参数后调用");
        }
        String paramsString = params == null ? "" : params.toString();
        return url + paramsString;
    }


    /** 是否需要缓存处理*/
    public boolean isCacheable() {
        boolean yiwai = cacheMethod == CacheMethod.CUN_WITH_TIME && cacheTime <= 0;
        return cacheMethod != null &&!yiwai&&!TextUtils.isEmpty(cacheKey);
    }


}
