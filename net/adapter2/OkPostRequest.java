package com.yiche.net.adapter2;

import com.yiche.net.LL;
import com.yiche.net.NetCenter;
import com.yiche.net.NetParams;
import com.yiche.net.NetUtils;
import com.yiche.net.RequestBody;
import com.yiche.net.YCallback;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;

/**
 * Post请求
 */
public class OkPostRequest extends IOkRequest {
    protected OkPostRequest(RequestBody rb) {
        super(rb);
    }

    @Override
    protected okhttp3.RequestBody buildRequestBody() {
        return buildPostRequestBody(rb.params);
    }

    @Override
    protected okhttp3.RequestBody wrapRequestBody(okhttp3.RequestBody requestBody, final YCallback callback) {
        if (callback == null||requestBody==null) return requestBody;
        CountingRequestBody countingRequestBody = new CountingRequestBody(requestBody, new CountingRequestBody.Listener() {
            @Override
            public void onRequestProgress(final long bytesWritten, final long contentLength) {

                NetCenter.getDelivery().postResponse(new Runnable() {
                    @Override
                    public void run() {
                        callback.inProgress(bytesWritten, contentLength);
                    }
                });

            }
        });
        return countingRequestBody;
    }

    @Override
    protected Request buildRequest(okhttp3.RequestBody requestBody) {
        return builder.post(requestBody).build();
    }


    /**
     * 根据参数生成Postq请求体RequestBody
     */
    protected okhttp3.RequestBody buildPostRequestBody(NetParams np) {
        if (np == null || !np.isHasParams()) {
            FormBody.Builder builder = new FormBody.Builder();
            return builder.build();
        }
        if (!np.isHasFileParams()) {
            FormBody.Builder builder = new FormBody.Builder();
            addParams(builder, np.urlParams);
            return builder.build();
        } else {
            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);
            addParams(builder, np.urlParams);
            // Add stream params
            for (ConcurrentHashMap.Entry<String, NetParams.MyStreamWrapper> entry : np.streamParams.entrySet()) {
                NetParams.MyStreamWrapper stream = entry.getValue();
                byte[] data = input2byte(stream.inputStream);
                if (data != null) {
                    okhttp3.RequestBody byteBody = okhttp3.RequestBody.create(MediaType.parse(stream.contentType), data);
                    String customName = stream.name==null?  System.currentTimeMillis()+"": stream.name;
                    builder.addFormDataPart(entry.getKey(), customName, byteBody);
                }else{
                    LL.w("process stream params error!!!!!!");
                }
            }
            // Add byte[] params
            for (ConcurrentHashMap.Entry<String, NetParams.MyBytesWrapper> entry : np.bytesParams.entrySet()) {
                NetParams.MyBytesWrapper stream = entry.getValue();
                if (stream.bytes != null) {
                    okhttp3.RequestBody byteBody = okhttp3.RequestBody.create(MediaType.parse(stream.contentType), stream.bytes);
                    String customName = stream.customName==null?  System.currentTimeMillis()+"": stream.customName;
                    builder.addFormDataPart(entry.getKey(), customName, byteBody);
                }else{
                    LL.w("process byte[] params error!!!!!!");
                }
            }
            // Add file params
            for (ConcurrentHashMap.Entry<String, NetParams.MyFileWrapper> entry : np.fileParams.entrySet()) {
                NetParams.MyFileWrapper fileWrapper = entry.getValue();
                String key = entry.getKey();

                okhttp3.RequestBody fileBody = okhttp3.RequestBody.create(MediaType.parse(fileWrapper.contentType), fileWrapper.file);
                String customFileName = fileWrapper.customFileName==null?  System.currentTimeMillis()+fileWrapper.file.getName(): fileWrapper.customFileName;
                builder.addFormDataPart(key, customFileName, fileBody);
                //httpclient的写法：addPart(entry.getKey(), fileWrapper.file, fileWrapper.contentType, fileWrapper.customFileName);
            }
            return builder.build();
        }
    }

    private void addParams(MultipartBody.Builder builder, Map<String, String> params) {
        if (params != null && !params.isEmpty()) {
            for (String key : params.keySet()) {
                builder.addFormDataPart(key,params.get(key));
//                builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + key + "\""),
//                        RequestBody.create(null, params.get(key)));
            }
        }
    }

    private void addParams(FormBody.Builder builder, Map<String, String> params) {
        if (params != null) {
            for (String key : params.keySet()) {
                try {
                    builder.add(key, params.get(key));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static final byte[] input2byte(InputStream inStream) {
        ByteArrayOutputStream swapStream =null ;
        try {
            swapStream = new ByteArrayOutputStream();
            byte[] buff = new byte[4096];
            int rc = 0;
            while ((rc = inStream.read(buff)) > 0) {
                swapStream.write(buff, 0, rc);
            }
            byte[] in2b = swapStream.toByteArray();
            return in2b;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }finally {
            NetUtils.silentCloseStream(inStream);
            NetUtils.silentCloseStream(swapStream);
        }
    }

}
