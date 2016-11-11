package com.yiche.net;

import android.os.Handler;

import java.util.concurrent.Executor;

/**
 * 结果投递员（主线程）
 */
public class Delivery {
    private final Executor mResponsePoster;

    public Delivery(final Handler handler) {
        // Make an Executor that just wraps the handler.
        mResponsePoster = new Executor() {
            @Override
            public void execute(Runnable command) {
                handler.post(command);
            }
        };
    }


    public void postResponse(Runnable runnable) {
        mResponsePoster.execute(runnable);
    }

}
