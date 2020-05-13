package com.example.tesseractocropencv;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class WriteFile {

    public static void writeFile(String data){
        if(checkExternalMedia()){
            File root = Environment.getExternalStorageDirectory();
            Log.i("root: ", root.toString());
            File dir = new File (root.getAbsolutePath() + "/Download");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String file_name = data.substring(0, 10);
            File file = new File(dir, file_name+".txt");
            Log.i("File path", String.valueOf(file));
            try {
                FileOutputStream f = new FileOutputStream(file);
                PrintWriter pw = new PrintWriter(f);
                pw.print(data);
                pw.flush();
                pw.close();
                f.close();
                Log.i("Write Result", "Successful!!!");
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("Write Result", "Error!!!");
            }
        }
        else{

        }
    }

    private static boolean checkExternalMedia() {
        String state = Environment.getExternalStorageState();

        boolean mExternalStorageWriteable = false;
        boolean mExternalStorageAvailable = false;
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
        return mExternalStorageAvailable;
    }
}
