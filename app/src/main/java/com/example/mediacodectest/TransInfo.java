package com.example.mediacodectest;

import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.util.ArrayList;

public class TransInfo {
    private static int trackInx;
    private static MediaFormat format;
    private static MediaExtractor extractor = new MediaExtractor();;
    private static byte[] payloads;
    private static boolean state = false;
    private static ArrayList<byte[]> decodeBytes = new ArrayList<>(0);
    private static ArrayList<byte[]> encodeBytes = new ArrayList<>(0);
    private static ArrayList<Long> presentationTimeUs  = new ArrayList<>(0);
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
        setDecodeBytes(payloads);
    }
    public static byte[] getPayloads(){
        return payloads;
    }
    public static void setDecodeBytes(byte[] input){
        decodeBytes.add(input);
    }
    public static ArrayList<byte[]> getDecodeBytes(){
        return decodeBytes;
    }
    public static void setEncodeBytes(byte[] input){
        encodeBytes.add(input);
    }
    public static ArrayList<byte[]> getEncodeBytes(){
        return encodeBytes;
    }
    public static void setState(Boolean input){
        state = input;
    }
    public static Boolean getState(){
        return state;
    }
    public static void setTrackInx(int input){
        trackInx = input;
    }
    public static int getTrackInx(){
        return trackInx;
    }
}
