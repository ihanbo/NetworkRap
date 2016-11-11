package com.yiche.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.io.Closeable;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工具类
 */
public class NetUtils {



    /** 从head里获取Charset*/
    public static String parseCharset(Map<String, String> headers, String defaultCharset) {
        if (headers == null || headers.isEmpty()) {
            return defaultCharset;
        }
        String contentType = headers.get("Content-Type");
        if (contentType != null) {
            String[] params = contentType.split(";");
            for (int i = 1; i < params.length; i++) {
                String[] pair = params[i].trim().split("=");
                if (pair.length == 2) {
                    if (pair[0].equals("charset")) {
                        return pair[1];
                    }
                }
            }
        }
        return defaultCharset;
    }

    /**  从head里获取Charset*/
    public static String parseCharset(Map<String, String> headers) {
        // "ISO-8859-1"
        return parseCharset(headers, "UTF-8");
    }


    /**
     * 根据URL和NetParams拼成get请求的url
     *
     * @param shouldEncodeUrl
     * @param url
     * @param params
     * @return
     */
    public static String getUrlWithQueryString(boolean shouldEncodeUrl, String url, NetParams params) {
        if (url == null) {
            return null;
        } else {
            String paramString;
            if (shouldEncodeUrl) {
                try {
                    paramString = URLDecoder.decode(url, "UTF-8");
                    URL _url = new URL(paramString);
                    URI _uri = new URI(_url.getProtocol(), _url.getUserInfo(), _url.getHost(), _url.getPort(), _url.getPath(), _url.getQuery(), _url.getRef());
                    url = _uri.toASCIIString();
                } catch (Exception var6) {
                    Log.e("AsyncHttpClient", "getUrlWithQueryString encoding URL", var6);
                }
            }

            if (params != null && !params.urlParams.isEmpty()) {
                paramString = params.getParamString().trim();
                if (!paramString.equals("") && !paramString.equals("?")) {
                    url = url + (url.contains("?") ? "&" : "?");
                    url = url + paramString;
                }
            }
            return url;
        }
    }


    /**
     * 猜文件的mimetype
     *
     * @param path
     * @return
     */
    private static String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }





    public static void log(String ss) {
        Log.i("net", ss);
    }
    public static void logw(String ss) {
        Log.w("net", ss);
    }



    public static String generateKey(RequestBody requestBody) {
        if (requestBody == null) {
            return null;
        }
        return requestBody.geneRateOnlyKey();
    }

    public static String generateKey(String url, NetParams params) {
        if (url == null) {
            return null;
        }
        return RequestBody.get().url(url).setParams(params).geneRateOnlyKey();
    }




    /**
     * 获取网络状态
     */
    public static String getNetworkState(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            return "unConnected";
        }
        boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        if (isWiFi) {
            return "wifi";
        }
        // 取得Mobile信息 2G、3G还是4G
        TelephonyManager mTelephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = mTelephonyManager.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            default:
                return "Unknown";
        }
    }

    /**
     * 关闭流文件
     */
    public static void silentCloseStream(Closeable stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e) {
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * 获取根域名
     * @param url
     * @return
     */
    public static String getRootDomain(String url) {
        String domain = "";
        Pattern pattern = Pattern
                .compile("[\\w-]+\\.(com.cn|net.cn|gov.cn|org.cn|com|net|org|gov|cc|biz|info|cn|co)\\b()*");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            domain = matcher.group();
        }
        if (domain == null || domain.trim().equals(""))
            return null;
        else {
            return domain;
        }
    }


}
