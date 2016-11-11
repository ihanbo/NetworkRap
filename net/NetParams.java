package com.yiche.net;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 网络请求的参数
 */
public class NetParams implements Serializable {

    private static final long serialVersionUID = -7763507739563218570L;
    public String contentEncoding = "UTF_8";

    /**
     * 表单参数
     */
    public final Map<String, String> urlParams = new LinkedHashMap<>();
    /**
     * 流参数
     */
    public final ConcurrentHashMap<String, MyStreamWrapper> streamParams = new ConcurrentHashMap<String, MyStreamWrapper>();
    /**
     * 文件参数
     */
    public final ConcurrentHashMap<String, MyFileWrapper> fileParams = new ConcurrentHashMap<String, MyFileWrapper>();

    /**
     * 字节数组参数
     */
    public final ConcurrentHashMap<String, MyBytesWrapper> bytesParams = new ConcurrentHashMap<String, MyBytesWrapper>();

    protected boolean autoCloseInputStreams;

    public NetParams() {
        this((Map<String, String>) null);
    }

    public NetParams(Map<String, String> source) {
        if (source != null) {
            for (Map.Entry<String, String> entry : source.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    public NetParams(Object... keysAndValues) {
        int len = keysAndValues.length;
        if (len % 2 != 0)
            throw new IllegalArgumentException(
                    "Supplied arguments must be even");
        for (int i = 0; i < len; i += 2) {
            String key = String.valueOf(keysAndValues[i]);
            String val = String.valueOf(keysAndValues[i + 1]);
            put(key, val);
        }
    }

    /**
     * 返回key对应的value，没有的话返回null
     */
    public String get(String key) {
        return urlParams.get(key);
    }


    public void put(String key, String value) {
        if (key != null && value != null) {
            urlParams.put(key, value);
        }
    }

    public void put(String key, int value) {
        if (key != null) {
            urlParams.put(key, String.valueOf(value));
        }
    }

    public void put(String key, float value) {
        if (key != null) {
            urlParams.put(key, String.valueOf(value));
        }
    }

    public void put(String key, double value) {
        if (key != null) {
            urlParams.put(key, String.valueOf(value));
        }
    }

    public void put(String key, long value) {
        if (key != null) {
            urlParams.put(key, String.valueOf(value));
        }
    }


    /**
     * 添加文件参数
     */
    public void put(String key, File file) throws FileNotFoundException {
        put(key, file, null, null);
    }

    public void put(String key, String customFileName, File file)
            throws FileNotFoundException {
        put(key, file, null, customFileName);
    }

    public void put(String key, File file, String contentType)
            throws FileNotFoundException {
        put(key, file, contentType, null);
    }

    public void put(String key, File file, String contentType,
                    String customFileName) throws FileNotFoundException {
        if (file == null || !file.exists()) {
            throw new FileNotFoundException();
        }
        if (key != null) {
            fileParams.put(key, new MyFileWrapper(file, contentType,
                    customFileName));
        }
    }

    //字节数组
    public void put(String key, byte[] bytes) {
        put(key, bytes, null, null);
    }

    public void put(String key, String customFileName,  byte[] bytes){
        put(key, bytes, null, customFileName);
    }

    public void put(String key,  byte[] bytes, String contentType){
        put(key, bytes, contentType, null);
    }

    public void put(String key,  byte[] bytes, String contentType,
                    String customFileName) {
        if (key != null) {
            bytesParams.put(key, new MyBytesWrapper(bytes, contentType,
                    customFileName));
        }
    }
    //流
    public void put(String key, InputStream stream, String name,
                    String contentType) {
        put(key, stream, name, contentType, autoCloseInputStreams);
    }

    public void put(String key, InputStream stream, String name,
                    String contentType, boolean autoClose) {
        if (key != null && stream != null) {
            streamParams.put(key, MyStreamWrapper.newInstance(stream, name,
                    contentType, autoClose));
        }
    }

    public void remove(String key) {
        urlParams.remove(key);
        streamParams.remove(key);
        fileParams.remove(key);
    }

    public boolean has(String key) {
        return urlParams.get(key) != null || streamParams.get(key) != null
                || fileParams.get(key) != null;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : urlParams
                .entrySet()) {
            if (result.length() > 0)
                result.append("&");

            result.append(entry.getKey());
            result.append("=");
            result.append(entry.getValue());
        }

        for (Map.Entry<String, MyStreamWrapper> entry : streamParams
                .entrySet()) {
            if (result.length() > 0)
                result.append("&");

            result.append(entry.getKey());
            result.append("=");
            result.append("STREAM");
        }

        for (Map.Entry<String, MyFileWrapper> entry : fileParams
                .entrySet()) {
            if (result.length() > 0)
                result.append("&");

            result.append(entry.getKey());
            result.append("=");
            result.append("FILE");
        }

        return result.toString();
    }

    /**
     * 设置是否自动关闭
     */
    @Deprecated
    public void setAutoCloseInputStreams(boolean flag) {
        autoCloseInputStreams = flag;
    }

    protected List<BasicNameValuePair> getParamsList() {
        List<BasicNameValuePair> lparams = new LinkedList<BasicNameValuePair>();

        for (Map.Entry<String, String> entry : urlParams
                .entrySet()) {
            lparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        return lparams;
    }

    /**
     * 生成参数拼接字符串key1=value1&key2=value2&...
     * 注意是encode后的
     * @return
     */
    public String getParamString() {
        return format(getParamsList(), QP_SEP_A, contentEncoding);
    }
    /**
     * 生成参数拼接字符串key1=value1&key2=value2&...
     * 原始String为encode
     * @return
     */
    public String getPostParamString() {
        String PARAMETER_SEPARATOR = "&";
        String NAME_VALUE_SEPARATOR = "=";
        StringBuilder result = new StringBuilder();
        Iterator var5 = getParamsList().iterator();

        while (var5.hasNext()) {
            NameValuePair parameter = (NameValuePair) var5.next();
            String encodedName = parameter.getName();
            String value = parameter.getValue();
            String encodedValue = value != null ? value : "";
            if (result.length() > 0) {
                result.append(PARAMETER_SEPARATOR);
            }

            result.append(encodedName);
            result.append(NAME_VALUE_SEPARATOR);
            result.append(encodedValue);
        }

        return result.toString();
    }


    public Map<String, String> getUrlParams() {
        return urlParams;
    }


    /**
     * OkHttp用的post请求body
     */
    public byte[] getBody() {
        if (fileParams.isEmpty() && streamParams.isEmpty()&&bytesParams.isEmpty()) {
            return encodeParameters(urlParams, contentEncoding);
        } else {
            return SimpleMultipartEntity.getMultipartBody(this);
        }
    }

    private byte[] encodeParameters(Map<String, String> params, String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                encodedParams.append('&');
            }
            return encodedParams.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }


    public boolean isHasParams() {
        return !urlParams.isEmpty() || !streamParams.isEmpty() || !fileParams.isEmpty()|| !bytesParams.isEmpty();
    }

    public boolean isHasFileParams() {
        return !streamParams.isEmpty() || !fileParams.isEmpty()|| !bytesParams.isEmpty();
    }







    //=====start=====拷自http源码 格式化参数及encode用
    private static final BitSet URLENCODER = new BitSet(256);
    private static final String NAME_VALUE_SEPARATOR = "=";
    private static final char QP_SEP_A = '&';
    public static final Charset UTF_8 = Charset.forName("UTF-8");

    public static String format(
            final List<? extends NameValuePair> parameters,
            final char parameterSeparator,
            final String charset) {
        final StringBuilder result = new StringBuilder();
        for (final NameValuePair parameter : parameters) {
            final String encodedName = encodeFormFields(parameter.getName(), charset);
            final String encodedValue = encodeFormFields(parameter.getValue(), charset);
            if (result.length() > 0) {
                result.append(parameterSeparator);
            }
            result.append(encodedName);
            if (encodedValue != null) {
                result.append(NAME_VALUE_SEPARATOR);
                result.append(encodedValue);
            }
        }
        return result.toString();
    }

    private static String encodeFormFields(final String content, final String charset) {
        if (content == null) {
            return null;
        }
        return urlEncode(content, charset != null ? Charset.forName(charset) : UTF_8, URLENCODER, true);
    }

    private static String urlEncode(
            final String content,
            final Charset charset,
            final BitSet safechars,
            final boolean blankAsPlus) {
        if (content == null) {
            return null;
        }
        final int RADIX = 16;
        final StringBuilder buf = new StringBuilder();
        final ByteBuffer bb = charset.encode(content);
        while (bb.hasRemaining()) {
            final int b = bb.get() & 0xff;
            if (safechars.get(b)) {
                buf.append((char) b);
            } else if (blankAsPlus && b == ' ') {
                buf.append('+');
            } else {
                buf.append("%");
                final char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, RADIX));
                final char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, RADIX));
                buf.append(hex1);
                buf.append(hex2);
            }
        }
        return buf.toString();
    }
    //=====end=====拷自http源码 格式化参数及encode用

    /**
     * 文件参数描述类
     */
    public static class MyFileWrapper implements Serializable {
        private static final long serialVersionUID = 3427261911165920527L;
        public final File file;
        public final String contentType;
        public final String customFileName;

        public MyFileWrapper(File file, String contentType,
                             String customFileName) {
            this.file = file;
            this.contentType = contentType;
            this.customFileName = customFileName;
        }
    }

    /**
     * 字节数组参数描述类
     */
    public static class MyBytesWrapper implements Serializable {
        private static final long serialVersionUID = 3427261911165920527L;
        public final byte[] bytes;
        public final String contentType;
        public final String customName;

        public MyBytesWrapper(byte[] bytes, String contentType,
                              String customName) {
            this.bytes = bytes;
            this.contentType = contentType;
            this.customName = customName;
        }
    }

    /**
     * 流参数描述类
     */
    public static class MyStreamWrapper {
        public final InputStream inputStream;
        public final String name;
        public final String contentType;
        public final boolean autoClose;
        public final static String APPLICATION_OCTET_STREAM = "application/octet-stream";

        public MyStreamWrapper(InputStream inputStream, String name,
                               String contentType, boolean autoClose) {
            this.inputStream = inputStream;
            this.name = name;
            this.contentType = contentType;
            this.autoClose = autoClose;
        }

        static MyStreamWrapper newInstance(InputStream inputStream,
                                           String name, String contentType, boolean autoClose) {
            return new MyStreamWrapper(inputStream, name,
                    contentType == null ? APPLICATION_OCTET_STREAM
                            : contentType, autoClose);
        }
    }

    public interface NameValuePair {
        String getName();

        String getValue();
    }

    public static class ExtraPostBody{
        String mediaType;
        byte[] data;

        public ExtraPostBody(String mediaType, byte[] data) {
            this.mediaType = mediaType;
            this.data = data;
        }
    }

    /**
     * 拷自http源码
     */
    public static class BasicNameValuePair implements NetParams.NameValuePair, Cloneable, Serializable {

        private static final long serialVersionUID = -6437800749411518984L;

        private final String name;
        private final String value;

        public BasicNameValuePair(final String name, final String value) {
            super();
            if (name == null) {
                throw new IllegalArgumentException("Name may not be null");
            }
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return this.name;
        }

        public String getValue() {
            return this.value;
        }

        public String toString() {
            // don't call complex default formatting for a simple toString

            if (this.value == null) {
                return name;
            } else {
                int len = this.name.length() + 1 + this.value.length();
                StringBuilder buffer = new StringBuilder(len);
                buffer.append(this.name);
                buffer.append("=");
                buffer.append(this.value);
                return buffer.toString();
            }
        }

        public boolean equals(final Object object) {
            if (object == null) return false;
            if (this == object) return true;
            if (object instanceof NetParams.NameValuePair) {
                BasicNameValuePair that = (BasicNameValuePair) object;
                return this.name.equals(that.name)
                        && equals(this.value, that.value);
            } else {
                return false;
            }
        }

        public int hashCode() {
            int hash = HASH_SEED;
            hash = hashCode(hash, this.name);
            hash = hashCode(hash, this.value);
            return hash;
        }

        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }


        //以下为拷自 org.apache.commons.httpclient.util.LangUtils的工具方法
        public static final int HASH_SEED = 17;
        public static final int HASH_OFFSET = 37;

        public static int hashCode(final int seed, final int hashcode) {
            return seed * HASH_OFFSET + hashcode;
        }

        public static int hashCode(final int seed, final Object obj) {
            return hashCode(seed, obj != null ? obj.hashCode() : 0);
        }

        public static boolean equals(final Object obj1, final Object obj2) {
            return obj1 == null ? obj2 == null : obj1.equals(obj2);
        }
    }
}
