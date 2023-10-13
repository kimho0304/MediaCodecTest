package com.example.mediacodectest;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Surface;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DecodingActivity extends AppCompatActivity {
    private static final String TAG = "DecodingActivity_Debug";
    private Uri selectedFile;
    private File fileFromUri;
    private Surface mOutputSurface;
    private DecodingClass decodeObj;
    private DecodingClass.FrameCallback mFrameCallBack;

    private Button runBtn;

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decoding);

        runBtn = findViewById(R.id.runBtn);

        // 메인 액티비티에서 선택한 파일의 Uri를 받을 때, getParcelableExtra 메소드 사용 필수.
        selectedFile = getIntent().getParcelableExtra("file");

        try {
            fileFromUri = getFile(getApplicationContext(), selectedFile);
            Log.i(TAG, "fileFromUri info: "+fileFromUri.toString());
        } catch (IOException e) {
            Log.e(TAG, "getFile() error.");
            e.printStackTrace();
        }

        try {
            decodeObj = new DecodingClass(fileFromUri, mOutputSurface, mFrameCallBack);
        } catch (IOException e) {
            Log.e(TAG, "Allocation DecodingClass object is failed.");
            e.printStackTrace();
        }

        DecodingClass.PlayerFeedback feedback = null;
        DecodingClass.PlayTask decodeThread = new DecodingClass.PlayTask(decodeObj, feedback);

        runBtn.setOnClickListener(view -> {
            Thread thread = new Thread(decodeThread);
            thread.start();
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
