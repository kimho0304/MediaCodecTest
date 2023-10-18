package com.example.mediacodectest;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SampleActivity extends AppCompatActivity {
    private static final String TAG = "SampleActivity_Debug";
    private Uri selectedFile;
    private File fileFromUri;
    private Button runBtn;
    private VideoView textureView;
    private String pathToReEncodedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        selectedFile = getIntent().getParcelableExtra("file");
        try {
            fileFromUri = getFile(getApplicationContext(), selectedFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        textureView = findViewById(R.id.textureView);

        runBtn = findViewById(R.id.runBtn);
        runBtn.setOnClickListener(view -> {


            try {
                DecodingClass.mSourceFile = fileFromUri;
                pathToReEncodedFile =
                        new DecodingClass().changeResolution(fileFromUri);

                Log.i(TAG, pathToReEncodedFile);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }

            File file = new File(pathToReEncodedFile);

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(file.getAbsolutePath());

            Log.i(TAG, mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT));
            Log.i(TAG, mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO));
            Log.i(TAG, mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE));
            textureView.setVideoPath(file.getAbsolutePath());
            textureView.start();
            textureView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() { // 비디오 리스너 등록.
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // 준비 완료되면 비디오 재생.
                    mp.setLooping(true); // 비디오 무한루프 설정: true.
                    mp.start(); // 비디오 재생 시작.
                }
            });
        });



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
