package com.example.mediacodectest;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {
    public static final String TAG = "MainActivity_Debug";
    public TextureView surface;
    public Uri selectedFile;
    public File file;
    private static final int REQ_CODE = 123; // startActivityForResult에 쓰일 사용자 정의 요청 코드

    // UI variables definition ↓
    private Button select_file_btn;
    public Surface mOutputSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surface = findViewById(R.id.surface);
        surface.setSurfaceTextureListener(this);

        select_file_btn = findViewById(R.id.selectFileBtn);
        select_file_btn.setOnClickListener(view -> {
            openFile();

            /*ExtractDecodeEditEncodeMuxTest.srcFd = getApplicationContext().getResources().openRawResourceFd(R.raw.heartless);
            new Thread() {
                public void run() {
                    ExtractDecodeEditEncodeMuxTest test = new ExtractDecodeEditEncodeMuxTest();
                    test.setContext(MainActivity.this);
                    try {
                        test.testExtractDecodeEditEncodeMuxAudioVideo(nOutputSurface);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }.start();*/
        });
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture st, int width, int height) {
        mOutputSurface = new Surface(st);
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture st, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture st) {
        return (st == null);
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture st) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { // startActivityForResult 실행 후, 값을 전달 받는 액티비티 함수. startActivityForResult 실행 후 파일 탐색기 액티비티에서 파일을 선택 하면, 이 선택 된 파일이 전달할 값이 됨.
        // 그러므로 onActivityResult에 이 선택 된 파일을 전달함.
        super.onActivityResult(requestCode, resultCode, data); // 생성자 호출
        Log.i("videocrypto", "onActivityResult");

        if (requestCode == REQ_CODE && resultCode == Activity.RESULT_OK) { // 요청 코드가 사용자가 정의한 값이랑 동일하고, 결과가 OK 상태(정상적)인 경우:
            if (data != null) { // 전달 받은 값(파일 탐색기에서 선택한 파일)이 존재할 경우:
                selectedFile = data.getData(); // URI 객체인 selectedFile에 그 값을 할당.

                // URI 객체인 selectedFile을 새로운 DocumentFile형 객체에 할당. URI 객체의 경우 파일의 원본 정보가 아닌 새로 래핑된 정보만 제공하므로,
                // 원본 정보를 볼 수 있는 DocumentFile 객체에 새로 할당한다.


                /*try {
                    File tmp = getFile(getApplicationContext(), selectedFile);
                    Log.i(TAG, "Path of selected video: " + tmp.getAbsolutePath());
                    ExtractBitmaps.inputFile = tmp;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ExtractBitmaps extractor = new ExtractBitmaps(mOutputSurface);
                try {
                    /*SurfaceTexture st = surface.getSurfaceTexture();
                    Surface s = new Surface(st);
                    extractor.testExtractMpegFrames();
                } catch (Throwable e) {
                    e.printStackTrace();
                }*/
            }
        }

       Intent toDecodeActivity = new Intent(this, DecodingActivity.class);
        toDecodeActivity.putExtra("file", selectedFile);
        startActivity(toDecodeActivity);
    }

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

    // https://stackoverflow.com/questions/45589736/uri-file-size-is-always-0
    private String getRealSizeFromUri(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Audio.Media.SIZE};
            cursor = context.getContentResolver().query(uri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    protected void openFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); // 파일 탐색기를 여는 intent 등록
        intent.addCategory(Intent.CATEGORY_OPENABLE); //onActivityResult 내에서 사용할 ContentProvider로 URI 객체를 접근하기 위해 쓰이는 intent 등록.
        //ContetProvider는 app 사이에서 data를 공유하는 역할을 함. ContentResolver는 ContentProvider를 통해 데이터에 접근해서 값을 가져옴.
        intent.setType("*/*"); //파일 형식 지정: *은 모든 타입을 의미

        startActivityForResult(intent, REQ_CODE); //위의 intent를 통해 파일 탐색 액티비티 실행.
    }

}