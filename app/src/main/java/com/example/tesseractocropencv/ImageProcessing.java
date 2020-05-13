package com.example.tesseractocropencv;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ImageProcessing {
    public static Bitmap imageProcessing(Bitmap input) {
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
}
