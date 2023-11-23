package com.android.imageuploadcomp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
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

@SuppressLint("StaticFieldLeak")
public class ImageUploadTask extends AsyncTask<byte[], Void, String> {

    private static final String TAG = "ImageUploadTask";
    private static final String API_BASE_URL = "https://www.google.com/";
    private final ImageUploadService service;
    private final Activity context;
    private final ProgressBar progressBar;

    public ImageUploadTask(Activity context, ProgressBar progressBar) {
        this.progressBar = progressBar;
        this.context = context;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(ImageUploadService.class);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Show the progress bar before starting the task
        progressBar.setVisibility(View.VISIBLE);
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
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        // Hide the progress bar after the task is completed
        progressBar.setVisibility(View.INVISIBLE);
        // Handle the result if needed
        // You may show a toast, update UI, etc.
    }

    private void showToast(String message) {
        if (context != null) {
            context.runOnUiThread(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
        }
    }
}
