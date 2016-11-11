package com.yiche.net.adapter2;


import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * 请求拦截器
 */
public class OkIntercetor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response= chain.proceed(chain.request());
        return response;
    }

}
