package com.example.secupay_jni.Utils;


import android.util.Base64;

public class Base64Utils {

    public static String encodeToString(byte[] input) {
        return Base64.encodeToString(input, Base64.DEFAULT);
    }
}