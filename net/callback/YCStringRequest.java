package com.yiche.net.callback;

import com.yiche.net.CallBacackAvailableListener;
import com.yiche.net.YCallback;
import com.yiche.net.parser.StringParser;

/**
 * String回调
 */
public abstract  class YCStringRequest extends YCallback<String> {


    public YCStringRequest() {
        parser = StringParser.getInstance();
    }

    public YCStringRequest(CallBacackAvailableListener listener) {
        this();
        this.listener = listener;
    }

}
