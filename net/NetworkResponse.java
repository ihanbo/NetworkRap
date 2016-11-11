package com.yiche.net;
/**
 * Created by Han on 2016/10/17.
 */
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 响应结果的包裹
 */
public class NetworkResponse implements IReponse {
    public NetworkResponse(int statusCode, IReponse responseData, Map<String, String> headers, long networkTimeMs) {
        this.statusCode = statusCode;
        this.responseData = responseData;
        this.headers = headers;
        this.networkTimeMs = networkTimeMs;
    }

    public NetworkResponse(int statusCode, IReponse responseData, Map<String, String> headers) {
        this(statusCode, responseData, headers, 0);
    }

    public NetworkResponse(IReponse responseData) {
        this(200, responseData, Collections.<String, String>emptyMap(), 0);
    }

    public NetworkResponse(IReponse responseData, Map<String, String> headers) {
        this(200, responseData, headers, 0);
    }

    public NetworkResponse() {
        statusCode=-1;
        responseData=null;
        headers = new HashMap<>();
        networkTimeMs = 0;
    }

    /** 响应状态码*/
    public final int statusCode;
    public final IReponse responseData;
    /** 响应头*/
    public final Map<String, String> headers;
    /** 请求-响应的时间 */
    public final long networkTimeMs;

    @Override
    public byte[] bytes() throws IOException {
        return responseData==null? null:responseData.bytes();
    }

    @Override
    public InputStream byteStream() {
        return  responseData==null? null:responseData.byteStream();
    }

    @Override
    public String string() throws IOException {
        return  responseData==null? null:responseData.string();
    }
}

