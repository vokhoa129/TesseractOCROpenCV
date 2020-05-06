package com.example.tesseractocropencv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

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
        Bitmap bitmapNew = ImageProcessing(bitmap);
//        bitmapNew = Deskewing(bitmapNew);
        doRecognize(bitmapNew);
        image.setImageBitmap(bitmapNew);
    }

    public static Bitmap ImageProcessing(Bitmap input) {
        Bitmap output = Bitmap.createBitmap(input.getWidth(),
                input.getHeight(),
                Bitmap.Config.ARGB_8888);
        Mat source = new Mat();
        Utils.bitmapToMat(input, source);
        Imgproc.cvtColor(source, source, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(source, source, new Size(3, 3), 0);
        Imgproc.threshold(source,source,0,255,Imgproc.THRESH_OTSU);

        //        Imgproc.adaptiveThreshold(source, source, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 4);
        Utils.matToBitmap(source, output);
        return output;
    }
//    public Bitmap Deskewing(Bitmap input) {
//        Bitmap output = Bitmap.createBitmap(input.getWidth(),
//                input.getHeight(),
//                Bitmap.Config.ARGB_8888);
//        Mat source = new Mat();
//        Core.bitwise_not(source, source);
//        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
//        //Find all white pixels
//        Mat wLocMat = Mat.zeros(source.size(), source.type());
//        Core.findNonZero(source, wLocMat);
//
//        //Create an empty Mat and pass it to the function
//        MatOfPoint matOfPoint = new MatOfPoint(wLocMat);
//
//        //Translate MatOfPoint to MatOfPoint2f in order to use at a next step
//        MatOfPoint2f mat2f = new MatOfPoint2f();
//        matOfPoint.convertTo(mat2f, CvType.CV_32FC2);
//
//        //Get rotated rect of white pixels
//        RotatedRect rotatedRect = Imgproc.minAreaRect(mat2f);
//
//        Point[] vertices = new Point[4];
//        rotatedRect.points(vertices);
//        List<MatOfPoint> boxContours = new ArrayList<>();
//        boxContours.add(new MatOfPoint(vertices));
//        Imgproc.drawContours(source, boxContours, 0, new Scalar(128, 128, 128), -1);
//
//        double resultAngle = rotatedRect.angle;
//        if (rotatedRect.size.width > rotatedRect.size.height) {
//            rotatedRect.angle += 90.f;
//        }
//
//        //Or
//        //rotatedRect.angle = rotatedRect.angle < -45 ? rotatedRect.angle + 90.f : rotatedRect.angle;
//
//        Mat result = deskew(source, resultAngle);
//        Utils.matToBitmap(result, output);
//        return output;
//    }
//    public Mat deskew(Mat src, double angle) {
//        Point center = new Point(src.width()/2, src.height()/2);
//        Mat rotImage = Imgproc.getRotationMatrix2D(center, angle, 1.0);
//        //1.0 means 100 % scale
//        Size size = new Size(src.width(), src.height());
//        Imgproc.warpAffine(src, src, rotImage, size, Imgproc.INTER_LINEAR + Imgproc.CV_WARP_FILL_OUTLIERS);
//        return src;
//    }
    public void doRecognize(Bitmap bitmap) {
        if (m_tess == null) {
            return;
        }
        try {
            m_tess.setImage(bitmap);
            String result = m_tess.getUTF8Text();
            text.setText(result);
            writeData(result);
        } catch (Exception e) {
            // Do what you like here...
        }
    }
    public void writeData(String data) {
        checkExternalMedia();
        File root = android.os.Environment.getExternalStorageDirectory();
        text.append("\nExternal file system root: "+root);
        File dir = new File (root.getAbsolutePath() + "/download");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, "myData.txt");

        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            pw.print(data);
            pw.flush();
            pw.close();
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        text.append("\n\nFile written to "+file);
        }

    private void checkExternalMedia() {
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // Can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Can't read or write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        text.append("\n\nExternal Media: readable="
                +mExternalStorageAvailable+" writable="+mExternalStorageWriteable);
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