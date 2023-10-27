package com.example.mediacodectest;
// https://github.com/mstorsjo/android-decodeencodetest/blob/master/src/com/example/decodeencodetest/TextureRender.java

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.Button;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class EncodingActivity extends AppCompatActivity  implements SurfaceHolder.Callback {
    private static final String TAG = "EncodingActivity_Debug";
    private static final boolean VERBOSE = true; // lots of logging
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
    private VideoView videoView;
    private SurfaceHolder surfaceHolder;
    private MediaPlayer mediaPlayer;
    private TextureView mTextureView;
    private Surface outputSurface;
    private Button playBtn, loadBtn;
    private File resultFile;
    // private Uri tmpFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encoding);

        /*
        tmpFile = getIntent().getParcelableExtra("file");
        Log.i(TAG, tmpFile.toString());
        videoView = findViewById(R.id.videoView);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() { // 비디오 리스너 등록.
            @Override
            public void onPrepared(MediaPlayer mp) {
                // 준비 완료되면 비디오 재생.
                mp.setLooping(true); // 비디오 무한루프 설정: true.
                //mp.start(); // 비디오 재생 시작.
            }
        });*/

        surfaceView = findViewById(R.id.playView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        loadBtn = findViewById(R.id.loadBtn);
        loadBtn.setOnClickListener(view->{
            try {
                createVideo();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        playBtn = findViewById(R.id.playBtn);
        playBtn.setOnClickListener(view-> playVideo());
    }

    private void playVideo(){
        try {
            // Set the data source to the new video file
            mediaPlayer = new MediaPlayer();
          /*  File fuck = getFile(getApplicationContext(), tmpFile);
            Log.i(TAG, fuck.toString());*/
            //videoView.setVideoPath(resultFile.getAbsolutePath());
            //videoView.start();
            mediaPlayer.setDataSource(resultFile.getAbsolutePath());
            Log.i(TAG, "resultFile info: " + resultFile.getAbsolutePath());

            Log.i(TAG, "setDataSource() worked.");
            mediaPlayer.setDisplay(surfaceHolder);
            Log.i(TAG, "setDisplay() worked.");
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                Log.e(TAG, "Failed to prepare: " + e);
                e.printStackTrace();
            }
            mediaPlayer.start();
            Log.i(TAG, "start() worked.");

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
            //resultFile = new File("/data/user/0/com.example.mediacodectest/cache/tmp.mp4");
            //resultFile.createNewFile();
            Log.i(TAG, "New video size: " + resultFile.length());
            Log.i(TAG, "New video path: " + resultFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Failed to create new tmp file: " + e);
            e.printStackTrace();
        }

        String filePath = resultFile.getAbsolutePath();


        /*
        . Muxer에는 그냥 저장하시면 의미 없습니다.

            디코딩이 된 데이터는 NV21/I420의 색상값을 가지는 그냥 단순 데이터입니다. 압축된 데이터가 아니기에 Muxer에 저장할 수 없죠. 안드로이드의 Muxer에서는 MediaCodec Encoder 데이터를 가져와야 합니다.

            그리고 다시...

            기존 영상의 5프레임을 건너뛴 연상을 그냥 해당 프레임만 file로 저장하시면 이상한 이미지가 나옵니다.

            우선 동영상에 대한 기본 개념을 찾아보시고 작업하시면 좋을것 같습니다.....(앞에 추가해드린 링크에 대부분? 포함이 되어 있긴합니다.)

         */

        //------------------encoding--------------------
        MediaFormat originalFormat = TransInfo.getFormat();

        MediaFormat encodingFormat = MediaFormat.createVideoFormat("video/avc", originalFormat.getInteger(MediaFormat.KEY_HEIGHT), originalFormat.getInteger(MediaFormat.KEY_WIDTH));
        encodingFormat.setInteger(MediaFormat.KEY_BIT_RATE, 125000);
        encodingFormat.setInteger(MediaFormat.KEY_FRAME_RATE, originalFormat.getInteger(MediaFormat.KEY_FRAME_RATE));
        encodingFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, 2130708361);
        encodingFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);



        MediaCodec encoder = MediaCodec.createEncoderByType("video/avc");
        MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();

        encoder.configure(encodingFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        //outputSurface = encoder.createInputSurface();

        encoder.start();








        //--------------------------------------

        // Create a MediaMuxer for the new video file
        /*MediaMuxer mediaMuxer = new MediaMuxer(filePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        //extractor.setDataSource(filePath);
        Log.d(TAG, "mediaMuxer: " + mediaMuxer);

        // Add the video track with the original MediaFormat
        int videoTrackIndex = mediaMuxer.addTrack(originalFormat);
        Log.d(TAG, "original format: " + originalFormat); // 정상.
        Log.d(TAG, "videoTrackIndex: " + videoTrackIndex); // 정상.

        // Start the muxer
        mediaMuxer.start();

        // Loop through your video frame payloads and write them to the new file
        ArrayList<byte[]> payloadsList = TransInfo.getPaysArray();
        ArrayList<Long> presentationTimeUsList = TransInfo.getPresentationTimeUs();
        int l = presentationTimeUsList.size(); // == payloadsList.size().
        byte[] frameData = null;
        Log.i(TAG, "payloads length: " + payloadsList.size() +", times length: "+presentationTimeUsList.size());
        // -> same for length.

        for (int i = 0; i < l-1; i++) {
            frameData = payloadsList.get(i);
            ByteBuffer buffer = ByteBuffer.wrap(frameData);
            Log.d(TAG, "buffer info: " + frameData.length);

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            bufferInfo.presentationTimeUs = presentationTimeUsList.get(i); // Set presentation time
            bufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME; // Set flags if needed

            // Write the video frame data to the muxer
            mediaMuxer.writeSampleData(videoTrackIndex, buffer, bufferInfo);
        }
        */
        Log.i(TAG, "End of encoding.");
        Log.i(TAG, "New video size: " + resultFile.length());
        Log.i(TAG, "New video info: " + resultFile.canExecute());

        // Stop and release the MediaMuxer
        //mediaMuxer.stop();
        //mediaMuxer.release();

        // bufferOS.close();
        //extractor.release();
        //TransInfo.getExtractor().release();

        // surfaceView.setVideoPath(filePath);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged worked.");
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        if (mediaPlayer != null)
            mediaPlayer.release();

        Log.i(TAG, "surfaceDestroyed worked.");
    }


    // https://stackoverflow.com/questions/65447194/how-to-convert-uri-to-file-android-10
    public static File getFile(Context context, Uri uri) throws IOException {
        File destinationFilename = new File(context.getFilesDir().getPath() + File.separatorChar + queryName(context.getContentResolver(), uri));
        try (InputStream ins = context.getContentResolver().openInputStream(uri)) {
            createFileFromStream(ins, destinationFilename);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }
        return destinationFilename;
    }

    // https://stackoverflow.com/questions/65447194/how-to-convert-uri-to-file-android-10
    public static void createFileFromStream(InputStream ins, File destination) {
        try (OutputStream os = new FileOutputStream(destination)) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = ins.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
        } catch (Exception ex) {
            Log.e("Save File", ex.getMessage());
            ex.printStackTrace();
        }
    }

    // https://stackoverflow.com/a/38304115
    private static String queryName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor = resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();

        String[] result = name.split("\\."); // '.'을 기준으로 확장명 분리 ex) ".txt"
        return result[0];
    }
}
