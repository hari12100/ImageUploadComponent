package com.android.imageuploadcomp;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.android.imageuploadcomp.Model.ImageUploadResponse;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ImageUploadTask extends AsyncTask<byte[], Void, String> {

    private static final String TAG = "ImageUploadTask";
    private static final String API_BASE_URL = "https://www.google.com/";
    private final ImageUploadService service;
    private Activity context;

    public ImageUploadTask(Activity context) {
        this.context = context;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(ImageUploadService.class);
    }

    @Override
    protected String doInBackground(byte[]... params) {
        try {
            return uploadImage(params[0]);
        } catch (Exception e) {
            Log.e(TAG, "Error uploading image", e);
            return null;
        }
    }

    private String uploadImage(byte[] imageBytes) throws IOException {
        try {
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);

            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", "image.jpg", requestFile);

            Call<ImageUploadResponse> call = service.uploadImage(imagePart);

            Response<ImageUploadResponse> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                return new Gson().toJson(response.body());
            } else {
                showToast("API request failed");
                throw new IOException("API request failed");
            }
        } catch (IOException e) {
            throw e;
        }
    }

    private void showToast(String message) {
        if (context != null) {
            context.runOnUiThread(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
        }
    }
}
