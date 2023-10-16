package com.example.mediacodectest;
// https://github.com/mstorsjo/android-decodeencodetest/blob/master/src/com/example/decodeencodetest/TextureRender.java

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

public class EncodingActivity extends AppCompatActivity {
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
    private MediaFormat outputVideoFormat;
    private VideoView videoView;

    private TextureView mTextureView;
    private Button playBtn, loadBtn;
    MediaExtractor videoExtractor = null;
    MediaExtractor audioExtractor = null;
    OutputSurface outputSurface = null;
    MediaCodec videoDecoder = null;
    MediaCodec audioDecoder = null;
    MediaCodec videoEncoder = null;
    MediaCodec audioEncoder = null;
    MediaMuxer muxer = null;
    private File resultFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encoding);

        videoView = findViewById(R.id.playView);
        // 비디오 리스너 등록.
        videoView.setOnPreparedListener(mp -> {
            // 준비 완료되면 비디오 재생.
            mp.setLooping(true); // 비디오 무한루프 설정: true.
            // mp.start(); // 비디오 재생 시작.
        });

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
            videoView.start();
        });

        MediaCodecInfo videoCodecInfo = selectCodec(OUTPUT_VIDEO_MIME_TYPE);

        outputVideoFormat =
                MediaFormat.createVideoFormat(OUTPUT_VIDEO_MIME_TYPE, mWidth, mHeight);
        outputVideoFormat.setInteger(
                MediaFormat.KEY_COLOR_FORMAT, OUTPUT_VIDEO_COLOR_FORMAT);
        outputVideoFormat.setInteger(MediaFormat.KEY_BIT_RATE, OUTPUT_VIDEO_BIT_RATE);
        outputVideoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, OUTPUT_VIDEO_FRAME_RATE);
        outputVideoFormat.setInteger(
                MediaFormat.KEY_I_FRAME_INTERVAL, OUTPUT_VIDEO_IFRAME_INTERVAL);

        if (VERBOSE) Log.d(TAG, "video format: " + outputVideoFormat);

        // Create a MediaCodec for the desired codec, then configure it as an encoder with
        // our desired properties. Request a Surface to use for input.
        AtomicReference<Surface> inputSurfaceReference = new AtomicReference<Surface>();
    }

    private void playVideo(){
        MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            // Set the data source to the new video file
            mediaPlayer.setDataSource(resultFile.getAbsolutePath());

            // Prepare and start playback
            mediaPlayer.prepare();
            mediaPlayer.start();

            // Set up event listeners (optional)
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // Handle playback completion
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    // Handle playback error
                    return false; // Return true if the error is handled
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

// Release the MediaPlayer when done
        mediaPlayer.release();
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
        long presentationTimeUs = 0;
        for (byte[] frameData : TransInfo.getPaysArray()) {
            ByteBuffer buffer = ByteBuffer.wrap(frameData);
            bufferInfo = new MediaCodec.BufferInfo();

            presentationTimeUs = extractor.getSampleTime();
            bufferInfo.presentationTimeUs = presentationTimeUs; // Set presentation time
            bufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME; // Set flags if needed

            // Write the video frame data to the muxer
            mediaMuxer.writeSampleData(videoTrackIndex, buffer, bufferInfo);

            // Increment presentation time if necessary
            // presentationTimeUs += frameDurationUs; // Adjust frame duration as needed
        }

        // Stop and release the MediaMuxer
        mediaMuxer.stop();
        mediaMuxer.release();

        bufferOS.close();

        videoView.setVideoPath(filePath);
    }

    private MediaCodec createVideoEncoder(MediaCodecInfo codecInfo, MediaFormat format, AtomicReference<Surface> surfaceReference) throws IOException {
        MediaCodec encoder = MediaCodec.createByCodecName(codecInfo.getName());
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        // Must be called before start() is.
        surfaceReference.set(encoder.createInputSurface());
        encoder.start();
        return encoder;
    }

    /**
     * Returns the first codec capable of encoding the specified MIME type, or null if no match was
     * found.
     */
    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }
}
