package com.android.imageuploadcomp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.webkit.MimeTypeMap;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;

import com.android.imageuploadcomp.Activity.ImagePreviewActivity;
import com.android.imageuploadcomp.Utils.ImageCompressionUtil;
import com.android.imageuploadcomp.Utils.ImageUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class ImageUploadComponent extends RelativeLayout {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_CAPTURE_REQUEST = 2;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 3;
    private Activity mActivity;

    public interface OnImageSelectedListener {
        void onImageSelected(Uri imageUri);
    }

    private OnImageSelectedListener onImageSelectedListener;
    private AppCompatButton btnPreview, btnSubmit;
    private AppCompatTextView tvFileInfo;

    private AppCompatImageView imageView;
    private Uri selectedImageUri;

    public ImageUploadComponent(Context context) {
        super(context);
        if (context instanceof Activity) {
            this.mActivity = (Activity) context;
        } else {
            throw new IllegalArgumentException("Context must be an instance of Activity");
        }
        init();
    }

    public ImageUploadComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (context instanceof Activity) {
            this.mActivity = (Activity) context;
        } else {
            throw new IllegalArgumentException("Context must be an instance of Activity");
        }
        init();
    }

    public ImageUploadComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (context instanceof Activity) {
            this.mActivity = (Activity) context;
        } else {
            throw new IllegalArgumentException("Context must be an instance of Activity");
        }
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.upload_component, this, true);

        AppCompatButton btnSelectImage = findViewById(R.id.btnSelectImage);
        btnPreview = findViewById(R.id.btnPreview);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvFileInfo = findViewById(R.id.tvFileInfo);
        imageView = findViewById(R.id.imageView);

        imageView.setOnClickListener(v -> selectImage());
        btnSelectImage.setOnClickListener(v -> selectImage());

        btnPreview.setOnClickListener(v -> {
            Intent previewIntent = new Intent(mActivity, ImagePreviewActivity.class);
            previewIntent.putExtra("imageUri", selectedImageUri.toString());
            mActivity.startActivity(previewIntent);
        });

        btnSubmit.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                byte[] imageData = ImageUtils.convertUriToByteArray(mActivity, selectedImageUri, 800, 600);
                ;

                ImageUploadTask uploadTask = new ImageUploadTask(mActivity);
                uploadTask.execute(imageData);
            }
        });

    }

    private void selectImage() {
        if (mActivity != null && mActivity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        } else {
            initiateImageCapture();
        }
    }

    private void requestCameraPermission() {
        if (mActivity != null) {
            ActivityCompat.requestPermissions(mActivity,
                    new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initiateImageCapture();
            } else {
                Toast.makeText(getContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initiateImageCapture() {

        CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mActivity);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, (dialog, item) -> {
            switch (item) {
                case 0:
                    Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    mActivity.startActivityForResult(intentCamera, CAMERA_CAPTURE_REQUEST);
                    break;
                case 1:
                    Intent intentFile = new Intent(Intent.ACTION_GET_CONTENT);
                    intentFile.setType("image/*");
                    mActivity.startActivityForResult(intentFile, PICK_IMAGE_REQUEST);
                    break;
                case 2:
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            displayFileInfo();
            if (onImageSelectedListener != null) {
                onImageSelectedListener.onImageSelected(selectedImageUri);
            }
            //compressAndUploadImage(selectedImageUri);

            imageView.setImageURI(selectedImageUri);
        } else if (requestCode == CAMERA_CAPTURE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            selectedImageUri = getImageUri(mActivity, photo);

            displayFileInfo();
            if (onImageSelectedListener != null) {
                onImageSelectedListener.onImageSelected(selectedImageUri);
            }
            //compressAndUploadImage(selectedImageUri);

            imageView.setImageURI(selectedImageUri);
        } else {
            Toast.makeText(mActivity, "Image selection/capture failed", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri getImageUri(Context context, Bitmap image) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), image, "Title", null);
        return Uri.parse(path);

        /*File imageFile = ImageCompressionUtil.saveBitmapToFile(context, image, "compressed_image.jpg");
        return Uri.fromFile(imageFile);*/
    }

    private void compressAndUploadImage(Uri imageUri) {
        Bitmap compressedBitmap = ImageCompressionUtil.compressImage(mActivity, imageUri, 800, 800);

        File compressedFile = ImageCompressionUtil.saveBitmapToFile(mActivity, compressedBitmap, "compressed_image.jpg");

        if (onImageSelectedListener != null) {
            onImageSelectedListener.onImageSelected(Uri.fromFile(compressedFile));
        }
    }

    private void displayFileInfo() {
        if (selectedImageUri != null) {
            String fileName = getFileName(selectedImageUri);
            String fileType = getFileType(selectedImageUri);
            String fileInfo = "File: " + fileName + "\nType: " + fileType;
            tvFileInfo.setText(fileInfo);
            btnPreview.setVisibility(VISIBLE);
            btnSubmit.setVisibility(VISIBLE);
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private String getFileType(Uri uri) {
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public void setOnImageSelectedListener(OnImageSelectedListener listener) {
        this.onImageSelectedListener = listener;
    }
}