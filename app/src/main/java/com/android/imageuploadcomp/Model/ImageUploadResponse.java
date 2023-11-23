package com.android.imageuploadcomp.Model;

import com.google.gson.annotations.SerializedName;

public class ImageUploadResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
