package com.android.imageuploadcomp;

import com.android.imageuploadcomp.Model.ImageUploadResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ImageUploadService {
    @Multipart
    @POST("upload")
    Call<ImageUploadResponse> uploadImage(
            @Part MultipartBody.Part image
    );
}
