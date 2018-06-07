package com.wireless.ambeent.mozillaprototype.httprequests;

import com.wireless.ambeent.mozillaprototype.pojos.MessageObject;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface IRest {


 //   @FormUrlEncoded
    @POST(".")
    Call<ResponseBody> sendMessagesToNetwork(@Body ArrayList<MessageObject> messageObjects);



}
