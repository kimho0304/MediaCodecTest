package com.example.mediacodectest;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DecodingActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        TextureView.SurfaceTextureListener, DecodingClass.PlayerFeedback {
    private static final String TAG = "DecodingActivity_Debug";
    private Uri selectedFile;
    private File fileFromUri;
    private DecodingClass decodeObj;
    private DecodingClass.FrameCallback mFrameCallBack;

    // UI Initializaion ↓:
    private TextureView mTextureView;
    private boolean mShowStopLabel = false;
    private DecodingClass.PlayTask mPlayTask;
    private boolean mSurfaceTextureReady = false;

    private Button playBtn, encodeBtn;

    public void onResume() {
        super.onResume();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void onPause() {
        super.onPause();

        if (mPlayTask != null) {
            stopPlayback();
            mPlayTask.waitForStop();
        }
    }

    private void stopPlayback() {
        if (mPlayTask != null) {
            mPlayTask.requestStop();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decoding);
        // 메인 액티비티에서 선택한 파일의 Uri를 받을 때, getParcelableExtra 메소드 사용 필수.
        selectedFile = getIntent().getParcelableExtra("file");
        Log.i(TAG, selectedFile.toString());
        mTextureView = findViewById(R.id.playView);
        mTextureView.setSurfaceTextureListener(this);

        try {
            fileFromUri = getFile(getApplicationContext(), selectedFile);
            Log.i(TAG, "fileFromUri info: " + fileFromUri);
        } catch (IOException e) {
            Log.e(TAG, "getFile() error.");
            e.printStackTrace();
        }

        encodeBtn = findViewById(R.id.encodeBtn);
        encodeBtn.setEnabled(false);
        encodeBtn.setOnClickListener(view -> {
            Intent toEncode = new Intent(this, EncodingActivity.class);
            // toEncode.putExtra("file", selectedFile);
            startActivity(toEncode);
        });

        playBtn = findViewById(R.id.playBtn);
        playBtn.setOnClickListener(view->{
            clickPlayStop();
        });


        updateControls();
    }

    public void clickPlayStop() {
        if (mShowStopLabel) {
            Log.d(TAG, "stopping movie");
            stopPlayback();
            // Don't update the controls here -- let the task thread do it after the movie has
            // actually stopped.
            //mShowStopLabel = false;
            //updateControls();
        } else {
            if (mPlayTask != null) {
                Log.w(TAG, "movie already playing");
                return;
            }
            Log.d(TAG, "starting movie");
            SpeedControlCallback callback = new SpeedControlCallback();
            /*if (((CheckBox) findViewById(R.id.locked60fps_checkbox)).isChecked()) {
                // TODO: consider changing this to be "free running" mode
                callback.setFixedPlaybackRate(60);
            }*/
            SurfaceTexture st = mTextureView.getSurfaceTexture();
            Surface surface = new Surface(st);
            DecodingClass player = null;
            try {
                player = new DecodingClass(
                        fileFromUri, surface, callback);
            } catch (IOException ioe) {
                Log.e(TAG, "Unable to play movie", ioe);
                surface.release();
                return;
            }
            // adjustAspectRatio(player.getVideoWidth(), player.getVideoHeight());

            mPlayTask = new DecodingClass.PlayTask(player, this);
            if (((CheckBox) findViewById(R.id.loopPlaybackCheckbox)).isChecked()) {
                mPlayTask.setLoopMode(true);
            }

            mShowStopLabel = true;
            updateControls();
            mPlayTask.execute();
        }
    }

    private void adjustAspectRatio(int videoWidth, int videoHeight) {
        int viewWidth = mTextureView.getWidth();
        int viewHeight = mTextureView.getHeight();
        double aspectRatio = (double) videoHeight / videoWidth;

        int newWidth, newHeight;
        if (viewHeight > (int) (viewWidth * aspectRatio)) {
            // limited by narrow width; restrict height
            newWidth = viewWidth;
            newHeight = (int) (viewWidth * aspectRatio);
        } else {
            // limited by short height; restrict width
            newWidth = (int) (viewHeight / aspectRatio);
            newHeight = viewHeight;
        }
        int xoff = (viewWidth - newWidth) / 2;
        int yoff = (viewHeight - newHeight) / 2;
        Log.v(TAG, "video=" + videoWidth + "x" + videoHeight +
                " view=" + viewWidth + "x" + viewHeight +
                " newView=" + newWidth + "x" + newHeight +
                " off=" + xoff + "," + yoff);

        Matrix txform = new Matrix();
        mTextureView.getTransform(txform);
        txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
        //txform.postRotate(10);          // just for fun
        txform.postTranslate(xoff, yoff);
        mTextureView.setTransform(txform);
    }

    private void updateControls() {
        Button play = findViewById(R.id.playBtn);
        if (mShowStopLabel) {
            play.setText("Stop");
        } else {
            play.setText("Play");
        }
        play.setEnabled(mSurfaceTextureReady);

        // We don't support changes mid-play, so dim these.
        CheckBox check = (CheckBox) findViewById(R.id.loopPlaybackCheckbox);
        check.setEnabled(!mShowStopLabel);
        encodeBtn.setEnabled(TransInfo.getState());
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture st, int width, int height) {
        // There's a short delay between the start of the activity and the initialization
        // of the SurfaceTexture that backs the TextureView.  We don't want to try to
        // send a video stream to the TextureView before it has initialized, so we disable
        // the "play" button until this callback fires.
        Log.d(TAG, "SurfaceTexture ready (" + width + "x" + height + ")");
        mSurfaceTextureReady = true;
        updateControls();
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void playbackStopped() {
        Log.d(TAG, "playback stopped");
        mShowStopLabel = false;
        mPlayTask = null;
        updateControls();
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
