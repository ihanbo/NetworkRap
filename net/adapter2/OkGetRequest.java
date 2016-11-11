package com.yiche.net.adapter2;

import com.yiche.net.NetUtils;
import com.yiche.net.RequestBody;

import okhttp3.Request;

/**
 * Get请求
 */
public class OkGetRequest extends IOkRequest {
    protected OkGetRequest(RequestBody rb) {
        super(rb);
        String newUrl = NetUtils.getUrlWithQueryString(rb.shouldEncode,rb.url,rb.params);
        builder.url(newUrl);
    }

    @Override
    protected okhttp3.RequestBody buildRequestBody() {
        return null;
    }

    @Override
    protected Request buildRequest(okhttp3.RequestBody requestBody) {
        return builder.get().build();
    }
}
