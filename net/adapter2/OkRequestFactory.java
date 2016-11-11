package com.yiche.net.adapter2;

import com.yiche.net.NetMethod;
import com.yiche.net.RequestBody;


/**
 * 生成
 */
public class OkRequestFactory {

    public static IOkRequest create(RequestBody rb) {
        if (rb.method == NetMethod.GET) {
            return new OkGetRequest(rb);
        } else if (rb.method == NetMethod.POST) {
            return new OkPostRequest(rb);
        }
        return null;
    }


}
