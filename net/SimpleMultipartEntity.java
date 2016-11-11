/*
    Android Asynchronous Http Client
    Copyright (c) 2011 James Smith <james@loopj.com>
    https://loopj.com

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

/*
    This code is taken from Rafael Sanches' blog. Link is no longer working (as of 17th July 2015)
    https://blog.rafaelsanches.com/2011/01/29/upload-using-multipart-post-using-httpclient-in-android/
*/

package com.yiche.net;

import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Simplified multipart entity mainly used for sending one or more files.
 */
public class SimpleMultipartEntity {

    public final static String APPLICATION_OCTET_STREAM =
            "application/octet-stream";

    public final static String APPLICATION_JSON =
            "application/json";

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_CONTENT_RANGE = "Content-Range";
    public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
    public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    public static final String ENCODING_GZIP = "gzip";


    private static final String LOG_TAG = "SimpleMultipartEntity";

    private static final String STR_CR_LF = "\r\n";
    private static final byte[] CR_LF = STR_CR_LF.getBytes();
    private static final byte[] TRANSFER_ENCODING_BINARY =
            ("Content-Transfer-Encoding: binary" + STR_CR_LF).getBytes();

    private final static char[] MULTIPART_CHARS =
            "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private final String boundary;
    private final byte[] boundaryLine;
    private final byte[] boundaryEnd;
    private final List<FilePart> fileParts = new ArrayList<>();
    // The buffer we use for building the message excluding files and the last
    // boundary
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private boolean isRepeatable;
    private long bytesWritten;

    private long totalSize;

    public SimpleMultipartEntity() {
        final StringBuilder buf = new StringBuilder();
        final Random rand = new Random();
        for (int i = 0; i < 30; i++) {
            buf.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }

        boundary = buf.toString();
        boundaryLine = ("--" + boundary + STR_CR_LF).getBytes();
        boundaryEnd = ("--" + boundary + "--" + STR_CR_LF).getBytes();

    }

    public void addPart(String key, String value, String contentType) {
        try {
            out.write(boundaryLine);
            out.write(createContentDisposition(key));
            out.write(createContentType(contentType));
            out.write(CR_LF);
            out.write(value.getBytes());
            out.write(CR_LF);
        } catch (final IOException e) {
            // Shall not happen on ByteArrayOutputStream
            Log.e(LOG_TAG, "addPart ByteArrayOutputStream exception", e);
        }
    }

    public void addPartWithCharset(String key, String value, String charset) {
        if (charset == null) charset = "UTF_8";
        addPart(key, value, "text/plain; charset=" + charset);
    }

    public void addPart(String key, String value) {
        addPartWithCharset(key, value, null);
    }

    public void addPart(String key, File file) {
        addPart(key, file, null);
    }

    public void addPart(String key, File file, String type) {
        fileParts.add(new FilePart(key, file, normalizeContentType(type)));
    }

    public void addPart(String key, File file, String type, String customFileName) {
        fileParts.add(new FilePart(key, file, normalizeContentType(type), customFileName));
    }

    public void addPart(String key, String streamName, InputStream inputStream, String type)
            throws IOException {

        out.write(boundaryLine);

        // Headers
        out.write(createContentDisposition(key, streamName));
        out.write(createContentType(type));
        out.write(TRANSFER_ENCODING_BINARY);
        out.write(CR_LF);

        // Stream (file)
        final byte[] tmp = new byte[4096];
        int l;
        while ((l = inputStream.read(tmp)) != -1) {
            out.write(tmp, 0, l);
        }

        out.write(CR_LF);
        out.flush();
    }

    private String normalizeContentType(String type) {
        return type == null ? APPLICATION_OCTET_STREAM : type;
    }

    private byte[] createContentType(String type) {
        String result = HEADER_CONTENT_TYPE + ": " + normalizeContentType(type) + STR_CR_LF;
        return result.getBytes();
    }

    private byte[] createContentDisposition(String key) {
        return (
                HEADER_CONTENT_DISPOSITION +
                        ": form-data; name=\"" + key + "\"" + STR_CR_LF).getBytes();
    }

    private byte[] createContentDisposition(String key, String fileName) {
        return (
                HEADER_CONTENT_DISPOSITION +
                        ": form-data; name=\"" + key + "\"" +
                        "; filename=\"" + fileName + "\"" + STR_CR_LF).getBytes();
    }

    private void updateProgress(long count) {
        bytesWritten += count;
    }

    public long getContentLength() {
        long contentLen = out.size();
        for (FilePart filePart : fileParts) {
            long len = filePart.getTotalLength();
            if (len < 0) {
                return -1; // Should normally not happen
            }
            contentLen += len;
        }
        contentLen += boundaryEnd.length;
        return contentLen;
    }


    public String[] getContentType() {
        return new String[]{
                HEADER_CONTENT_TYPE,
                "multipart/form-data; boundary=" + boundary};
    }

    public boolean isChunked() {
        return false;
    }

    public void setIsRepeatable(boolean isRepeatable) {
        this.isRepeatable = isRepeatable;
    }

    public boolean isRepeatable() {
        return isRepeatable;
    }

    public boolean isStreaming() {
        return false;
    }

    public void writeTo(final OutputStream outstream) throws IOException {
        bytesWritten = 0;
        totalSize = (int) getContentLength();
        out.writeTo(outstream);
        updateProgress(out.size());

        for (FilePart filePart : fileParts) {
            filePart.writeTo(outstream);
        }
        outstream.write(boundaryEnd);
        updateProgress(boundaryEnd.length);
    }

    public void getContentEncoding() {
        return ;
    }

    public void consumeContent() throws IOException, UnsupportedOperationException {
        if (isStreaming()) {
            throw new UnsupportedOperationException(
                    "Streaming entity does not implement #consumeContent()");
        }
    }

    public InputStream getContent() throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "getContent() is not supported. Use writeTo() instead.");
    }

    private class FilePart {
        public final File file;
        public final byte[] header;

        public FilePart(String key, File file, String type, String customFileName) {
            header = createHeader(key, TextUtils.isEmpty(customFileName) ? file.getName() : customFileName, type);
            this.file = file;
        }

        public FilePart(String key, File file, String type) {
            header = createHeader(key, file.getName(), type);
            this.file = file;
        }

        private byte[] createHeader(String key, String filename, String type) {
            ByteArrayOutputStream headerStream = new ByteArrayOutputStream();
            try {
                headerStream.write(boundaryLine);

                // Headers
                headerStream.write(createContentDisposition(key, filename));
                headerStream.write(createContentType(type));
                headerStream.write(TRANSFER_ENCODING_BINARY);
                headerStream.write(CR_LF);
            } catch (IOException e) {
                // Can't happen on ByteArrayOutputStream
                Log.e(LOG_TAG, "createHeader ByteArrayOutputStream exception", e);
            }
            return headerStream.toByteArray();
        }

        public long getTotalLength() {
            long streamLength = file.length() + CR_LF.length;
            return header.length + streamLength;
        }

        public void writeTo(OutputStream out) throws IOException {
            out.write(header);
            updateProgress(header.length);

            FileInputStream inputStream = new FileInputStream(file);
            final byte[] tmp = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(tmp)) != -1) {
                out.write(tmp, 0, bytesRead);
                updateProgress(bytesRead);
            }
            out.write(CR_LF);
            updateProgress(CR_LF.length);
            out.flush();
            silentCloseInputStream(inputStream);
        }
    }

    public static void silentCloseInputStream(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch (IOException e) {
            Log.w(LOG_TAG, "Cannot close input stream", e);
        }
    }

    private static SimpleMultipartEntity createMultipartEntity(NetParams np) {
        SimpleMultipartEntity entity = new SimpleMultipartEntity();
        for (ConcurrentHashMap.Entry<String, String> entry : np.urlParams.entrySet()) {
            entity.addPartWithCharset(entry.getKey(), entry.getValue(), np.contentEncoding);
        }


        // Add stream params
        for (ConcurrentHashMap.Entry<String, NetParams.MyStreamWrapper> entry : np.streamParams.entrySet()) {
            NetParams.MyStreamWrapper stream = entry.getValue();
            if (stream.inputStream != null) {
                try {
                    entity.addPart(entry.getKey(), stream.name, stream.inputStream,
                            stream.contentType);
                } catch (IOException e) {
                    e.printStackTrace();
                    NetUtils.log("SimpleMultipartEntity createMultipartEntity addFile error !!!!!!!");
                }
            }
        }

        // Add byte[] params
        for (ConcurrentHashMap.Entry<String, NetParams.MyBytesWrapper> entry : np.bytesParams.entrySet()) {
            NetParams.MyBytesWrapper bytesWrapper = entry.getValue();
            if (bytesWrapper.bytes != null) {
                try {
                    entity.addPart(entry.getKey(), bytesWrapper.customName, new ByteArrayInputStream(bytesWrapper.bytes),
                            bytesWrapper.contentType);
                } catch (IOException e) {
                    e.printStackTrace();
                    NetUtils.log("SimpleMultipartEntity createMultipartEntity addFile error !!!!!!!");
                }
            }
        }

        // Add file params
        for (ConcurrentHashMap.Entry<String, NetParams.MyFileWrapper> entry : np.fileParams.entrySet()) {
            NetParams.MyFileWrapper fileWrapper = entry.getValue();
            entity.addPart(entry.getKey(), fileWrapper.file, fileWrapper.contentType, fileWrapper.customFileName);
        }
        return entity;
    }


    public static byte[] getMultipartBody(NetParams np) {
        SimpleMultipartEntity mMultiPartEntity = createMultipartEntity(np);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            // 将mMultiPartEntity中的参数写入到bos中
            mMultiPartEntity.writeTo(bos);
        } catch (IOException e) {
            Log.e("", "IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }
}
