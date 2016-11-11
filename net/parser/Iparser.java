package com.yiche.net.parser;

import com.yiche.net.NetRes;
import com.yiche.net.NetworkResponse;

import java.io.IOException;

/**
 * CallBack的解析器
 * 解析数据的
 */
public interface Iparser<T> {
    NetRes<T> parse(NetworkResponse networkResponse);


    Iparser DEFAULT = new Iparser<byte[]>() {
         @Override
         public NetRes<byte[]> parse(NetworkResponse networkResponse) {
             try {
                 return NetRes.success(networkResponse.bytes(),networkResponse,"default parser no care result");
             } catch (IOException e) {
                 return NetRes.error(e,networkResponse);
             }
         }
     };
}
