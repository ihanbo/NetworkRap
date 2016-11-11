package com.yiche.net;
import com.yiche.net.parser.Iparser;

/**
 * 请求回调
 *
 * @param <T>
 */
public abstract class YCallback<T> {
    public RequestBody rb;
    /** 回调是否可用的判断*/
    public CallBacackAvailableListener listener;
    /**  解析响应内容的解析器*/
    protected Iparser<T> parser = Iparser.DEFAULT;


    /** 进度中（有的不支持 慎用） */
    public void inProgress(long count, long allcount) { }

    /** 解析NetworkResponse 子线程中*/
    public NetRes<T> parseNetworkResponse(NetworkResponse response){
        return parser.parse(response);
    }

    /** 投递结果 主线程中 成功,preProcessState 为预处理返回的状态码 */
    public void onResponse(NetRes<T> netResPonse) {}

    /** 投递失败结果 主线程中*/
    public abstract void onError(Throwable e);
    /** 投递成功结果 主线程中*/
    public abstract void onSuccess(T result);


    /** 回调是否有效*/
    public boolean isAvailable() {
        return listener == null || listener.isAvailable();
    }



    public void setRb(RequestBody rb) {
        this.rb = rb;
    }

    public void setParser(Iparser<T> parser) {
        this.parser = parser;
    }



    public static final YCallback CALLBACK_DEFAULT = new YCallback() {

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onSuccess(Object result) {

        }

    };

}