package com.yiche.net.parser;

import com.yiche.net.NetRes;
import com.yiche.net.NetworkResponse;

import java.io.IOException;

/**
 * Created by hanbo1 on 2016/4/8.
 */
public class BinaryParser implements Iparser<byte[]> {
    private static BinaryParser instance = new BinaryParser();

    public static BinaryParser getInstance() {
        return instance;
    }

    @Override
    public NetRes<byte[]> parse(NetworkResponse networkResponse) {
        try {
            return NetRes.success(networkResponse.bytes(), networkResponse, "result is binary data!");
        } catch (IOException e) {
            return NetRes.error(e,networkResponse);
        }
    }
}
