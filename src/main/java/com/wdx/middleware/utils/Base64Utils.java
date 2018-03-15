package com.wdx.middleware.utils;

import sun.misc.BASE64Encoder;

public class Base64Utils {

    private static  BASE64Encoder base64Encoder = new BASE64Encoder();

    public static String encode(String value){
        return base64Encoder.encode(value.getBytes());
    }
}
