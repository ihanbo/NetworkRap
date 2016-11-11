package com.yiche.net;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ihanb on 2016/11/10.
 */

public interface IReponse {
    byte[] bytes() throws IOException;
    InputStream byteStream();
    String string() throws IOException;
}
