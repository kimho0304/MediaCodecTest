package com.example.mediacodectest;

import android.media.MediaFormat;

public class TransInfo {
    private static MediaFormat format;
    private static byte[] payloads;
    TransInfo(){
    }

    public static void setFormat(MediaFormat input){
        format = input;
    }
    public static MediaFormat getFormat(){
        return format;
    }

    public static void setPayloads(byte[] input){
        payloads = input;
    }
    public static byte[] getPayloads(){
        return payloads;
    }

    private static void accumulator(){

    }
}
