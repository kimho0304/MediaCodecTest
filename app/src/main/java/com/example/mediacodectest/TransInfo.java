package com.example.mediacodectest;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.util.ArrayList;

public class TransInfo {
    private static MediaFormat format;
    private static ArrayList<MediaCodec.BufferInfo> bufferInfo = new ArrayList<>();
    private static MediaExtractor extractor = new MediaExtractor();
    private static byte[] payloads;
    private static boolean state = false;
    private static ArrayList<byte[]> decodeBytes = new ArrayList<>(0);
    private static ArrayList<byte[]> encodeBytes = new ArrayList<>(0);
    private static ArrayList<Long> presentationTimeUs = new ArrayList<>(0);
    private static ArrayList<Integer> trackInx = new ArrayList<>(0);

    TransInfo() {
    }

    public static void setTrackInx(int input) {
        trackInx.add(input);
    }

    public static ArrayList<Integer> getTrackInx(){
        return trackInx;
    }

    public static void setFormat(MediaFormat input) {
        format = input;
    }

    public static MediaFormat getFormat() {
        return format;
    }

    public static void setExtractor(MediaExtractor input) {
        extractor = input;
    }

    public static MediaExtractor getExtractor() {
        return extractor;
    }

    public static void setPresentationTimeUs(long input) {
        presentationTimeUs.add(input);
    }

    public static ArrayList<Long> getPresentationTimeUs() {
        return presentationTimeUs;
    }

    public static void setBufferInfo(long curTime, int curFlag) {
        MediaCodec.BufferInfo tmpInfo = new MediaCodec.BufferInfo();
        tmpInfo.presentationTimeUs = curTime;
        tmpInfo.flags = curFlag;

        bufferInfo.add(tmpInfo);
    }

    public static ArrayList<MediaCodec.BufferInfo> getBufferinfo() {
        return bufferInfo;
    }

    public static void setPayloads(byte[] input) {
        payloads = input;
        setDecodeBytes(payloads);
    }

    public static byte[] getPayloads() {
        return payloads;
    }

    public static void setDecodeBytes(byte[] input) {
        decodeBytes.add(input);
    }

    public static ArrayList<byte[]> getDecodeBytes() {
        return decodeBytes;
    }

    public static void setEncodeBytes(byte[] input) {
        encodeBytes.add(input);
    }

    public static ArrayList<byte[]> getEncodeBytes() {
        return encodeBytes;
    }

    public static void setState(Boolean input) {
        state = input;
    }

    public static Boolean getState() {
        return state;
    }
}
