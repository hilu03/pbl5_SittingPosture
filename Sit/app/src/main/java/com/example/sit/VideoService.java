package com.example.sit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
public interface VideoService
{
    @GET("/video_feed")
    Call<ResponseBody> getVideoFrame();
}
