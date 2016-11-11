/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yiche.net;

/**
 * 投递结果的封装，成功和失败都有
 */
public class NetRes<T> {


    /** 异常*/
    public  Throwable error;

    /**　请求体*/
    public RequestBody rb;
    /** 响应的包裹 */
    public NetworkResponse response;
    /**结果*/
    public final T result;


    //辅助信息
    public String descMsg = "Han: message is not set";
    //响应内容，可能没有
    public String responseString = "not set";
    public Object middleExtra;


    public static <T> NetRes<T> success(T result, NetworkResponse response, String descMsg) {
        return new NetRes(result, response).setDesc(descMsg);
    }

    public static NetRes error(Throwable error,NetworkResponse response) {
        return new NetRes(error).setDesc(error.getMessage()).setNetWorkResponse(response);
    }

    private NetRes setNetWorkResponse(NetworkResponse response) {
        this.response = response;
        return this;
    }

    private NetRes(T result, NetworkResponse response) {
        this.result = result;
        this.error = null;
        this.response = response;
    }

    private NetRes(Throwable error) {
        this.result = null;
        this.response = null;
        this.error = error;
    }

    public NetRes<T> setRb(RequestBody rb) {
        this.rb = rb;
        return this;
    }

    public NetRes<T> setDesc(String descMsg) {
        this.descMsg = descMsg;
        return this;
    }

    public NetRes<T> setMiddleExtra(Object middleExtra) {
        this.middleExtra = middleExtra;
        return this;
    }

    public boolean isSuccess() {
        return error == null;
    }


    public NetRes<T> setResponseString(String responseString) {
        this.responseString = responseString;
        return this;
    }
}
