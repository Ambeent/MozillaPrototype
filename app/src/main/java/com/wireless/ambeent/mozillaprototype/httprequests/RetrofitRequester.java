package com.wireless.ambeent.mozillaprototype.httprequests;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitRequester {


    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

   /* private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addConverterFactory(ScalarsConverterFactory.create());*/


    public static Retrofit.Builder realBuilder(String url){
        return
                new Retrofit.Builder()
                        .baseUrl(url)
                        .addConverterFactory(GsonConverterFactory.create())
                        .addConverterFactory(ScalarsConverterFactory.create());
    }


    public static <S> S createService(Class<S> serviceClass, String ipAddress)
    {
         httpClient
//                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))    //DEBUG
                .connectTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS);

        Retrofit retrofit = realBuilder(ipAddress).client(
                httpClient.build())
                .build();
        return retrofit.create(serviceClass);
    }


}
