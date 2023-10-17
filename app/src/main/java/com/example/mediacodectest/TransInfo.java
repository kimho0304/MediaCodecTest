package com.example.mediacodectest;

import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.util.ArrayList;

public class TransInfo {
    private static MediaFormat format;
    private static MediaExtractor extractor = new MediaExtractor();;
    private static byte[] payloads;
    private static boolean state = false;
    private static ArrayList<byte[]> paysBuffer = new ArrayList<>(0);
    private static ArrayList<Long> presentationTimeUs  = new ArrayList<>(0);;
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
        presentationTimeUs.add(input);
    }
    public static ArrayList<Long> getPresentationTimeUs(){
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
    public static void setState(Boolean input){
        state = input;
    }
    public static Boolean getState(){
        return state;
    }
}
