package com.android.imageuploadcomp.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.imageuploadcomp.ImageUploadComponent;
import com.android.imageuploadcomp.R;

public class MainActivity extends AppCompatActivity implements ImageUploadComponent.OnImageSelectedListener {
    private ImageUploadComponent imageUploadComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageUploadComponent = findViewById(R.id.imageUploadComponent);
        imageUploadComponent.setOnImageSelectedListener(this);
    }

    @Override
    public void onImageSelected(Uri imageUri) {
        Log.d("imageUri", imageUri.toString());
        if (imageUri != null) {
            /*Intent previewIntent = new Intent(this, ImagePreviewActivity.class);
            previewIntent.putExtra("imageUri", imageUri.toString());
            startActivity(previewIntent);*/
        } else {
            Toast.makeText(this, "Image selection failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageUploadComponent.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        imageUploadComponent.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}