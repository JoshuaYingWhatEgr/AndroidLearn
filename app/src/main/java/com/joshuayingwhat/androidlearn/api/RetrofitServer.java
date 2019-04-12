package com.joshuayingwhat.androidlearn.api;

import com.joshuayingwhat.androidlearn.bean.CategoryResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RetrofitServer {
    @GET("random/data/福利/{number}")
    Call<CategoryResult> getRandomBeauties(@Path("number") int number);
}
