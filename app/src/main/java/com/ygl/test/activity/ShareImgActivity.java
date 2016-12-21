package com.ygl.test.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.ygl.test.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ShareImgActivity extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_img);
        imageView = (ImageView) findViewById(R.id.iv_share_img);
        handleImage();
    }

    private void handleImage(){
        Intent intent=getIntent();
        String action=intent.getAction();
        String type=intent.getType();
        if(action.equals(Intent.ACTION_SEND)&&type.equals("image/*")){
            Uri uri=intent.getParcelableExtra(Intent.EXTRA_STREAM);
            //接收多张图片
            //ArrayList<Uri> uris=intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if(uri!=null ){
                try {
                    FileInputStream fileInputStream=new FileInputStream(uri.getPath());
                    Bitmap bitmap= BitmapFactory.decodeStream(fileInputStream);
                    imageView.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
