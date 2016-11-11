package com.yiche.net.adapter2;

import android.content.Context;

import com.yiche.net.Delivery;
import com.yiche.net.INet;
import com.yiche.net.IReponse;
import com.yiche.net.NetworkResponse;
import com.yiche.net.RequestBody;
import com.yiche.net.YCallback;
import com.yiche.net.RequestObjectTag;

import java.net.CookieHandler;
import java.net.CookiePolicy;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Call;
import okhttp3.CookieJar;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * Created by hanbo1 on 2016/4/5.
 */
public class OkNet implements INet {
    private static OkHttpClient mClient;
    private Delivery delivery;

    public static OkHttpClient getOkHttpClient(Context context) {
        if(mClient==null&&context==null){
            throw new RuntimeException("H: HttpClient not init and context is null!!we need real context to init HttpClient. ");
        }
        if (mClient == null) {
            synchronized (OkNet.class) {
                if (mClient == null) {
                    OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
                    //cookie支持
                    okHttpClientBuilder.cookieJar(new CookieJarImpl(new PersistentCookieStore(context)));
                    okHttpClientBuilder.hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });
                    OkIntercetor okIntercetor = new OkIntercetor();
                    okHttpClientBuilder.addNetworkInterceptor(okIntercetor);
                    okHttpClientBuilder.connectTimeout(30, TimeUnit.SECONDS);
                    okHttpClientBuilder.readTimeout(30, TimeUnit.SECONDS);
                    okHttpClientBuilder.writeTimeout(30, TimeUnit.SECONDS);
                    mClient = okHttpClientBuilder.build();
                }
            }
        }
        return mClient;
    }

    public OkNet() {
    }


    @Override
    public void init(Context context, Delivery delivery) {
        getOkHttpClient(context);
        java.net.CookieManager cookieManager = new java.net.CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
        this.delivery = delivery;
    }

    @Override
    public <T> void newRequest(RequestBody rb, YCallback<T> callback) {
        IOkRequest okRequest = OkRequestFactory.create(rb);
        YCall yCall = new YCall(okRequest);
        if (callback != null) {
            callback.setRb(rb);
        }
        yCall.execute(callback, delivery);
    }

    @Override
    public NetworkResponse newRequest(RequestBody rb) throws Exception {
        IOkRequest okRequest = OkRequestFactory.create(rb);
        YCall yCall = new YCall(okRequest);
        NetworkResponse response = yCall.execute();
        return response;
    }

    @Override
    public void clearCookie() {
        CookieJar cookieJar = mClient.cookieJar();
        if (cookieJar != null && cookieJar instanceof CookieJarImpl) {
            CookieJarImpl cookie = (CookieJarImpl) cookieJar;
            cookie.getCookieStore().removeAll();
        }
    }

    @Override
    public void authCookie() {
        PersistentCookieStore cookieStore = getCookieStore();
        if (cookieStore == null || cookieStore.getCookies()==null|| cookieStore.getCookies().isEmpty()) {
            return;
        }
        boolean cookieExpired = cookieStore.getCookies().get(0).expiresAt() <= System.currentTimeMillis();
        //TODO 过期怎么处理?
    }



    @Override
    public void cacelByTag(Object tag) {
        for (Call call : mClient.dispatcher().queuedCalls()) {
            RequestObjectTag realTag = ((RequestBody)call.request().tag()).requestTag;
            if (tag.equals(realTag.requestCancelKey)) {
                call.cancel();
            }
        }
        for (Call call : mClient.dispatcher().runningCalls()) {
            RequestObjectTag realTag = ((RequestBody)call.request().tag()).requestTag;
            if (tag.equals(realTag.requestCancelKey)) {
                call.cancel();
            }
        }
    }

    @Override
    public PersistentCookieStore getCookieStore() {
        CookieJar cookieJar = mClient.cookieJar();
        if (cookieJar == null || !(cookieJar instanceof CookieJarImpl)) {
            return null;
        }
        CookieJarImpl cookie = (CookieJarImpl) cookieJar;
        return cookie.getCookieStore();
    }


    public void cancelTag(Object tag) {
        for (Call call : mClient.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : mClient.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }




    /** 转换response*/
    public static NetworkResponse transformResponse(Response okHttpResponse) {
        if(okHttpResponse==null){
            return new NetworkResponse();
        }
        //状态码
        int statusCode = okHttpResponse.code();
        //内容
        IReponse responseData = null;
        if(okHttpResponse.body()!=null){
            responseData = new OkResponseData(okHttpResponse.body());
        }
        //Head信息
        Map<String, String> heads = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        Headers responseHeaders = okHttpResponse.headers();
        if(responseHeaders!=null&&responseHeaders.size()>0){
            for (int i = 0; i < responseHeaders.size(); i++) {
                String name = responseHeaders.name(i), value = responseHeaders.value(i);
                heads.put(name, value);
            }
        }
        //响应时间
        long costTime = okHttpResponse.receivedResponseAtMillis()-okHttpResponse.sentRequestAtMillis();
        return new NetworkResponse(statusCode, responseData, heads, costTime);
    }

}
