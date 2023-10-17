package com.example.mediacodectest;
// https://github.com/mstorsjo/android-decodeencodetest/blob/master/src/com/example/decodeencodetest/TextureRender.java

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaMuxer;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class EncodingActivity extends AppCompatActivity  implements SurfaceHolder.Callback {
    private static final String TAG = "EncodingActivity_Debug";
    private static final boolean VERBOSE = false; // lots of logging
    // parameters for the video encoder
    private static final String OUTPUT_VIDEO_MIME_TYPE = "video/avc"; // H.264 Advanced Video Coding
    private static final int OUTPUT_VIDEO_BIT_RATE = 2000000; // 2Mbps
    private static final int OUTPUT_VIDEO_FRAME_RATE = 15; // 15fps
    private static final int OUTPUT_VIDEO_IFRAME_INTERVAL = 10; // 10 seconds between I-frames
    private static final int OUTPUT_VIDEO_COLOR_FORMAT =
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;

    /**
     * Width of the output frames.
     */
    private int mWidth = -1;
    /**
     * Height of the output frames.
     */
    private int mHeight = -1;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private MediaPlayer mediaPlayer;
    private TextureView mTextureView;
    private Button playBtn, loadBtn;
    private File resultFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encoding);

        surfaceView = findViewById(R.id.playView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        // 비디오 리스너 등록.
        /* surfaceView.setOnPreparedListener(mp -> {
            // 준비 완료되면 비디오 재생.
            mp.setLooping(true); // 비디오 무한루프 설정: true.
            // mp.start(); // 비디오 재생 시작.
        }); */

        loadBtn = findViewById(R.id.loadBtn);
        loadBtn.setOnClickListener(view->{
            try {
                createVideo();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        playBtn = findViewById(R.id.playBtn);
        playBtn.setOnClickListener(view->{
            playVideo();
        });
    }

    private void playVideo(){
        mediaPlayer = new MediaPlayer();
        try {
            // Set the data source to the new video file
            mediaPlayer.setDataSource(resultFile.getAbsolutePath());
            Log.i(TAG, "setDataSource worked.");
            mediaPlayer.setDisplay(surfaceHolder);
            Log.i(TAG, "surfaceCreated worked.");
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            mediaPlayer.start();
            Log.i(TAG, "start worked.");
            // Prepare and start playback
            /*mediaPlayer.prepare();
            mediaPlayer.start();*/
        } catch (IOException e) {
            Log.e(TAG, "Failed to play the file: " + e);
            e.printStackTrace();
        }

// Release the MediaPlayer when done
        // mediaPlayer.release();
    }

    private void createVideo() throws IOException {
        try {
            resultFile = File.createTempFile("tmp", "mp4");
        } catch (IOException e) {
            Log.e(TAG, "Failed to create new tmp file: " + e);
            e.printStackTrace();
        }

        String filePath = resultFile.getAbsolutePath();

        BufferedOutputStream bufferOS = null;
        try {
            bufferOS = new BufferedOutputStream(new FileOutputStream(resultFile));
            Log.i(TAG, "bufferOS Info: " + bufferOS);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException: " + e);
            e.printStackTrace();
        }

        // Create a MediaMuxer for the new video file
        MediaMuxer mediaMuxer = new MediaMuxer(filePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        Log.d(TAG, "mediaMuxer: " + mediaMuxer);

        // Add the video track with the original MediaFormat
        int videoTrackIndex = mediaMuxer.addTrack(TransInfo.getFormat());
        Log.d(TAG, "videoTrackIndex: " + videoTrackIndex);

        // Start the muxer
        mediaMuxer.start();

        MediaExtractor extractor = TransInfo.getExtractor();
        Log.d(TAG, "extractor: " + extractor);
        MediaCodec.BufferInfo bufferInfo = null;

        // Loop through your video frame payloads and write them to the new file
        ArrayList<byte[]> payloadsList = TransInfo.getPaysArray();
        ArrayList<Long> presentationTimeUsList = TransInfo.getPresentationTimeUs();
        int l = presentationTimeUsList.size();
        byte[] frameData = null;
        long presentationTimeUs = 0;
        Log.i(TAG, "payloads length: " + payloadsList.size() +", times length: "+presentationTimeUsList.size());

        for (int i = 0; i < l; i++) {
            frameData = payloadsList.get(i);
            ByteBuffer buffer = ByteBuffer.wrap(frameData);
            bufferInfo = new MediaCodec.BufferInfo();
            Log.d(TAG, "buffer info: " + frameData.length);

            presentationTimeUs = presentationTimeUsList.get(i);
            bufferInfo.presentationTimeUs = presentationTimeUs; // Set presentation time
            bufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME; // Set flags if needed

            // Write the video frame data to the muxer
            mediaMuxer.writeSampleData(videoTrackIndex, buffer, bufferInfo);
            // extractor.advance();
            // Increment presentation time if necessary
            // presentationTimeUs += frameDurationUs; // Adjust frame duration as needed
        }
        Log.i(TAG, "End of encoding.");

        // Stop and release the MediaMuxer
        mediaMuxer.stop();
        mediaMuxer.release();

        bufferOS.close();
        extractor.release();
        TransInfo.getExtractor().release();

        // surfaceView.setVideoPath(filePath);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mediaPlayer.setDisplay(surfaceHolder);
        Log.i(TAG, "surfaceCreated worked.");
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mediaPlayer.start();
        Log.i(TAG, "start worked.");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged worked.");
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed worked.");
    }
}
