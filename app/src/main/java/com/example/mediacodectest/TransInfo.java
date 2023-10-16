package com.example.mediacodectest;

import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.util.ArrayList;

public class TransInfo {
    private static MediaFormat format;
    private static MediaExtractor extractor;
    private static byte[] payloads;
    static boolean state = false;
    private static ArrayList<byte[]> paysBuffer = new ArrayList<>(0);
    private static long presentationTimeUs;
    TransInfo(){
    }
    public static void setFormat(MediaFormat input){
        format = input;
    }
    public static MediaFormat getFormat(){
        return format;
    }
    public static void setExtractor(MediaExtractor input){
        extractor = input;
    }
    public static MediaExtractor getExtractor(){
        return extractor;
    }

    public static void setPresentationTimeUs(long input){
        presentationTimeUs = input;
    }
    public static long getPresentationTimeUs(){
        return presentationTimeUs;
    }
    public static void setPayloads(byte[] input){
        payloads = input;
        setPaysArray(payloads);
    }
    public static byte[] getPayloads(){
        return payloads;
    }
    public static void setPaysArray(byte[] input){
        paysBuffer.add(input);
    }
    public static ArrayList<byte[]> getPaysArray(){
        return paysBuffer;
    }

    private static void accumulator(){

    }
}
