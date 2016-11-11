package com.yiche.net.parser;

import com.yiche.net.NetRes;
import com.yiche.net.NetworkResponse;

import java.io.IOException;

/**
 * 解析String内容
 */
public class StringParser implements Iparser<String> {
    private static final StringParser instance = new StringParser();
    public static StringParser getInstance(){
        return instance;
    }
    @Override
    public NetRes<String> parse(NetworkResponse networkResponse) {
        String parsed = null;
        try {
            parsed = networkResponse.string();
            if(parsed==null){
                return NetRes.error(new IOException("H: StringParser string result is null!"),networkResponse);
            }
            return NetRes.success(parsed, networkResponse, parsed).setResponseString(parsed);
        } catch (Exception e) {
            return NetRes.error(e,networkResponse);
        }
    }
}
