package com.example.tesseractocropencv;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public class MainActivity extends Activity {
    private TessBaseAPI m_tess;
    private static final int SELECT_IMAGE = 1;
    Button chooseImage;
    ImageView image;
    TextView text;
    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        grantWritePermission();
        chooseImage = findViewById(R.id.bt_choose_image);
        image = findViewById(R.id.iv_image);
        text = findViewById(R.id.tv_text);

        initImageView();
    }
    private void initImageView() {
        chooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setChooseImage();
            }
        });
        try {
            prepareLanguageDirVie();
            prepareLanguageDirEng();
            prepareLanguageDirFra();
            m_tess = new TessBaseAPI();
            m_tess.init(String.valueOf(getFilesDir()), "vie+eng+fra");
        } catch (Exception e) {
            // Logging here
        }
    }

    private void setChooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = null;
        Uri selectedImageUri = data.getData();
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
            }
        }
        Bitmap bitmapNew = ImageProcessing.imageProcessing(bitmap);
//        bitmapNew = Deskewing(bitmapNew);
        doRecognize(bitmapNew);
        image.setImageBitmap(bitmapNew);
    }
    public void doRecognize(Bitmap bitmap) {
        if (m_tess == null) {
            return;
        }
        try {
            m_tess.setImage(bitmap);
            String result = m_tess.getUTF8Text();
            text.setText(result);
            WriteFile.writeFile(result);
        } catch (Exception e) {
            // Do what you like here...
        }
    }

    private void grantWritePermission(){
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "READ_EXTERNAL_STORAGE & WRITE_EXTERNAL_STORAGE  allowed !!!", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(MainActivity.this, "READ_EXTERNAL_STORAGE & WRITE_EXTERNAL_STORAGEdenied !!!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }


    private void copyFileVie() throws IOException {
        // work with assets folder
        AssetManager assMng = getAssets();
        InputStream isVie = assMng.open("tessdata/vie.traineddata");
        OutputStream osVie = new FileOutputStream(getFilesDir() + "/tessdata/vie.traineddata");
        byte[] buffer = new byte[1024];
        int read;
        while ((read = isVie.read(buffer)) != -1) {
            osVie.write(buffer, 0, read);
        }

        isVie.close();
        osVie.flush();
        osVie.close();
    }

    private void prepareLanguageDirVie() throws IOException {
        File dir = new File(getFilesDir() + "/tessdata");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File trainedData = new File(getFilesDir() + "/tessdata/vie.traineddata");
        if (!trainedData.exists()) {
            copyFileVie();
        }
   }

    private void copyFileEng() throws IOException {
        // work with assets folder
        AssetManager assMng = getAssets();
        InputStream isEng = assMng.open("tessdata/eng.traineddata");
        OutputStream osEng = new FileOutputStream(getFilesDir() + "/tessdata/eng.traineddata");
        byte[] buffer = new byte[1024];
        int read;
        while ((read = isEng.read(buffer)) != -1) {
            osEng.write(buffer, 0, read);
        }

        isEng.close();
        osEng.flush();
        osEng.close();
    }

    private void prepareLanguageDirEng() throws IOException {
        File dir = new File(getFilesDir() + "/tessdata");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File trainedData = new File(getFilesDir() + "/tessdata/eng.traineddata");
        if (!trainedData.exists()) {
            copyFileEng();
        }
    }

    private void copyFileFra() throws IOException {
        // work with assets folder
        AssetManager assMng = getAssets();
        InputStream isFra = assMng.open("tessdata/fra.traineddata");
        OutputStream osFra = new FileOutputStream(getFilesDir() + "/tessdata/fra.traineddata");
        byte[] buffer = new byte[1024];
        int read;
        while ((read = isFra.read(buffer)) != -1) {
            osFra.write(buffer, 0, read);
        }

        isFra.close();
        osFra.flush();
        osFra.close();
    }

    private void prepareLanguageDirFra() throws IOException {
        File dir = new File(getFilesDir() + "/tessdata");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File trainedData = new File(getFilesDir() + "/tessdata/fra.traineddata");
        if (!trainedData.exists()) {
            copyFileFra();
        }
    }
}