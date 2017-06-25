package com.example.aayat.krypto;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.googlecode.tesseract.android.TessBaseAPI;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
public class Capture_Image extends AppCompatActivity {

    Bitmap image=null;
    private TessBaseAPI mTess;
    String datapath = "";
    private static int GALLERY = 1,TAKE_PIC =2;
    String ImageDecode;
    ImageView imageViewLoad;
    Button LoadImage,photobutton,runocr;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture__image);

        imageViewLoad = (ImageView) findViewById(R.id.imageView1);
        LoadImage = (Button) findViewById(R.id.button1);
        photobutton=(Button)findViewById(R.id.button2);
        runocr=(Button)findViewById(R.id.Runocr);



        photobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(photoCaptureIntent, TAKE_PIC);
            }
        });


        LoadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, GALLERY);
            }
        });
    }



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == GALLERY && resultCode == RESULT_OK
                    && null != data) {
                Uri URI = data.getData();
                String[] FILE = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(URI,FILE, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(FILE[0]);
                ImageDecode = cursor.getString(columnIndex);
                cursor.close();
                image = BitmapFactory.decodeFile(ImageDecode);
                String language = "eng";
                datapath = getFilesDir() + "/tesseract/";
                mTess = new TessBaseAPI();
                checkFile(new File(datapath + "tessdata/"));
                mTess.init(datapath, language);
                imageViewLoad.setImageBitmap(image);
            }

           else if (requestCode == TAKE_PIC && resultCode == Activity.RESULT_OK) {
                Bitmap mybitmap = (Bitmap)data.getExtras().get("data");
                String partFilename = currentDateFormat();
                storeCameraPhotoInSDCard(mybitmap, partFilename);

                // display the image from SD Card to ImageView Control
                String storeFilename = "photo_1.jpg";
                image = getImageFileFromSDCard(storeFilename);
                imageViewLoad.setImageBitmap(image);
            }
        }
        catch(Exception e)
        {
            String stackTrace = Log.getStackTraceString(e);
            Toast.makeText(this,stackTrace, Toast.LENGTH_SHORT).show();

        }
    }

    private String currentDateFormat(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH_mm_ss");
        String  currentTimeStamp = dateFormat.format(new Date());
        return currentTimeStamp;
    }

    private void storeCameraPhotoInSDCard(Bitmap bitmap, String currentDate){
        //File outputFile = new File(Environment.getExternalStorageDirectory(), "photo_" + currentDate + ".jpg");

        try {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/saved_images");
            myDir.mkdir();
            Log.d("MyDir .. ",myDir.toString());
            String fname = "photo_1.jpg";
            Log.d("fname .. ",fname);
            File outputfile = new File(myDir,fname);
            if (outputfile.exists ())
                outputfile.delete ();
            try {
                outputfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(outputfile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private Bitmap getImageFileFromSDCard(String filename){
        Bitmap bitmap = null;
        File imageFile = new File(Environment.getExternalStorageDirectory().toString()+"/saved_images/" + filename);
        try {
            FileInputStream fis = new FileInputStream(imageFile);
            bitmap = BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public void send_to_processImage(View view){
        String OCRresult = null;
        mTess.setImage(image);
        OCRresult = mTess.getUTF8Text();
        //OCRTextView.setText(OCRresult);
        Intent intent = new Intent(this, Image_Process.class);
        intent.putExtra("MESSAGE",OCRresult);
        startActivity(intent);
    }

    private void checkFile(File dir) {
        if (!dir.exists()&& dir.mkdirs()){
            copyFiles();
        }
        if(dir.exists()) {
            String datafilepath = datapath+ "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);

            if (!datafile.exists()) {
                copyFiles();
            }
        }
    }

    private void copyFiles() {
        try {
            String filepath = datapath + "/tessdata/eng.traineddata";
            AssetManager assetManager = getAssets();

            InputStream instream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }

            outstream.flush();
            outstream.close();
            instream.close();

            File file = new File(filepath);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}





