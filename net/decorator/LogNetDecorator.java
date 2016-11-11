package com.yiche.net.decorator;

import com.yiche.net.NetRes;
import com.yiche.net.NetworkResponse;
import com.yiche.net.WrapedCallBack;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by ihanb on 2016/11/11.
 */

public class LogNetDecorator extends WrapedCallBack {
    private static  LogNetDecorator instance;
    public static LogNetDecorator getInstnace(){
        if(instance==null){
            instance = new LogNetDecorator();
        }
        return instance;
    }

    private LogNetDecorator() {
    }

    @Override
    public void onResponse(NetRes netResPonse) {
        logNetString(netResPonse);
        super.onResponse(netResPonse);
    }

    /** 打印网络日志  */
    public static String logNetString(NetRes res) {
        if (res == null) {
            return "NetRes is null,no msg log! ";
        }
        StringBuilder sb = new StringBuilder(res.isSuccess() ? "===========SUCC===========" :
                "==============FAILED============");
        sb.append("\n").append(res.rb.toString())
                .append("===Response:").append("\nHead:").append(headString(res.response))
                .append("\nResp:").append(res.responseString);
        return sb.toString();
    }

    private static String headString(NetworkResponse response) {
        if (response == null || response.headers == null || response.headers.isEmpty()) {
            return "No Heads";
        }
        StringBuilder result = new StringBuilder();
        Iterator<Map.Entry<String, String>> iter = response.headers.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = iter.next();
            String key = entry.getKey();
            String val = entry.getValue();
            result.append(key).append(": ").append(val).append(" | ");
        }
        return result.toString();
    }

}
