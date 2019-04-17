package com.example.compress3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Date;
public class MainActivityImpl extends Main1 {
     public class Main1 extends AppCompatActivity {

         @Override
         protected void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
             setContentView(R.layout.activity_main);
         }


         protected void onActivityResult(int requestCode, int resultCode, Intent data) {
             super.onActivityResult(requestCode, resultCode, data);

             if (resultCode == Activity.RESULT_OK) {
                 switch (requestCode) {
                     case PICK_GALLERY_IMAGE:
                         Uri uriPhoto = data.getData();
                         Log.d(TAG + ".PICK_GALLERY_IMAGE", "Selected image uri path :" + uriPhoto.toString());

                         img.setImageURI(uriPhoto);

                         sourceFile = new File(getPathFromGooglePhotosUri(uriPhoto));

                         destFile = new File(file, "img_" + dateFormatter.format(new Date()).toString() + ".png");

                         Log.d(TAG, "Source File Path :" + sourceFile);

                         try {
                             copyFile(sourceFile, destFile);
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
                         break;
                     case PICK_CAMERA_IMAGE:
                         Log.d(TAG + ".PICK_CAMERA_IMAGE", "Selected image uri path :" + imageCaptureUri);
                         img.setImageURI(imageCaptureUri);
                         break;
                 }
             }
         }

         public void copyFile(File sourceFile, File destFile) throws IOException {
             if (!sourceFile.exists()) {
                 return;
             }

             FileChannel source = null;
             FileChannel destination = null;
             source = new FileInputStream(sourceFile).getChannel();
             destination = new FileOutputStream(destFile).getChannel();
             if (destination != null && source != null) {
                 destination.transferFrom(source, 0, source.size());
             }
             if (source != null) {
                 source.close();
             }
             if (destination != null) {
                 destination.close();
             }
         }

         public String getPathFromGooglePhotosUri(Uri uriPhoto) {
             if (uriPhoto == null)
                 return null;

             FileInputStream input = null;
             FileOutputStream output = null;
             try {
                 ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uriPhoto, "r");
                 FileDescriptor fd = pfd.getFileDescriptor();
                 input = new FileInputStream(fd);

                 String tempFilename = getTempFilename(this);
                 output = new FileOutputStream(tempFilename);

                 int read;
                 byte[] bytes = new byte[4096];
                 while ((read = input.read(bytes)) != -1) {
                     output.write(bytes, 0, read);
                 }
                 return tempFilename;
             } catch (IOException ignored) {
                 // Nothing we can do
             } finally {
                 closeSilently(input);
                 closeSilently(output);
             }
             return null;
         }

         public void closeSilently(Closeable c) {
             if (c == null)
                 return;
             try {
                 c.close();
             } catch (Throwable t) {
                 // Do nothing
             }
         }

         private String getTempFilename(Context context) throws IOException {
             File outputDir = context.getCacheDir();
             File outputFile = File.createTempFile("image", "tmp", outputDir);
             return outputFile.getAbsolutePath();
         }
     }
}